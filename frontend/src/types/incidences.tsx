export type Severity =
    | "CRITICAL"
    | "HIGH"
    | "MODERATE"
    | "LOW"
    | "INFORMATIONAL";

export type Cause =
    | "WEATHER"
    | "MAINTENANCE"
    | "STRIKE"
    | "TECHNICAL_PROBLEM"
    | "MEDICAL_EMERGENCY"
    | "POLICE_ACTION"
    | "HOLIDAY"
    | "OTHER";

export interface AffectedNetwork {
    mapId: number;
    lineIds: number[];
}

export interface Classification {
    severity: Severity;
    cause: Cause;
}

export interface Description {
    name: string;
    summary: string;
}

export interface Occurrence {
    timestamp: string; // date-time
    duration: string;   // duration
    instantaneous: boolean;
}

export interface UpdateIncidenceCommand {
    uuid: string; // uuid
    changeAffectedNetwork?: AffectedNetwork;
    changeOccurrence?: Occurrence;
    changeDescription?: Description;
    changeClassification?: Classification;
}

export interface CreateIncidenceCommand {
    affectedNetwork: AffectedNetwork;
    occurrence: Occurrence;
    description: Description;
    classification: Classification;
}

export interface IncidenceDetails {
    affectedNetwork: AffectedNetwork;
    occurrence: Occurrence;
    description: Description;
    classification: Classification;
}

export interface Incidence {
    id?: string; // uuid
    affectedNetwork: AffectedNetwork;
    occurrence: Occurrence;
    description: Description;
    classification: Classification;
    incidenceDetails: IncidenceDetails;
}

export interface PagedResponseIncidence {
    content: Incidence[];
    page: number;
    size: number;
    totalElements: number;
}

export interface DeleteIncidenceCommand {
    id: string; // uuid
}
