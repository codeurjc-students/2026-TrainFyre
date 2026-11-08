import { describe, expect, it } from 'vitest'

import { resolveApiBaseUrl, resolvePageSize } from '../../src/api/config'

describe('Configuración de la API', () => {
    it('acepta HTTPS y elimina barras finales', () => {
        expect(resolveApiBaseUrl('https://api.example.com/api/v1///')).toBe('https://api.example.com/api/v1')
    })

    it('usa el valor por defecto cuando no hay configuración', () => {
        expect(resolveApiBaseUrl(undefined)).toBe('https://localhost:443/api/v1')
    })

    it('admite HTTP solo en la propia máquina', () => {
        expect(resolveApiBaseUrl('http://localhost:8080/api/v1')).toBe('http://localhost:8080/api/v1')
        expect(() => resolveApiBaseUrl('http://api.example.com')).toThrow(/HTTPS/)
    })

    it('rechaza URLs inválidas, esquemas peligrosos y credenciales embebidas', () => {
        expect(() => resolveApiBaseUrl('no es una url')).toThrow()
        expect(() => resolveApiBaseUrl('javascript:alert(1)')).toThrow(/HTTPS/)
        expect(() => resolveApiBaseUrl('https://user:pass@api.example.com')).toThrow(/credenciales/)
    })

    it('valida el tamaño de página', () => {
        expect(resolvePageSize('25')).toBe(25)
        expect(resolvePageSize('0')).toBe(100)
        expect(resolvePageSize('abc')).toBe(100)
        expect(resolvePageSize(undefined)).toBe(100)
    })
})
