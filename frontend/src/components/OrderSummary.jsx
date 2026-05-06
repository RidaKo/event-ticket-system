import { Box, Button, Divider, Group, Paper, Stack, Text, Title } from "@mantine/core";
import { formatMoney } from "../utils.js";

export default function OrderSummary({ summary, actionLabel, onAction, actionDisabled, footer }) {
  const empty = !summary || summary.items.length === 0;

  return (
    <Paper className="summary-card" radius="md" p="lg" withBorder>
      <Stack gap="md">
        <Title order={3} size="h4" c="brand.9">
          Order Summary
        </Title>

        {empty ? (
          <Text size="sm" c="dimmed">
            No tickets selected
          </Text>
        ) : (
          <>
            <Stack gap="xs">
              {summary.items.map((item) => (
                <Group className="summary-line" key={item.ticketTypeId} justify="space-between" gap="md" wrap="nowrap">
                  <Text size="sm" c="brand.9">
                    {item.name} x {item.quantity}
                  </Text>
                  <Text size="sm" fw="bold" c="brand.9">
                    {formatMoney(item.lineTotal)}
                  </Text>
                </Group>
              ))}
            </Stack>

            <Divider color="brand.2" />

            <Stack gap="xs">
              <Group justify="space-between" gap="md">
                <Text size="sm" c="dimmed">
                  Subtotal
                </Text>
                <Text size="sm" fw="bold" c="brand.9">
                  {formatMoney(summary.subtotal)}
                </Text>
              </Group>
              {Number(summary.discountAmount) > 0 && (
                <Group justify="space-between" gap="md">
                  <Text size="sm" c="dimmed">
                    Discount ({summary.discountCode})
                  </Text>
                  <Text size="sm" fw="bold" c="brand.7">
                    -{formatMoney(summary.discountAmount)}
                  </Text>
                </Group>
              )}
              <Group className="total-row" justify="space-between" gap="md">
                <Text fw="bold" c="brand.9">
                  Total
                </Text>
                <Text fw="bold" c="brand.9">
                  {formatMoney(summary.total)}
                </Text>
              </Group>
            </Stack>
          </>
        )}

        {actionLabel && (
          <Button fullWidth color="brand" onClick={onAction} disabled={actionDisabled}>
            {actionLabel}
          </Button>
        )}

        {footer && <Box>{footer}</Box>}
      </Stack>
    </Paper>
  );
}
