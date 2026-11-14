import { api, notify } from '../../api'
import { Action } from '../../components/Action'
import { Avatar } from '../../components/Avatar'
import { Field } from '../../components/Field'
import { Form } from '../../components/Form'
import { Notice } from '../../components/Notice'
import { useAvatar } from '../../hooks/useAvatar'
import { AVATAR_MAX_MB, AVATAR_TYPES, avatarFromForm } from '../../model'

interface AvatarSectionProps {
    readonly id: string
}

const ACCEPTED_TYPES = AVATAR_TYPES.join(',')

export function AvatarSection({ id }: AvatarSectionProps) {
    const { url, error } = useAvatar(id)

    async function upload(data: FormData) {
        await api.uploadAvatar(id, avatarFromForm(data))
        notify('Foto de perfil actualizada.')
    }

    async function remove() {
        if (!globalThis.confirm('¿Eliminar la foto de perfil?')) return

        await api.deleteAvatar(id)
        notify('Foto de perfil eliminada.')
    }

    return (
        <section className="card avatar-section">
            <h2>Foto de perfil</h2>

            <div className="avatar-preview">
                <Avatar url={url} large />
                <p className="muted small">
                    {url ? 'Foto de perfil actual.' : 'Todavía no hay foto de perfil.'}
                </p>
            </div>

            <Notice error>{error}</Notice>

            <Form label="Subir foto" onSubmit={upload}>
                <Field label="Imagen" hint={`PNG, JPEG o WebP. Máximo ${AVATAR_MAX_MB} MB.`}>
                    <input name="file" type="file" accept={ACCEPTED_TYPES} />
                </Field>
            </Form>

            {url && (
                <Action danger onClick={remove}>
                    Eliminar foto
                </Action>
            )}
        </section>
    )
}
