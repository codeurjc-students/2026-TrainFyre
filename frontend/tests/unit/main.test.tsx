import { screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/src/App.tsx', () => ({ default: () => <p>App stub</p> }))

const reactEnv = globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }

describe('main entry point', () => {
  beforeEach(() => {
    document.body.innerHTML = '<div id="root"></div>'
    vi.resetModules()
    // main.tsx renders with createRoot outside of act(); that is expected here
    reactEnv.IS_REACT_ACT_ENVIRONMENT = false
  })

  afterEach(() => {
    reactEnv.IS_REACT_ACT_ENVIRONMENT = true
    document.body.innerHTML = ''
  })

  it('mounts the App in the #root element', async () => {
    await import('@/src/main.tsx')

    expect(await screen.findByText('App stub')).toBeInTheDocument()
    expect(document.getElementById('root')).toContainElement(screen.getByText('App stub'))
  })
})
