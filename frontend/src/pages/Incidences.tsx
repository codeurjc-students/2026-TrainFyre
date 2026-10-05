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

    return (
        <section>
            <p>
                Mostrando {incidencias.length}
                {total !== null && ` de ${total}`} incidencias
            </p>

            {error && <p>Error al cargar incidencias: {error}</p>}

            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead>Nombre</TableHead>
                        <TableHead>Lineas afectadas</TableHead>
                        <TableHead>Retraso previsto</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    {incidencias.map((incidencia) => (
                        <TableRow key={incidencia.id}>
                            <TableCell>{incidencia.description.name}</TableCell>
                            <TableCell>{incidencia.affectedNetwork.lineIds}</TableCell>
                            <TableCell>{Temporal.Duration.from(incidencia.occurrence.duration).total({ unit: "minutes" })}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            {cargando && <p>Cargando incidencias...</p>}
        </section>
    );
}
