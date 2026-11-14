/**
 * Señal compartida de "el avatar ha cambiado". Permite que todas las vistas
 * (cabecera, página de cuenta) recarguen la imagen tras subirla o eliminarla.
 */
let version = 0

const listeners = new Set<() => void>()

export const getAvatarVersion = () => version

export function subscribeAvatar(listener: () => void) {
    listeners.add(listener)

    return () => {
        listeners.delete(listener)
    }
}

export function avatarChanged() {
    version++

    for (const listener of listeners) listener()
}
