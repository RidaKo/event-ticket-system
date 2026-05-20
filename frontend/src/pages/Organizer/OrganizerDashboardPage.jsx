import { Card, SimpleGrid, Text, Title } from "@mantine/core";
import { useEffect, useState } from "react";
import {getEventsByOrganizerId} from "../../api/eventsApi";
import {getVenuesByOrganizerId} from "../../api/venuesApi";

export default function OrganizerDashboardPage({ organizerId }) {
    const [events, setEvents] = useState([]);
    const [venues, setVenues] = useState([]);

    useEffect(() => {
        async function load() {
            try {
                const [eventsData, venuesData] = await Promise.all([
                    getEventsByOrganizerId(organizerId),
                    getVenuesByOrganizerId(organizerId),
                ]);

                setEvents(eventsData);
                setVenues(venuesData);

                console.log("EVENTS:", eventsData);
                console.log("VENUES:", venuesData);
            } catch (err) {
                console.error("Dashboard load error:", err);
            }
        }

        if (organizerId) load();
    }, [organizerId]);

    return (
        <>
            <Title mb="lg">Organizer Dashboard</Title>

            <SimpleGrid cols={3}>
                <Card withBorder p="lg">
                    <Text size="sm" c="dimmed">
                        Total Events
                    </Text>
                    <Title order={2}>{events.length}</Title>
                </Card>

                <Card withBorder p="lg">
                    <Text size="sm" c="dimmed">
                        Tickets Sold
                    </Text>
                    <Title order={2}>0</Title>
                </Card>

                <Card withBorder p="lg">
                    <Text size="sm" c="dimmed">
                        Venues
                    </Text>
                    <Title order={2}>{venues.length}</Title>
                </Card>
            </SimpleGrid>
        </>
    );
}