import { Button, Group, Modal, Stack, Text } from "@mantine/core";

export default function ConflictDialog({ opened, onClose, onRefresh, onOverwrite }) {
  function handleRefresh() {
    onClose();
    onRefresh();
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
        <Group justify="flex-end" gap="sm">
          <Button variant="default" onClick={handleRefresh}>
            Refresh data
          </Button>
          <Button color="brand" onClick={handleOverwrite}>
            Force overwrite
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
}
