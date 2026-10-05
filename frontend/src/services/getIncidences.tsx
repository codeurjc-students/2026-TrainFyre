import axios from 'axios';
import { API_BASE_URL } from '@/src/config/api';
import type { PagedResponseIncidence } from '@/src/types/incidences';

export async function getIncidences(page: number, size: number, signal?: AbortSignal): Promise<PagedResponseIncidence> {
    const response = await axios.get<PagedResponseIncidence>(
        `${API_BASE_URL}/incidence`,
        { params: { page, size }, signal }
    );

    return response.data;
}
