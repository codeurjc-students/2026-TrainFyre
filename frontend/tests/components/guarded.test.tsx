// @vitest-environment jsdom
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { Action } from '../../src/components/Action'
import { Form } from '../../src/components/Form'
import { useResource } from '../../src/hooks/useResource'

afterEach(cleanup)

describe('Form', () => {
    it('ignora envíos duplicados mientras hay uno en curso', async () => {
        let release: () => void = () => {}
        const onSubmit = vi.fn(() => new Promise<void>(resolve => { release = resolve }))

        render(<Form label="Enviar" onSubmit={onSubmit}><input name="a" defaultValue="x" /></Form>)

        const button = screen.getByRole('button', { name: 'Enviar' })

        await userEvent.click(button)
        await userEvent.click(button)

        expect(onSubmit).toHaveBeenCalledTimes(1)
        expect(onSubmit.mock.calls[0]).toHaveLength(1)

        release()
        await waitFor(() => expect(screen.getByRole('button', { name: 'Enviar' })).toBeEnabled())
    })

    it('muestra el mensaje de error de la tarea', async () => {
        render(<Form onSubmit={async () => { throw new Error('Fallo controlado') }}><input name="a" /></Form>)

        await userEvent.click(screen.getByRole('button', { name: 'Guardar cambios' }))

        expect((await screen.findByRole('alert')).textContent).toBe('Fallo controlado')
    })
})

describe('Action', () => {
    it('muestra errores y vuelve a habilitarse', async () => {
        render(<Action onClick={async () => { throw new Error('No se pudo') }}>Borrar</Action>)

        await userEvent.click(screen.getByRole('button', { name: 'Borrar' }))

        expect((await screen.findByRole('alert')).textContent).toBe('No se pudo')
        expect(screen.getByRole('button', { name: 'Borrar' })).toBeEnabled()
    })
})

describe('useResource', () => {
    function View({ loader }: { readonly loader: (signal: AbortSignal) => Promise<string> }) {
        const resource = useResource('k', loader)

        if (resource.loading) return <p>cargando</p>
        if (resource.error) return <p role="alert">{resource.error}</p>

        return <button onClick={resource.reload}>{resource.data}</button>
    }

    it('carga, recarga y notifica errores', async () => {
        const loader = vi.fn<(signal: AbortSignal) => Promise<string>>()
            .mockResolvedValueOnce('uno')
            .mockResolvedValueOnce('dos')
            .mockRejectedValueOnce(new Error('roto'))

        render(<View loader={loader} />)

        await userEvent.click(await screen.findByRole('button', { name: 'uno' }))
        await userEvent.click(await screen.findByRole('button', { name: 'dos' }))

        expect((await screen.findByRole('alert')).textContent).toBe('roto')
        expect(loader).toHaveBeenCalledTimes(3)
    })

    it('cancela la petición al desmontar', async () => {
        let captured: AbortSignal | undefined
        const loader = (signal: AbortSignal) => {
            captured = signal
            return new Promise<string>(() => {})
        }

        const { unmount } = render(<View loader={loader} />)

        await waitFor(() => expect(captured).toBeDefined())
        unmount()

        expect(captured?.aborted).toBe(true)
    })
})
