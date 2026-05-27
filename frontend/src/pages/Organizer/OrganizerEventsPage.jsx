import { useEffect, useState } from "react";
import { Card, Group, Select, Stack, Text, Title } from "@mantine/core";
import { getEventsByOrganizerId, updateEventStatus } from "../../api/eventsApi.js";

export default function OrganizerEventsPage({ organizerId }) {
    const [events, setEvents] = useState([]);

    // fetch only on load / reload
    useEffect(() => {
        let active = true;

        async function loadEvents() {
            try {
                const data = await getEventsByOrganizerId(organizerId);
                if (!active) {
                    return;
                }
                setEvents(data);
            } catch (err) {
                if (active) {
                    console.error(err);
                }
            }
        }

        if (organizerId) loadEvents();
        return () => {
            active = false;
        };
    }, [organizerId]);


    function handleStatusChange(eventId, status) {

        setEvents((prev) =>
            prev.map((e) =>
                e.id === eventId
                    ? { ...e, status }
                    : e
            )
        );

        updateEventStatus(eventId, status).catch((err) => {
            console.error("Backend update failed:", err);
        });
    }

    return (
        <>
            <Title mb="lg">My Events</Title>

            <Stack>
                {events.map((event) => (
                    <Card key={event.id} withBorder p="md" mb="sm">
                        <Group justify="space-between" align="center">

                            <div>
                                <Text fw={600}>{event.title}</Text>
                                <Text size="sm" c="dimmed">
                                    {event.startDatetime}
                                </Text>
                            </div>

                            <Select
                                value={event.status}
                                data={[
                                    { value: "DRAFT", label: "Draft" },
                                    { value: "PUBLISHED", label: "Published" },
                                    { value: "CANCELED", label: "Canceled" },
                                ]}
                                onChange={(value) =>
                                    handleStatusChange(event.id, value)
                                }
                                w={150}
                            />
                        </Group>
                    </Card>
                ))}
            </Stack>
        </>
    );
}
