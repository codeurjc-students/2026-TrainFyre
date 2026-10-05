// hooks/useAllIncidences.ts
import { useEffect, useState } from 'react';
import { getIncidences } from '../services/getIncidences';
import type { PagedResponseIncidence } from '../types/incidences';

type Incidence = PagedResponseIncidence['content'][number];

export function useAllIncidences(size: number) {
    const [incidencias, setIncidencias] = useState<Incidence[]>([]);
    const [total, setTotal] = useState<number | null>(null);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const controller = new AbortController();

        async function cargarTodas() {
            let page = 0;
            let acumuladas: Incidence[] = [];

            setIncidencias([]);
            setTotal(null);
            setError(null);
            setCargando(true);

            try {
                while (!controller.signal.aborted) {
                    const respuesta = await getIncidences(
                        page,
                        size,
                        controller.signal
                    );

                    if (controller.signal.aborted) return;

                    acumuladas = [...acumuladas, ...respuesta.content];
                    setIncidencias(acumuladas); // Muestra esta página sin esperar a las demás
                    setTotal(respuesta.totalElements);

                    if (
                        respuesta.content.length === 0 ||
                        acumuladas.length >= respuesta.totalElements
                    ) {
                        break;
                    }

                    page++;
                }
            } catch (err: unknown) {
                if (!controller.signal.aborted) {
                    setError(err instanceof Error ? err.message : 'Error desconocido');
                }
            } finally {
                if (!controller.signal.aborted) setCargando(false);
            }
        }

        void cargarTodas();

        return () => controller.abort();
    }, [size]);

    return { incidencias, total, cargando, error };
}
