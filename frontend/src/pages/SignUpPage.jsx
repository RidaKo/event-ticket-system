import {
  Button,
  Grid,
  Group,
  Paper,
  Stack,
  Text,
  ThemeIcon,
  Title,
} from "@mantine/core";
import RegistrationForm from "../components/RegistrationForm.jsx";

function UserPlusIcon() {
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

function SignInIcon() {
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

export default function SignUpPage({ navigate }) {
  return (
    <Grid className="auth-shell" gutter="lg" align="flex-start">
      <Grid.Col span={{ base: 12, md: 7 }}>
        <Stack gap="md">
          <Group className="auth-card-header" align="center" gap="sm" wrap="nowrap">
            <ThemeIcon className="auth-icon" variant="light" color="brand" radius="xl" size="lg">
              <UserPlusIcon />
            </ThemeIcon>
            <Stack gap={2}>
              <Title order={2} c="brand.9">
                Create Account
              </Title>
              <Text size="sm" c="dimmed">
                Save your info for faster checkout and event updates.
              </Text>
            </Stack>
          </Group>

          <RegistrationForm />
        </Stack>
      </Grid.Col>

      <Grid.Col span={{ base: 12, md: 5 }}>
        <Paper className="auth-secondary-card" radius="md" p="xl" withBorder>
          <Stack gap="md">
            <ThemeIcon className="auth-icon" variant="light" color="brand" radius="xl" size="lg">
              <SignInIcon />
            </ThemeIcon>
            <Stack gap="xs">
              <Title order={3} size="h4" c="brand.9">
                Already have an account?
              </Title>
              <Text size="sm" c="dimmed">
                Sign in with your email and password.
              </Text>
            </Stack>
            <Button color="brand" variant="light" onClick={() => navigate("/signin")}>
              Sign in
            </Button>
          </Stack>
        </Paper>
      </Grid.Col>
    </Grid>
  );
}
