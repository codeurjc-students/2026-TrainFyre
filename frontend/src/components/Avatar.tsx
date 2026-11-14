interface AvatarProps {
    readonly url?: string
    readonly large?: boolean
}

/**
 * Imagen de perfil decorativa (el nombre va al lado). Sin imagen no se muestra
 * nada, salvo en su versión grande, que deja un marco en blanco.
 */
export function Avatar({ url, large = false }: AvatarProps) {
    const className = `avatar${large ? ' large' : ''}`

    if (url) return <img className={className} src={url} alt="" />
    if (large) return <span className={`${className} empty`} aria-hidden="true" />

    return null
}
