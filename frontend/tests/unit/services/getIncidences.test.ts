import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getIncidences } from '@/src/services/getIncidences'
import { makeIncidence, makePage } from '../../support/fixtures'

vi.mock('axios', () => ({ default: { get: vi.fn() } }))

describe('getIncidences service', () => {
  beforeEach(() => {
    vi.mocked(axios.get).mockReset()
  })

  it('requests GET /incidence with the page and size as query params', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: makePage([], 0) })
    const controller = new AbortController()

    await getIncidences(2, 50, controller.signal)

    expect(axios.get).toHaveBeenCalledWith('http://api.test/incidence', {
      params: { page: 2, size: 50 },
      signal: controller.signal,
    })
  })

  it('returns only the body of the response', async () => {
    const page = makePage([makeIncidence()], 1)
    vi.mocked(axios.get).mockResolvedValue({ data: page, status: 200 })

    await expect(getIncidences(0, 100)).resolves.toBe(page)
  })

  it('does not require an abort signal', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: makePage([], 0) })

    await getIncidences(0, 10)

    expect(axios.get).toHaveBeenCalledWith('http://api.test/incidence', {
      params: { page: 0, size: 10 },
      signal: undefined,
    })
  })

  it('propagates the error when the request fails', async () => {
    vi.mocked(axios.get).mockRejectedValue(new Error('Network Error'))

    await expect(getIncidences(0, 100)).rejects.toThrow('Network Error')
  })
})
