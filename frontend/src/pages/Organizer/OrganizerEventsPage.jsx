import { useEffect, useState } from "react";

import {
    Card,
    Group,
    Stack,
    Text,
    Title,
} from "@mantine/core";

import { getEventsByOrganizerId } from "../../api/eventsApi.js";

export default function OrganizerEventsPage({
                                                organizerId,
                                            }) {

    const [events, setEvents] = useState([]);

    useEffect(() => {
        loadEvents();
    }, []);

    async function loadEvents() {

        try {
            const data =
                await getEventsByOrganizerId(
                    organizerId
                );

            setEvents(data);

        } catch (err) {
            console.error(err);
        }
    }

    return (
        <>
            <Title mb="lg">
                My Events
            </Title>

            <Stack>

                {events.map((event) => (
                    <Card
                        key={event.id}
                        withBorder
                        p="lg"
                    >
                        <Group justify="space-between">

                            <div>
                                <Title order={4}>
                                    {event.title}
                                </Title>

                                <Text c="dimmed">
                                    {event.status}
                                </Text>
                            </div>

                        </Group>
                    </Card>
                ))}

            </Stack>
        </>
    );
}