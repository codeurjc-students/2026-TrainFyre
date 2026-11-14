// @vitest-environment jsdom
import { act, cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { api, ApiError, getAuthState, logout } from '../../src/api'
import { avatarChanged } from '../../src/api/avatarSync'
import { publishSession } from '../../src/api/session'
import { Avatar } from '../../src/components/Avatar'
import { SiteHeader } from '../../src/components/SiteHeader'
import { AvatarSection } from '../../src/pages/account/AvatarSection'

const id = '00000000-0000-4000-8000-000000000001'
const PNG_DATA_URL = 'data:image/png;base64,eA=='

function png() {
    return new Blob(['x'], { type: 'image/png' })
}

function signIn() {
    publishSession({
        id,
        token: 'token',
        deadline: Date.now() + 60_000,
        user: { username: 'pablo', roles: ['USER'], alertSubscriptions: [] },
    })
}

/**
 * jsdom no refleja en `new FormData(form)` los archivos asignados por
 * `userEvent.upload` (llegan vacíos). Un navegador real sí lo hace, así que el
 * helper completa el campo desde `input.files`.
 */
function formDataSeesSelectedFiles() {
    const Original = globalThis.FormData

    vi.stubGlobal('FormData', class extends Original {
        constructor(form?: HTMLFormElement) {
            super(form)

            const selected = form?.querySelector<HTMLInputElement>('input[type="file"]')?.files?.[0]

            if (selected) this.set('file', selected)
        }
    })
}

afterEach(() => {
    cleanup()
    logout('')
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
})

describe('Avatar', () => {
    it('no muestra nada sin imagen en su versión pequeña', () => {
        const { container } = render(<Avatar />)

        expect(container).toBeEmptyDOMElement()
    })

    it('deja un marco en blanco sin imagen en su versión grande', () => {
        const { container } = render(<Avatar large />)

        expect(container.querySelector('span.avatar.large.empty')).toHaveAttribute('aria-hidden', 'true')
        expect(container.querySelector('img')).toBeNull()
    })

    it('muestra la imagen y admite el tamaño grande', () => {
        const { container } = render(<Avatar url={PNG_DATA_URL} large />)
        const image = container.querySelector('img')

        expect(image).toHaveAttribute('src', PNG_DATA_URL)
        expect(image).toHaveClass('avatar', 'large')
        expect(image).toHaveAttribute('alt', '')
    })
})

describe('Avatar en la cabecera', () => {
    it('no pide ninguna imagen a un visitante', () => {
        const avatar = vi.spyOn(api, 'avatar')

        render(<SiteHeader path="/" />)

        expect(avatar).not.toHaveBeenCalled()
    })

    it('muestra la imagen del usuario autenticado', async () => {
        const avatar = vi.spyOn(api, 'avatar').mockResolvedValue(png())

        signIn()
        const { container } = render(<SiteHeader path="/" />)

        await waitFor(() => expect(container.querySelector('img.avatar')).toHaveAttribute('src', PNG_DATA_URL))
        expect(avatar).toHaveBeenCalledWith('me', expect.any(AbortSignal))
    })

    it('deja la cabecera como siempre si el usuario no tiene avatar', async () => {
        const avatar = vi.spyOn(api, 'avatar').mockResolvedValue(null)

        signIn()
        const { container } = render(<SiteHeader path="/" />)

        await waitFor(() => expect(avatar).toHaveBeenCalled())
        expect(container.querySelector('.avatar')).toBeNull()
        expect(screen.getByRole('link', { name: 'pablo' })).toBeInTheDocument()
    })
})

describe('AvatarSection', () => {
    it('invita a subir una foto cuando no existe y oculta el borrado', async () => {
        const avatar = vi.spyOn(api, 'avatar').mockResolvedValue(null)

        render(<AvatarSection id={id} />)

        await waitFor(() => expect(avatar).toHaveBeenCalledWith(id, expect.any(AbortSignal)))
        expect(screen.getByText('Todavía no hay foto de perfil.')).toBeInTheDocument()
        expect(screen.queryByRole('button', { name: 'Eliminar foto' })).not.toBeInTheDocument()
    })

    it('muestra la foto actual y permite eliminarla tras confirmar', async () => {
        const user = userEvent.setup()

        vi.spyOn(api, 'avatar').mockResolvedValue(png())
        const remove = vi.spyOn(api, 'deleteAvatar').mockResolvedValue(undefined)
        const confirm = vi.fn(() => false)

        vi.stubGlobal('confirm', confirm)
        render(<AvatarSection id={id} />)

        const button = await screen.findByRole('button', { name: 'Eliminar foto' })

        expect(screen.getByText('Foto de perfil actual.')).toBeInTheDocument()

        await user.click(button)
        expect(remove).not.toHaveBeenCalled()

        confirm.mockReturnValue(true)
        await user.click(button)

        await waitFor(() => expect(remove).toHaveBeenCalledWith(id))
        expect(getAuthState().notice).toBe('Foto de perfil eliminada.')
    })

    it('sube la imagen elegida', async () => {
        const user = userEvent.setup()
        const file = new File(['x'], 'foto.png', { type: 'image/png' })

        formDataSeesSelectedFiles()
        vi.spyOn(api, 'avatar').mockResolvedValue(null)
        const upload = vi.spyOn(api, 'uploadAvatar').mockResolvedValue(undefined)

        render(<AvatarSection id={id} />)

        await user.upload(screen.getByLabelText(/Imagen/), file)
        await user.click(screen.getByRole('button', { name: 'Subir foto' }))

        await waitFor(() => expect(upload).toHaveBeenCalledWith(id, expect.any(File)))
        expect(upload.mock.calls[0][1].name).toBe('foto.png')
        expect(getAuthState().notice).toBe('Foto de perfil actualizada.')
    })

    it('rechaza en cliente un archivo que no es una imagen admitida', async () => {
        const user = userEvent.setup({ applyAccept: false })
        const file = new File(['x'], 'documento.pdf', { type: 'application/pdf' })

        formDataSeesSelectedFiles()
        vi.spyOn(api, 'avatar').mockResolvedValue(null)
        const upload = vi.spyOn(api, 'uploadAvatar')

        render(<AvatarSection id={id} />)

        await user.upload(screen.getByLabelText(/Imagen/), file)
        await user.click(screen.getByRole('button', { name: 'Subir foto' }))

        expect(await screen.findByRole('alert')).toHaveTextContent('PNG, JPEG o WebP')
        expect(upload).not.toHaveBeenCalled()
    })

    it('muestra el error del backend al cargar la foto', async () => {
        vi.spyOn(api, 'avatar').mockRejectedValue(new ApiError(403, 'Sin permiso para ver la foto.'))

        render(<AvatarSection id={id} />)

        expect(await screen.findByRole('alert')).toHaveTextContent('Sin permiso para ver la foto.')
    })

    it('recarga la foto cuando cambia desde otra vista', async () => {
        const avatar = vi.spyOn(api, 'avatar').mockResolvedValue(null)

        render(<AvatarSection id={id} />)
        await waitFor(() => expect(avatar).toHaveBeenCalledTimes(1))

        act(() => avatarChanged())

        await waitFor(() => expect(avatar).toHaveBeenCalledTimes(2))
    })
})
