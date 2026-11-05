import { Require } from './components/Require'
import { Heading } from './components/Heading'
import { isUuid } from './model'
import { Access } from './pages/Access'
import { AdminUsers } from './pages/AdminUsers'
import { Home } from './pages/Home'
import { NotFound } from './pages/NotFound'
import { UserPage } from './pages/account/UserPage'
import { IncidenceEditor } from './pages/incidences/IncidenceEditor'
import { IncidenceList } from './pages/incidences/IncidenceList'
import { IncidencePage } from './pages/incidences/IncidencePage'

const ADMIN_USER_ROUTE = /^\/admin\/usuarios\/([^/]+)$/
const INCIDENCE_ROUTE = /^\/incidencias\/([^/]+)(\/editar)?$/

interface RoutesProps {
    readonly path: string
}

function NewIncidence() {
    return (
        <Require admin>
            <Heading title="Nueva incidencia">Publica una actualización de la red.</Heading>
            <IncidenceEditor />
        </Require>
    )
}

function dynamicRoute(path: string) {
    const userId = ADMIN_USER_ROUTE.exec(path)?.[1]

    if (userId && isUuid(userId)) {
        return <Require admin><UserPage id={userId} /></Require>
    }

    const incidence = INCIDENCE_ROUTE.exec(path)

    if (incidence && isUuid(incidence[1])) {
        const editing = !!incidence[2]
        const page = <IncidencePage id={incidence[1]} edit={editing} />

        return editing ? <Require admin>{page}</Require> : page
    }

    return <NotFound />
}

export function Routes({ path }: RoutesProps) {
    switch (path) {
        case '/': return <Home />
        case '/acceso': return <Access />
        case '/registro': return <Access register />
        case '/incidencias': return <IncidenceList />
        case '/cuenta': return <Require><UserPage /></Require>
        case '/incidencias/nueva': return <NewIncidence />
        case '/admin/usuarios': return <Require admin><AdminUsers /></Require>
        default: return dynamicRoute(path)
    }
}
