import {
  Alert,
  Box,
  Button,
  Divider,
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
import { getConfirmation } from "../api/checkoutApi.js";
import { formatDateTime, formatMoney } from "../utils.js";

export default function ConfirmationPage({ orderNumber, navigate }) {
  const [confirmation, setConfirmation] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setError("");

    getConfirmation(orderNumber)
      .then((data) => active && setConfirmation(data))
      .catch((err) => active && setError(err.message));

    return () => {
      active = false;
    };
  }, [orderNumber]);

  if (error) {
    return (
      <Alert color="red" variant="light">
        {error}
      </Alert>
    );
  }

  if (!confirmation) {
    return (
      <Group gap="sm">
        <Loader size="sm" color="brand" />
        <Text c="dimmed">Loading confirmation...</Text>
      </Group>
    );
  }

  return (
    <Stack className="confirmation-shell" gap="md">
      <Paper className="confirmation-header" radius="md" p="xl" withBorder>
        <Stack align="center" gap="xs">
          <ThemeIcon size={52} radius="xl" color="brand">
            OK
          </ThemeIcon>
          <Title order={2} c="brand.9">
            Order Confirmed
          </Title>
          <Text c="dimmed" ta="center">
            Your purchase is confirmed.
          </Text>
        </Stack>
      </Paper>

      <SimpleGrid cols={{ base: 1, sm: 3 }} spacing="md">
        <DetailBlock label="Order Number" value={confirmation.orderNumber} />
        <DetailBlock label="Order Date" value={formatDateTime(confirmation.confirmedAt)} />
        <DetailBlock label="Status" value={confirmation.status} />
      </SimpleGrid>

      <Paper className="detail-section" radius="md" p="lg" withBorder>
        <Stack gap="xs">
          <Title order={3} size="h4" c="brand.9">
            Event Details
          </Title>
          <Text fw="bold" c="brand.9">
            {confirmation.event.title}
          </Text>
          <Text size="sm" c="dimmed">
            {formatDateTime(confirmation.event.startsAt)}
          </Text>
          <Text size="sm" c="dimmed">
            {confirmation.event.venueName}
          </Text>
          <Text size="sm" c="dimmed">
            {confirmation.event.address}, {confirmation.event.city}
          </Text>
        </Stack>
      </Paper>

      <Paper className="detail-section" radius="md" p="lg" withBorder>
        <Stack gap="md">
          <Title order={3} size="h4" c="brand.9">
            Ticket Details
          </Title>
          <Stack gap="xs">
            {confirmation.summary.items.map((item) => (
              <Box key={item.ticketTypeId} className="confirmation-line">
                <Group justify="space-between" gap="md" wrap="nowrap">
                  <Stack gap={2}>
                    <Text size="sm" fw="bold" c="brand.9">
                      {item.name}
                    </Text>
                    <Text size="xs" c="dimmed">
                      Quantity: {item.quantity}
                    </Text>
                  </Stack>
                  <Text size="sm" fw="bold" c="brand.9">
                    {formatMoney(item.lineTotal)}
                  </Text>
                </Group>
              </Box>
            ))}
          </Stack>
        </Stack>
      </Paper>

      <Paper className="detail-section" radius="md" p="lg" withBorder>
        <Stack gap="xs">
          <Title order={3} size="h4" c="brand.9">
            Payment Information
          </Title>
          <Text size="sm" c="dimmed">
            {confirmation.paymentMethod} {confirmation.cardLast4 ? `**** ${confirmation.cardLast4}` : ""}
          </Text>
        </Stack>
      </Paper>

      <Paper className="detail-section" radius="md" p="lg" withBorder>
        <Stack gap="sm">
          <Title order={3} size="h4" c="brand.9">
            Order Summary
          </Title>
          <Group justify="space-between" gap="md">
            <Text size="sm" c="dimmed">
              Subtotal
            </Text>
            <Text size="sm" fw="bold" c="brand.9">
              {formatMoney(confirmation.summary.subtotal)}
            </Text>
          </Group>
          {Number(confirmation.summary.discountAmount) > 0 && (
            <Group justify="space-between" gap="md">
              <Text size="sm" c="dimmed">
                Discount ({confirmation.summary.discountCode})
              </Text>
              <Text size="sm" fw="bold" c="brand.7">
                -{formatMoney(confirmation.summary.discountAmount)}
              </Text>
            </Group>
          )}
          <Divider color="brand.2" />
          <Group justify="space-between" gap="md">
            <Text fw="bold" c="brand.9">
              Total Paid
            </Text>
            <Text fw="bold" c="brand.9">
              {formatMoney(confirmation.summary.total)}
            </Text>
          </Group>
        </Stack>
      </Paper>

      <Group justify="flex-end">
        <Button variant="light" color="brand" onClick={() => navigate("/orders")}>
          Back to Orders
        </Button>
      </Group>
    </Stack>
  );
}

function DetailBlock({ label, value }) {
  return (
    <Paper className="detail-section" radius="md" p="md" withBorder>
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
