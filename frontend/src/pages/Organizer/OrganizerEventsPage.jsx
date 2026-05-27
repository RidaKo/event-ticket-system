import { useEffect, useState } from "react";
import { Card, Group, Select, Stack, Text, Title } from "@mantine/core";
import { getEvent, getEventsByOrganizerId, updateEventStatus } from "../../api/eventsApi.js";
import ConflictDialog from "../../components/ConflictDialog.jsx";

export default function OrganizerEventsPage({ organizerId }) {
  const [events, setEvents] = useState([]);
  const [conflictInfo, setConflictInfo] = useState(null);

  async function loadEvents() {
    try {
      const data = await getEventsByOrganizerId(organizerId);
      setEvents(data);
    } catch (err) {
      console.error(err);
    }
  }

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const data = await getEventsByOrganizerId(organizerId);
        if (!active) return;
        setEvents(data);
      } catch (err) {
        if (active) console.error(err);
      }
    }

    if (organizerId) load();
    return () => {
      active = false;
    };
  }, [organizerId]);

  async function handleStatusChange(eventId, status, version) {
    // Optimistic update
    setEvents((prev) =>
      prev.map((e) => (e.id === eventId ? { ...e, status } : e))
    );

    try {
      await updateEventStatus(organizerId, eventId, status, version);
      // Reload to pick up the incremented @Version so the next change
      // sends the correct version number.
      loadEvents();
    } catch (err) {
      if (err.status === 409) {
        setConflictInfo({ eventId, intendedStatus: status });
      } else {
        console.error("Backend update failed:", err);
      }
      loadEvents();
    }
  }

  async function handleConflictRefresh() {
    await loadEvents();
  }

  async function handleConflictOverwrite() {
    if (!conflictInfo) return;
    try {
      const freshEvent = await getEvent(conflictInfo.eventId);
      await updateEventStatus(
        organizerId,
        conflictInfo.eventId,
        conflictInfo.intendedStatus,
        freshEvent.version
      );
      await loadEvents();
    } catch (err) {
      console.error("Force overwrite failed:", err);
      await loadEvents();
    }
  }

  return (
    <>
      <Title mb="lg">My Events</Title>

      <ConflictDialog
        opened={conflictInfo !== null}
        onClose={() => setConflictInfo(null)}
        onRefresh={handleConflictRefresh}
        onOverwrite={handleConflictOverwrite}
      />

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
                onChange={(value) => {
                  if (value) handleStatusChange(event.id, value, event.version);
                }}
                allowDeselect={false}
                w={150}
              />
            </Group>
          </Card>
        ))}
      </Stack>
    </>
  );
}
