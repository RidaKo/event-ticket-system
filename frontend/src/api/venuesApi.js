import {apiFetch} from "./client.js";

export function getVenues() {
    return apiFetch("/venues");
}
export function createVenue(data) {
    return apiFetch("/venues", {
        method: "POST",
        body: JSON.stringify(data),
    });
}

export function getVenuesByOrganizerId(organizerId) {
    return apiFetch(`/organizers/${organizerId}/venues`);
}