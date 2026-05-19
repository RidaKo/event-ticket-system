import { Button, Group, Stack, Text, TextInput } from "@mantine/core";
import { useState } from "react";

export default function DiscountCodeInput({ value, appliedCode, error, onApply, disabled }) {
  const [code, setCode] = useState(value || "");

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        onApply(code.trim());
      }}
    >
      <Stack gap="xs">
        <Text size="xs" fw="bold" c="dimmed" tt="uppercase">
          Promo Code
        </Text>
        <Group gap="xs" align="flex-end" wrap="nowrap">
          <TextInput
            className="discount-input"
            value={code}
            onChange={(event) => setCode(event.target.value)}
            placeholder="SAVE10"
            disabled={disabled}
            variant="filled"
          />
          <Button type="submit" variant="light" color="brand" disabled={disabled}>
            Apply
          </Button>
        </Group>
        {appliedCode && (
          <Text size="sm" c="brand.7">
            {appliedCode} applied
          </Text>
        )}
        {error && (
          <Text size="sm" c="red.7">
            {error}
          </Text>
        )}
      </Stack>
    </form>
  );
}
