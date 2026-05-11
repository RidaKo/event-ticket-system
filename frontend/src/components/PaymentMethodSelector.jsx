import { Paper, Radio, SimpleGrid, Stack, Text } from "@mantine/core";

const methods = [
  {
    value: "CARD",
    title: "Credit / Debit Card",
    description: "Visa, Mastercard, Amex",
  },
  {
    value: "WALLET",
    title: "Apple Pay / Google Pay",
    description: "Simulated wallet payment",
  },
];

export default function PaymentMethodSelector({ value, onChange }) {
  return (
    <Radio.Group value={value} onChange={onChange}>
      <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="sm">
        {methods.map((method) => (
          <Paper
            key={method.value}
            component="label"
            className="method-card"
            data-selected={value === method.value || undefined}
            radius="md"
            p="md"
            withBorder
          >
            <Radio value={method.value} />
            <Stack gap={2}>
              <Text size="sm" fw="bold" c="brand.9">
                {method.title}
              </Text>
              <Text size="xs" c="dimmed">
                {method.description}
              </Text>
            </Stack>
          </Paper>
        ))}
      </SimpleGrid>
    </Radio.Group>
  );
}
