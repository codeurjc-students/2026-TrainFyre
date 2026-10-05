import { useState } from 'react';
import { PAGE_SIZE } from '@/src/config/api';
import { useAllIncidences } from '../hooks/useAllIncidences';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';

export function Incidences() {
    const { incidencias, total, cargando, error } = useAllIncidences(PAGE_SIZE);
    const [mapa, setMapa] = useState('');

    const mapas = [...new Set(
        incidencias.map((incidencia) => incidencia.affectedNetwork.mapId)
    )];

    const incidenciasVisibles = incidencias.filter(
        (incidencia) =>
            mapa === '' || incidencia.affectedNetwork.mapId === Number(mapa)
    );

    return (
        <section className="mx-auto w-full max-w-6xl px-4 py-10 sm:px-6">
            <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
                        Incidencias
                    </h1>
                    <p className="mt-2 text-muted-foreground">
                        Consulta las líneas afectadas y el retraso previsto.
                        En un futuro se mostrarán estadísticas.
                    </p>
                </div>

                <p className="rounded-full bg-secondary px-4 py-2 text-sm font-medium text-secondary-foreground">
                    Mostrando {incidenciasVisibles.length}
                    {mapa === '' && total !== null && ` de ${total}`} incidencias
                </p>
            </div>

            {error && (
                <p role="alert" className="mb-5 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                    Error al cargar incidencias: {error}
                </p>
            )}

            <div className="overflow-hidden rounded-xl border bg-card shadow-sm">
                <label className="mb-5 flex w-fit flex-col gap-1 text-sm font-medium px-6 py-4">
                    <select
                        value={mapa}
                        onChange={(event) => setMapa(event.target.value)}
                        className="h-10 rounded-md border bg-background px-3 font-normal"
                    >
                        <option value="">Todos los mapas</option>
                        {mapas.map((id) => (
                            <option key={id} value={id}>
                                Mapa {id}
                            </option>
                        ))}
                    </select>
                </label>

                <Table className="min-w-[600px]">
                    <TableHeader className="bg-muted/60">
                        <TableRow className="hover:bg-transparent">
                            <TableHead className="px-6 py-4 font-semibold text-foreground">
                                Nombre
                            </TableHead>
                            <TableHead className="px-6 py-4 font-semibold text-foreground">
                                Líneas afectadas
                            </TableHead>
                            <TableHead className="px-6 py-4 text-right font-semibold text-foreground">
                                Retraso previsto
                            </TableHead>
                        </TableRow>
                    </TableHeader>

                    <TableBody>
                        {incidenciasVisibles.map((incidencia) => (
                            <TableRow key={incidencia.id}>
                                <TableCell className="px-6 py-5 font-medium">
                                    {incidencia.description.name}
                                </TableCell>
                                <TableCell className="px-6 py-5 text-muted-foreground">
                                    {incidencia.affectedNetwork.lineIds}
                                </TableCell>
                                <TableCell className="px-6 py-5 text-right font-medium tabular-nums">
                                    {Temporal.Duration.from(
                                        incidencia.occurrence.duration
                                    ).total({ unit: 'minutes' })} min
                                </TableCell>
                            </TableRow>
                        ))}

                        {!cargando && !error && incidenciasVisibles.length === 0 && (
                            <TableRow>
                                <TableCell
                                    colSpan={3}
                                    className="px-6 py-12 text-center text-muted-foreground"
                                >
                                    No hay incidencias disponibles.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>

            {cargando && (
                <p role="status" className="mt-4 text-sm text-muted-foreground">
                    Cargando incidencias…
                </p>
            )}
        </section>
    );
}
