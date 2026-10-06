import { act, renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useAllIncidences } from '@/src/hooks/useAllIncidences'
import { getIncidences } from '@/src/services/getIncidences'
import type { PagedResponseIncidence } from '@/src/types/incidences'
import { makeIncidence, makePage } from '../../support/fixtures'

vi.mock('@/src/services/getIncidences')

const mockedGet = vi.mocked(getIncidences)

/** A promise that the test resolves/rejects by hand, to control timing. */
function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason: unknown) => void
  const promise = new Promise<T>((res, rej) => {
    resolve = res
    reject = rej
  })
  return { promise, resolve, reject }
}

describe('useAllIncidences', () => {
  beforeEach(() => {
    mockedGet.mockReset()
  })

  it('starts in loading state with no data', () => {
    mockedGet.mockReturnValue(new Promise(() => {}))

    const { result } = renderHook(() => useAllIncidences(10))

    expect(result.current).toEqual({ incidencias: [], total: null, cargando: true, error: null })
  })

  it('loads a single page', async () => {
    const items = [makeIncidence(), makeIncidence()]
    mockedGet.mockResolvedValue(makePage(items, 2))

    const { result } = renderHook(() => useAllIncidences(10))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.incidencias).toEqual(items)
    expect(result.current.total).toBe(2)
    expect(result.current.error).toBeNull()
    expect(mockedGet).toHaveBeenCalledTimes(1)
    expect(mockedGet).toHaveBeenCalledWith(0, 10, expect.any(AbortSignal))
  })

  it('keeps requesting pages until all the elements are loaded', async () => {
    const [a, b, c] = [makeIncidence(), makeIncidence(), makeIncidence()]
    mockedGet
      .mockResolvedValueOnce(makePage([a, b], 3, 0, 2))
      .mockResolvedValueOnce(makePage([c], 3, 1, 2))

    const { result } = renderHook(() => useAllIncidences(2))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.incidencias).toEqual([a, b, c])
    expect(mockedGet.mock.calls.map(([page, size]) => [page, size])).toEqual([
      [0, 2],
      [1, 2],
    ])
  })

  it('shows each page as soon as it arrives, without waiting for the rest', async () => {
    const [a, b] = [makeIncidence(), makeIncidence()]
    const second = deferred<PagedResponseIncidence>()
    mockedGet.mockResolvedValueOnce(makePage([a], 2, 0, 1)).mockReturnValueOnce(second.promise)

    const { result } = renderHook(() => useAllIncidences(1))

    await waitFor(() => expect(result.current.incidencias).toEqual([a]))
    expect(result.current.cargando).toBe(true)
    expect(result.current.total).toBe(2)

    await act(async () => second.resolve(makePage([b], 2, 1, 1)))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.incidencias).toEqual([a, b])
  })

  it('stops when the server returns an empty page even if more were announced', async () => {
    mockedGet
      .mockResolvedValueOnce(makePage([makeIncidence()], 10, 0, 1))
      .mockResolvedValueOnce(makePage([], 10, 1, 1))

    const { result } = renderHook(() => useAllIncidences(1))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.incidencias).toHaveLength(1)
    expect(mockedGet).toHaveBeenCalledTimes(2)
  })

  it('reports the message of the error and stops loading', async () => {
    mockedGet.mockRejectedValue(new Error('Request failed with status code 500'))

    const { result } = renderHook(() => useAllIncidences(10))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.error).toBe('Request failed with status code 500')
    expect(result.current.incidencias).toEqual([])
  })

  it('reports a generic message when the thrown value is not an Error', async () => {
    mockedGet.mockRejectedValue('boom')

    const { result } = renderHook(() => useAllIncidences(10))

    await waitFor(() => expect(result.current.error).toBe('Error desconocido'))
    expect(result.current.cargando).toBe(false)
  })

  it('ignores a response that arrives after unmounting', async () => {
    const pending = deferred<PagedResponseIncidence>()
    mockedGet.mockReturnValue(pending.promise)

    const { result, unmount } = renderHook(() => useAllIncidences(10))
    const signal = mockedGet.mock.calls[0][2] as AbortSignal

    unmount()
    expect(signal.aborted).toBe(true)

    await act(async () => pending.resolve(makePage([makeIncidence()], 1)))

    expect(result.current.incidencias).toEqual([])
    expect(result.current.cargando).toBe(true)
    expect(mockedGet).toHaveBeenCalledTimes(1)
  })

  it('ignores an error that arrives after unmounting', async () => {
    const pending = deferred<PagedResponseIncidence>()
    mockedGet.mockReturnValue(pending.promise)

    const { result, unmount } = renderHook(() => useAllIncidences(10))
    unmount()

    await act(async () => pending.reject(new Error('canceled')))

    expect(result.current.error).toBeNull()
    expect(result.current.cargando).toBe(true)
  })

  it('discards the previous load and starts again when the size changes', async () => {
    const stale = deferred<PagedResponseIncidence>()
    const fresh = makeIncidence()
    mockedGet
      .mockReturnValueOnce(stale.promise)
      .mockResolvedValueOnce(makePage([fresh], 1, 0, 5))

    const { result, rerender } = renderHook(({ size }) => useAllIncidences(size), {
      initialProps: { size: 10 },
    })

    rerender({ size: 5 })
    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.incidencias).toEqual([fresh])

    // The response of the first (aborted) request must not overwrite the data
    await act(async () => stale.resolve(makePage([makeIncidence(), makeIncidence()], 2)))
    expect(result.current.incidencias).toEqual([fresh])
    expect(mockedGet).toHaveBeenNthCalledWith(2, 0, 5, expect.any(AbortSignal))
  })
})
