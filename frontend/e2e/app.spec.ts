import {
    expect,
    test,
} from '@playwright/test'

const USER_ID = '00000000-0000-4000-8000-000000000001'
const INCIDENCE_ID = '00000000-0000-4000-8000-000000000002'

const API_ORIGIN = 'https://localhost:8443'
const APP_ORIGIN = 'http://localhost:5173'
const INCIDENCE_NAME = 'Trabajos en la vía'
const HEADING_LOGIN = 'Inicia sesión'

const CORS_HEADERS = {
    'Access-Control-Allow-Origin': APP_ORIGIN,
}

test('un visitante consulta incidencias y no ve administración', async ({ page }) => {
    await page.route(`${API_ORIGIN}/**`, route => route.fulfill({
        status: 200,
        contentType: 'application/json',
        headers: CORS_HEADERS,
        body: JSON.stringify({
            content: [
                {
                    id: INCIDENCE_ID,
                    affectedNetwork: {
                        mapId: 1,
                        lineIds: [4],
                    },
                    occurrence: {
                        timestamp: '2026-11-03T12:00:00Z',
                        duration: 'PT15M',
                    },
                    description: {
                        name: INCIDENCE_NAME,
                        summary: 'Mantenimiento programado.',
                    },
                    classification: {
                        severity: 'LOW',
                        cause: 'MAINTENANCE',
                    },
                },
            ],
            page: 0,
            size: 100,
            totalElements: 1,
        }),
    }))

    await page.goto('/#/incidencias')

    await expect(
        page.getByRole('link', { name: INCIDENCE_NAME }),
    ).toBeVisible()

    await expect(
        page.getByRole('link', { name: 'Administración' }),
    ).toHaveCount(0)

    await page.getByRole('link', { name: INCIDENCE_NAME }).click()

    await expect(
        page.getByRole('heading', { name: 'Qué está ocurriendo' }),
    ).toBeVisible()
})

test('el registro inicia sesión y no persiste credenciales', async ({ page }) => {
    const calls: string[] = []

    const payload = Buffer.from(JSON.stringify({
        sub: USER_ID,
        roles: ['USER'],
        exp: Math.floor(Date.now() / 1000) + 900,
    })).toString('base64url')

    const jwt = `header.${payload}.signature`

    await page.route(`${API_ORIGIN}/**`, async route => {
        const request = route.request()
        const path = new URL(request.url()).pathname

        if (request.method() === 'OPTIONS') {
            await route.fulfill({
                status: 204,
                headers: {
                    ...CORS_HEADERS,
                    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
                    'Access-Control-Allow-Headers': 'Authorization, Content-Type',
                },
            })

            return
        }

        calls.push(`${request.method()} ${path}`)

        let body: unknown
        let status = 200

        if (
            path === '/users' &&
            request.method() === 'POST'
        ) {
            body = USER_ID
            status = 201
        } else if (path === '/auth/login') {
            body = {
                accessToken: jwt,
                tokenType: 'Bearer',
                expiresIn: 900,
            }
        } else if (path === '/users/me') {
            body = {
                username: 'pablo',
                roles: ['USER'],
                alertSubscriptions: [],
            }
        } else {
            status = 404
            body = {}
        }

        await route.fulfill({
            status,
            contentType: 'application/json',
            headers: CORS_HEADERS,
            body: JSON.stringify(body),
        })
    })

    await page.goto('/#/registro')

    // Seleccionamos por name para que los textos de ayuda
    // incluidos en los labels no alteren los selectores.
    await page.locator('input[name="username"]').fill('pablo')

    await page.locator('input[name="password"]').fill(
        'contraseña-segura',
    )

    await page.locator('input[name="confirmation"]').fill(
        'contraseña-segura',
    )

    await page.getByRole('button', {
        name: 'Crear cuenta y entrar',
    }).click()

    await expect(
        page.getByRole('heading', {
            name: 'Mi cuenta',
            exact: true,
        }),
    ).toBeVisible()

    expect(calls).toContain('POST /users')
    expect(calls).toContain('POST /auth/login')

    expect(
        calls.indexOf('POST /users'),
    ).toBeLessThan(
        calls.indexOf('POST /auth/login'),
    )

    const stored = await page.evaluate(() => ({
        local: localStorage.length,
        session: sessionStorage.length,
    }))

    expect(stored).toEqual({
        local: 0,
        session: 0,
    })

    await page.reload()

    await expect(
        page.getByRole('heading', {
            name: HEADING_LOGIN,
            exact: true,
        }),
    ).toBeVisible()
})

test('las rutas administrativas no son accesibles sin sesión', async ({ page }) => {
    await page.goto('/#/admin/usuarios')

    await expect(
        page.getByRole('heading', {
            name: HEADING_LOGIN,
            exact: true,
        }),
    ).toBeVisible()

    await expect(
        page.getByRole('heading', {
            name: 'Crear usuario',
            exact: true,
        }),
    ).toHaveCount(0)
})
