import { Button, Group, Modal, Stack, Text } from "@mantine/core";

function formatValue(value) {
  if (Array.isArray(value)) {
    return value.length ? value.join(", ") : "None";
  }
  if (value == null || value === "") {
    return "None";
  }
  return String(value);
}

export default function ConflictDialog({
  opened,
  onClose,
  onRefresh,
  onKeepEditing,
  onOverwrite,
  rows = [],
}) {
  function handleRefresh() {
    onClose();
    onRefresh();
  }

  function handleKeepEditing() {
    onClose();
    onKeepEditing?.();
  }

  function handleOverwrite() {
    onClose();
    onOverwrite();
  }

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title="Edit conflict"
      size="sm"
      centered
    >
      <Stack gap="md">
        <Text size="sm">
          Someone else modified this record while you were editing. What would you like to do?
        </Text>
        {rows.length > 0 && (
          <Stack gap="xs">
            {rows.map((row) => (
              <Stack key={row.label} gap={2}>
                <Text size="xs" fw={700} c="dimmed" tt="uppercase">
                  {row.label}
                </Text>
                <Text size="sm">
                  Current: {formatValue(row.current)}
                </Text>
                <Text size="sm">
                  Yours: {formatValue(row.attempted)}
                </Text>
              </Stack>
            ))}
          </Stack>
        )}
        <Group justify="flex-end" gap="sm">
          <Button variant="default" onClick={handleRefresh}>
            Refresh data
          </Button>
          {onKeepEditing && (
            <Button variant="light" color="brand" onClick={handleKeepEditing}>
              Keep editing
            </Button>
          )}
          <Button color="brand" onClick={handleOverwrite}>
            Force overwrite
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
}
