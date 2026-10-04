"use client"

import { useEffect, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"

type Incidence = {
    id?: string
    affectedNetwork?: {
        mapId?: number
        lineIds?: number[]
    }
    occurrence?: {
        timestamp?: string
        duration?: string
        instantaneous?: boolean
    }
    description?: {
        name?: string
        summary?: string
    }
    classification?: {
        severity?: string
        cause?: string
    }
    incidenceDetails?: Omit<Incidence, "id" | "incidenceDetails">
}

type PagedResponse = {
    content?: Incidence[]
    page?: number
    size?: number
    totalElements?: number
}

type GroupField = "mapId" | "lineIds" | "duration" | "severity" | "cause"

type Group = {
    key: string
    label: string
    count: number
    children?: Group[]
    incidences?: Incidence[]
}

const PAGE_SIZE = 100
const API_URL = "http://localhost:8080"


const FIELD_NAMES: Record<GroupField, string> = {
    mapId: "Mapa",
    lineIds: "Línea",
    duration: "Duración",
    severity: "Gravedad",
    cause: "Causa",
}

const EXTRA_FIELDS: GroupField[] = [
    "lineIds",
    "duration",
    "severity",
    "cause",
]

function networkOf(incidence: Incidence) {
    return (
        incidence.affectedNetwork ??
        incidence.incidenceDetails?.affectedNetwork
    )
}

function occurrenceOf(incidence: Incidence) {
    return incidence.occurrence ?? incidence.incidenceDetails?.occurrence
}

function descriptionOf(incidence: Incidence) {
    return incidence.description ?? incidence.incidenceDetails?.description
}

function classificationOf(incidence: Incidence) {
    return (
        incidence.classification ??
        incidence.incidenceDetails?.classification
    )
}

function formatDurationMinutes(duration?: string): string {
    if (!duration) return "—"

    const match = duration.match(
        /^P(?:(\d+)W)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:[.,]\d+)?)S)?)?$/i,
    )

    if (!match || match.slice(1).every((part) => part === undefined)) {
        return "—"
    }

    const [, weeks, days, hours, minutes, seconds] = match
    const totalMinutes =
        Number(weeks ?? 0) * 10080 +
        Number(days ?? 0) * 1440 +
        Number(hours ?? 0) * 60 +
        Number(minutes ?? 0) +
        Number((seconds ?? "0").replace(",", ".")) / 60

    return `${new Intl.NumberFormat("es-ES", {
        maximumFractionDigits: 2,
    }).format(totalMinutes)} min`
}


// Las duraciones ISO con años o meses no equivalen a un número fijo
// de horas, así que se clasifican por separado.
function durationGroup(duration?: string): string {
    if (!duration) return "Sin duración"

    const match = duration.match(
        /^P(?:(\d+)W)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:[.,]\d+)?)S)?)?$/i,
    )

    if (!match || match.slice(1).every((part) => part === undefined)) {
        return "Duración variable u otra"
    }

    const [, weeks, days, hours, minutes, seconds] = match
    const totalSeconds =
        Number(weeks ?? 0) * 604800 +
        Number(days ?? 0) * 86400 +
        Number(hours ?? 0) * 3600 +
        Number(minutes ?? 0) * 60 +
        Number((seconds ?? "0").replace(",", "."))

    if (totalSeconds < 3600) return "Menos de 1 hora"
    if (totalSeconds < 86400) return "De 1 a menos de 24 horas"
    return "24 horas o más"
}

function groupValues(incidence: Incidence, field: GroupField): string[] {
    switch (field) {
        case "mapId": {
            const id = networkOf(incidence)?.mapId
            return [id == null ? "Sin mapa" : String(id)]
        }
        case "lineIds": {
            const ids = networkOf(incidence)?.lineIds ?? []
            return ids.length
                ? [...new Set(ids.map(String))]
                : ["Sin línea"]
        }
        case "duration":
            return [durationGroup(occurrenceOf(incidence)?.duration)]
        case "severity":
            return [classificationOf(incidence)?.severity ?? "Sin gravedad"]
        case "cause":
            return [classificationOf(incidence)?.cause ?? "Sin causa"]
    }
}

function buildGroups(
    incidences: Incidence[],
    fields: GroupField[],
    depth = 0,
): Group[] {
    const field = fields[depth]
    const buckets = new Map<string, Incidence[]>()

    for (const incidence of incidences) {
        for (const value of groupValues(incidence, field)) {
            const bucket = buckets.get(value) ?? []
            bucket.push(incidence)
            buckets.set(value, bucket)
        }
    }

    return [...buckets.entries()]
        .sort(([a], [b]) =>
            field === "mapId" || field === "lineIds"
                ? Number.isNaN(Number(a)) || Number.isNaN(Number(b))
                    ? a.localeCompare(b, "es", { numeric: true })
                    : Number(a) - Number(b)
                : a.localeCompare(b, "es", { numeric: true }),
        )
        .map(([value, items]) => ({
            key: `${field}:${value}`,
            label: `${FIELD_NAMES[field]}: ${value}`,
            count: items.length,
            ...(depth < fields.length - 1
                ? { children: buildGroups(items, fields, depth + 1) }
                : { incidences: items }),
        }))
}

function formatDate(value?: string) {
    if (!value) return "—"
    const date = new Date(value)
    return Number.isNaN(date.getTime())
        ? value
        : `${new Intl.DateTimeFormat("es-ES", {
            dateStyle: "short",
            timeStyle: "short",
            timeZone: "UTC",
        }).format(date)} UTC`
}

function GroupView({
                       group,
                       depth = 0,
                   }: {
    group: Group
    depth?: number
}) {
    return (
        <Collapsible defaultOpen className="rounded-md border">
            <CollapsibleTrigger className="flex h-auto w-full items-center justify-between gap-3 whitespace-normal rounded-md px-4 py-3 text-left font-medium hover:bg-accent hover:text-accent-foreground">
                <span className="font-semibold">{group.label}</span>
                <span className="shrink-0 text-sm text-muted-foreground">
      {group.count} {group.count === 1 ? "incidencia" : "incidencias"} ▾
    </span>
            </CollapsibleTrigger>

            <CollapsibleContent className="space-y-3 border-t p-3">
                {group.children ? (
                    group.children.map((child) => (
                        <GroupView
                            key={`${group.key}/${child.key}`}
                            group={child}
                            depth={depth + 1}
                        />
                    ))
                ) : (
                    <div className="overflow-x-auto">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Nombre</TableHead>
                                    <TableHead>Líneas</TableHead>
                                    <TableHead>Fecha</TableHead>
                                    <TableHead>Duración</TableHead>
                                    <TableHead>Gravedad</TableHead>
                                    <TableHead>Causa</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {group.incidences?.map((incidence, index) => (
                                    <TableRow key={incidence.id ?? `${group.key}-${index}`}>
                                        <TableCell>
                                            {descriptionOf(incidence)?.name ?? "Sin nombre"}
                                        </TableCell>
                                        <TableCell>
                                            {networkOf(incidence)?.lineIds?.join(", ") || "—"}
                                        </TableCell>
                                        <TableCell>
                                            {formatDate(occurrenceOf(incidence)?.timestamp)}
                                        </TableCell>
                                        <TableCell>
                                            {formatDurationMinutes(occurrenceOf(incidence)?.duration)}
                                        </TableCell>
                                        <TableCell>
                                            {classificationOf(incidence)?.severity ?? "—"}
                                        </TableCell>
                                        <TableCell>
                                            {classificationOf(incidence)?.cause ?? "—"}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </CollapsibleContent>
        </Collapsible>
    )
}

export function Incidencias() {
    const [incidences, setIncidences] = useState<Incidence[]>([])
    const [groupFields, setGroupFields] = useState<GroupField[]>([])
    const [loaded, setLoaded] = useState(0)
    const [total, setTotal] = useState<number | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [retry, setRetry] = useState(0)

    useEffect(() => {
        const controller = new AbortController()

        async function loadAllPages() {
            setLoading(true)
            setError(null)
            setIncidences([])
            setLoaded(0)
            setTotal(null)

            const byId = new Map<string, Incidence>()
            let page = 0
            let fetched = 0

            try {
                while (!controller.signal.aborted) {
                    const response = await fetch(
                        `${API_URL}/incidence?page=${page}&size=${PAGE_SIZE}`,
                        {
                            signal: controller.signal,
                            headers: { Accept: "application/json" },
                            cache: "no-store",
                        },
                    )

                    if (!response.ok) {
                        throw new Error(`La API devolvió HTTP ${response.status}`)
                    }

                    const data: PagedResponse = await response.json()
                    if (!Array.isArray(data.content)) {
                        throw new Error("La respuesta no contiene una lista de incidencias")
                    }

                    if (controller.signal.aborted) return

                    for (const [index, incidence] of data.content.entries()) {
                        byId.set(incidence.id ?? `${page}-${index}`, incidence)
                    }

                    fetched += data.content.length
                    setIncidences([...byId.values()])
                    setLoaded(byId.size)
                    setTotal(data.totalElements ?? null)

                    if (
                        data.content.length === 0 ||
                        (data.totalElements != null && fetched >= data.totalElements) ||
                        (data.totalElements == null && data.content.length < PAGE_SIZE)
                    ) {
                        break
                    }

                    page += 1
                }
            } catch (cause) {
                if (!controller.signal.aborted) {
                    setError(
                        cause instanceof Error
                            ? cause.message
                            : "No se pudieron cargar las incidencias",
                    )
                }
            } finally {
                if (!controller.signal.aborted) setLoading(false)
            }
        }

        void loadAllPages()
        return () => controller.abort()
    }, [retry])

    const groups = useMemo(
        () => buildGroups(incidences, ["mapId", ...groupFields]),
        [incidences, groupFields],
    )
    const availableFields = EXTRA_FIELDS.filter(
        (field) => !groupFields.includes(field),
    )

    return (
        <section
            className="w-full flex-1 space-y-5 p-4"
            aria-label="Página principal de incidencias"
        >
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Incidencias</h1>
                    <p className="text-sm text-muted-foreground" aria-live="polite">
                        {loading ? "Cargando en segundo plano: " : "Cargadas: "}
                        {loaded}
                        {total !== null ? ` de ${total}` : ""}
                    </p>
                </div>
                <Button variant="outline" onClick={() => setRetry((n) => n + 1)}>
                    Recargar
                </Button>
            </div>

            <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm font-medium">Agrupar por:</span>
                <span className="rounded-md border px-3 py-2 text-sm">Mapa</span>

                {groupFields.map((field) => (
                    <Button
                        key={field}
                        variant="outline"
                        onClick={() =>
                            setGroupFields((current) =>
                                current.filter((item) => item !== field),
                            )
                        }
                        aria-label={`Quitar agrupación por ${FIELD_NAMES[field]}`}
                    >
                        {FIELD_NAMES[field]} ×
                    </Button>
                ))}

                {availableFields.length > 0 && (
                    <Select
                        value=""
                        onValueChange={(value) =>
                            setGroupFields((current) => [
                                ...current,
                                value as GroupField,
                            ])
                        }
                    >
                        <SelectTrigger className="w-48" aria-label="Añadir agrupación">
                            <SelectValue placeholder="Añadir agrupación" />
                        </SelectTrigger>
                        <SelectContent>
                            {availableFields.map((field) => (
                                <SelectItem key={field} value={field}>
                                    {FIELD_NAMES[field]}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                )}
            </div>

            {error && (
                <p role="alert" className="text-sm text-destructive">
                    Error al cargar: {error}. Se conservan los datos ya recibidos.
                </p>
            )}

            {groups.length > 0 ? (
                <div className="space-y-3">
                    {groups.map((group) => (
                        <GroupView key={group.key} group={group} />
                    ))}
                </div>
            ) : (
                <p className="text-sm text-muted-foreground">
                    {loading ? "Cargando incidencias…" : "No hay incidencias."}
                </p>
            )}
        </section>
    )
}
