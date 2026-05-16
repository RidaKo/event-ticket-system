import { Button, Group, Paper, Stack, Text, ThemeIcon, Title } from "@mantine/core";
import LoginForm from "./LoginForm.jsx";

export function SignInIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      width="1em"
      height="1em"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M15 3h3.5A1.5 1.5 0 0 1 20 4.5v15a1.5 1.5 0 0 1-1.5 1.5H15" />
      <path d="M10 17l5-5-5-5" />
      <path d="M15 12H3" />
    </svg>
  );
}

export function UserPlusIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      width="1em"
      height="1em"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <circle cx="10" cy="8" r="3.5" />
      <path d="M3.5 20c1.1-3.3 3.5-5 6.5-5 1.5 0 2.8.4 3.9 1.2" />
      <path d="M18 14v6" />
      <path d="M15 17h6" />
    </svg>
  );
}

export function MailIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      width="1em"
      height="1em"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M4 6h16v12H4z" />
      <path d="m4 7 8 6 8-6" />
    </svg>
  );
}

export function AuthHeader({ description, icon, title }) {
  return (
    <Group className="auth-card-header" align="center" gap="sm" wrap="nowrap">
      <ThemeIcon className="auth-icon" variant="light" color="brand" radius="xl" size="lg">
        {icon}
      </ThemeIcon>
      <Stack gap={2}>
        <Title order={2} c="brand.9">
          {title}
        </Title>
        <Text size="sm" c="dimmed">
          {description}
        </Text>
      </Stack>
    </Group>
  );
}

export function AuthSwitchCard({ actionLabel, description, icon, onAction, title }) {
  return (
    <Paper className="auth-secondary-card" radius="md" p="xl" withBorder>
      <Stack gap="md">
        <ThemeIcon className="auth-icon" variant="light" color="brand" radius="xl" size="lg">
          {icon}
        </ThemeIcon>
        <Stack gap="xs">
          <Title order={3} size="h4" c="brand.9">
            {title}
          </Title>
          <Text size="sm" c="dimmed">
            {description}
          </Text>
        </Stack>
        <Button color="brand" variant="light" onClick={onAction}>
          {actionLabel}
        </Button>
      </Stack>
    </Paper>
  );
}

export function SignInFormSection({ title = "Sign in to Your Account", ...loginProps }) {
  return <LoginForm sectionTitle={title} {...loginProps} />;
}
