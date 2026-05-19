import {
  Alert,
  AspectRatio,
  Badge,
  Box,
  Button,
  Grid,
  Group,
  Loader,
  Paper,
  SimpleGrid,
  Stack,
  Text,
  ThemeIcon,
  Title,
} from "@mantine/core";
import { useEffect, useState } from "react";
import { getEvent } from "../api/eventsApi.js";

function formatDate(value, timeZone) {
  if (!value) {
    return "Date to be announced";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Date to be announced";
  }

  try {
    return new Intl.DateTimeFormat("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
      year: "numeric",
      ...(timeZone ? { timeZone } : {}),
    }).format(date);
  } catch {
    return new Intl.DateTimeFormat("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
      year: "numeric",
    }).format(date);
  }
}

function formatTime(value, timeZone) {
  if (!value) {
    return "Time to be announced";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Time to be announced";
  }

  try {
    return new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      minute: "2-digit",
      timeZoneName: "short",
      ...(timeZone ? { timeZone } : {}),
    }).format(date);
  } catch {
    return new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      minute: "2-digit",
    }).format(date);
  }
}

function compactAddress(event) {
  return [event.address || event.venue?.addressLine1, event.city || event.venue?.city, event.country || event.venue?.country]
    .filter(Boolean)
    .join(", ");
}

function getUnavailableReason(event) {
  if (event.salesEnabled === false) {
    return "Ticket sales are not available for this event.";
  }

  const status = typeof event.status === "string" ? event.status.toLowerCase() : "";
  if (status === "draft" || status === "cancelled" || status === "canceled") {
    return "Ticket sales are not available for this event.";
  }

  const availability = typeof event.availability === "string" ? event.availability.toLowerCase() : "";
  if (availability === "sold_out" || availability === "sold out" || availability === "unavailable") {
    return "Ticket sales are not available for this event.";
  }

  return "";
}

export default function EventDetailsPage({ eventId, navigate }) {
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");

    getEvent(eventId)
      .then((data) => {
        if (!active) {
          return;
        }
        setEvent(data);
      })
      .catch((err) => {
        if (!active) {
          return;
        }
        setEvent(null);
        setError(err.message || "Unable to load event.");
      })
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [eventId]);

  if (loading && !event) {
    return (
      <Group gap="sm">
        <Loader size="sm" color="brand" />
        <Text c="dimmed">Loading event...</Text>
      </Group>
    );
  }

  if (error || !event) {
    return (
      <Alert color="red" variant="light">
        {error || "Event not found"}
      </Alert>
    );
  }

  const address = compactAddress(event);
  const ticketEventId = event.checkoutEventId || event.id;
  const eventTimeZone = event.timezone || event.timeZone || event.venue?.timezone;
  const eventDate = formatDate(event.startDatetime, eventTimeZone);
  const eventTime = formatTime(event.startDatetime, eventTimeZone);
  const unavailableReason = getUnavailableReason(event);

  return (
    <Stack gap="lg" className="event-details-shell">
      <Group justify="space-between" align="center" gap="md">
        <Button variant="subtle" color="brand" onClick={() => navigate("/")}>
          Browse Events
        </Button>
        {event.categoryName && (
          <Badge radius="sm" variant="light" color="brand">
            {event.categoryName}
          </Badge>
        )}
      </Group>

      <Paper className="detail-section" radius="md" p="lg" withBorder>
        <Grid gutter="lg" align="stretch">
          <Grid.Col span={{ base: 12, md: 5 }}>
            <AspectRatio ratio={16 / 10}>
              {event.coverPhotoUrl ? (
                <Box component="img" className="event-details-image" src={event.coverPhotoUrl} alt={event.title} />
              ) : (
                <Box className="media-placeholder">
                  <Text size="xs" fw="bold" tt="uppercase" c="brand.5">
                    Image
                  </Text>
                </Box>
              )}
            </AspectRatio>
          </Grid.Col>
          <Grid.Col span={{ base: 12, md: 7 }}>
            <Stack gap="md" h="100%" justify="space-between">
              <Stack gap="sm">
                <Title order={2} c="brand.9">
                  {event.title}
                </Title>
                <Text c="dimmed">{event.description || "Event details will be available soon."}</Text>
              </Stack>

              <Group justify="space-between" align="center" gap="md">
                <Stack gap={2}>
                  <Text size="sm" fw="bold" c="brand.9">
                    {eventDate}
                  </Text>
                  <Text size="sm" c="dimmed">
                    {eventTime}
                  </Text>
                </Stack>
                <Button
                  color="brand"
                  disabled={Boolean(unavailableReason)}
                  onClick={() => navigate(`/events/${ticketEventId}/checkout/tickets`)}
                >
                  {unavailableReason ? "Tickets unavailable" : "Buy tickets"}
                </Button>
              </Group>
              {unavailableReason && (
                <Text size="sm" c="dimmed">
                  {unavailableReason}
                </Text>
              )}
            </Stack>
          </Grid.Col>
        </Grid>
      </Paper>

      <Paper className="detail-section" radius="md" p="lg" withBorder>
        <Stack gap="md">
          <Title order={3} size="h4" c="brand.9">
            Event details
          </Title>
          <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="sm">
            <InfoBlock label="Date" value={eventDate} />
            <InfoBlock label="Time" value={eventTime} />
            <InfoBlock label="Venue" value={event.venueName || event.venue?.name || "Venue to be announced"} />
            <InfoBlock label="Address" value={address || "Address to be announced"} />
          </SimpleGrid>
        </Stack>
      </Paper>

      {(event.averageRating || event.reviewCount || event.reviews?.length > 0) && (
        <Stack gap="md">
          <Paper className="detail-section" radius="md" p="lg" withBorder>
            <Group gap="md" align="center">
              <ThemeIcon size={52} radius="md" variant="light" color="brand">
                {Number(event.averageRating || 0).toFixed(1)}
              </ThemeIcon>
              <Stack gap={2}>
                <Text fw="bold" c="brand.9">
                  {Number(event.averageRating || 0) >= 4 ? "Very good" : "Rating"}
                </Text>
                <Text size="sm" c="dimmed">
                  Based on {event.reviewCount || event.reviews?.length || 0} reviews
                </Text>
              </Stack>
            </Group>
          </Paper>

          {event.reviews?.length > 0 && (
            <Paper className="detail-section" radius="md" p="lg" withBorder>
              <Stack gap="md">
                <Title order={3} size="h4" c="brand.9">
                  Reviews
                </Title>
                {event.reviews.map((review) => (
                  <Box key={review.id} className="confirmation-line">
                    <Group justify="space-between" align="flex-start" gap="md" wrap="nowrap">
                      <Stack gap={2}>
                        <Text size="sm" fw="bold" c="brand.9">
                          {review.userName || "Guest"}
                        </Text>
                        <Text size="sm" c="dimmed">
                          {review.comment}
                        </Text>
                      </Stack>
                      <Badge radius="sm" variant="light" color="brand">
                        {Number(review.rating).toFixed(1)}
                      </Badge>
                    </Group>
                  </Box>
                ))}
              </Stack>
            </Paper>
          )}
        </Stack>
      )}
    </Stack>
  );
}

function InfoBlock({ label, value }) {
  return (
    <Paper className="event-info-block" radius="sm" p="md" withBorder>
      <Stack gap={4}>
        <Text size="xs" fw="bold" c="dimmed" tt="uppercase">
          {label}
        </Text>
        <Text size="sm" fw="bold" c="brand.9">
          {value}
        </Text>
      </Stack>
    </Paper>
  );
}
