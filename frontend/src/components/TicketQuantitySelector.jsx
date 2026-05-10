import { ActionIcon, Badge, Group, Paper, Stack, Text, ThemeIcon, Title } from "@mantine/core";
import { formatMoney } from "../utils.js";

export default function TicketQuantitySelector({ ticket, quantity, onChange }) {
  const soldOut = !ticket.salesEnabled || ticket.availableQuantity <= 0;

  return (
    <Paper className="ticket-card" radius="md" p="lg" withBorder data-disabled={soldOut || undefined}>
      <Group align="stretch" gap="lg" wrap="nowrap">
        <ThemeIcon
          className="ticket-icon"
          size={72}
          radius="md"
          variant="light"
          color={soldOut ? "gray" : "brand"}
        >
          T
        </ThemeIcon>

        <Stack className="ticket-body" gap="sm">
          <Group justify="space-between" align="flex-start" gap="md" wrap="nowrap">
            <BoxTitle ticket={ticket} soldOut={soldOut} />
            <Badge radius="sm" variant="light" color={soldOut ? "gray" : "brand"}>
              {soldOut ? "Sold out" : `${ticket.availableQuantity} left`}
            </Badge>
          </Group>

          <Text size="xl" fw="bold" c="brand.9">
            {formatMoney(ticket.price)}
          </Text>

          <Group className="quantity-control" gap={0} wrap="nowrap">
            <ActionIcon
              variant="default"
              radius="sm"
              size="lg"
              aria-label={`Decrease ${ticket.name} quantity`}
              onClick={() => onChange(Math.max(0, quantity - 1))}
              disabled={quantity === 0}
            >
              -
            </ActionIcon>
            <Text className="quantity-value" fw="bold" ta="center">
              {quantity}
            </Text>
            <ActionIcon
              variant="default"
              radius="sm"
              size="lg"
              aria-label={`Increase ${ticket.name} quantity`}
              onClick={() => onChange(quantity + 1)}
              disabled={soldOut || quantity >= ticket.availableQuantity}
            >
              +
            </ActionIcon>
          </Group>
        </Stack>
      </Group>
    </Paper>
  );
}

function BoxTitle({ ticket, soldOut }) {
  return (
    <Title order={3} size="h4" c={soldOut ? "dimmed" : "brand.9"} lineClamp={2}>
      {ticket.name}
    </Title>
  );
}
