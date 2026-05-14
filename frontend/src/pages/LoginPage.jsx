import {
  Alert,
  Button,
  Checkbox,
  Grid,
  Group,
  Paper,
  PasswordInput,
  Stack,
  Text,
  TextInput,
  ThemeIcon,
  Title,
} from "@mantine/core";
import { useState } from "react";

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

export default function LoginPage({ navigate }) {
  const [form, setForm] = useState({
    email: "",
    password: "",
    remember: true,
  });
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submitLogin(event) {
    event.preventDefault();
    setError("");
    setNotice("");

    if (!form.email.trim()) {
      setError("Email address is required");
      return;
    }

    if (!form.password) {
      setError("Password is required");
      return;
    }

    setNotice("Sign in is ready for the authentication API.");
  }

  return (
    <Grid className="auth-shell" gutter="lg" align="center">
      <Grid.Col span={{ base: 12, md: 7 }}>
        <Paper className="auth-card" radius="md" p="xl" withBorder>
          <form onSubmit={submitLogin}>
            <Stack gap="md">
              <Group className="auth-card-header" align="center" gap="sm" wrap="nowrap">
                <ThemeIcon className="auth-icon" variant="light" color="brand" radius="xl" size="lg">
                  <SignInIcon />
                </ThemeIcon>
                <Stack gap={2}>
                  <Title order={2} c="brand.9">
                    Sign in
                  </Title>
                  <Text size="sm" c="dimmed">
                    Use your account to manage orders and events.
                  </Text>
                </Stack>
              </Group>

              <TextInput
                label="Email Address"
                type="email"
                value={form.email}
                onChange={(event) => updateForm("email", event.target.value)}
              />
              <PasswordInput
                label="Password"
                value={form.password}
                onChange={(event) => updateForm("password", event.target.value)}
              />

              <Group justify="space-between" align="center" gap="sm">
                <Checkbox
                  checked={form.remember}
                  label="Remember me"
                  onChange={(event) => updateForm("remember", event.currentTarget.checked)}
                />
                <Button type="button" variant="subtle" color="brand" size="xs">
                  Forgot password
                </Button>
              </Group>

              {error && (
                <Alert color="red" variant="light">
                  {error}
                </Alert>
              )}
              {notice && (
                <Alert color="brand" variant="light">
                  {notice}
                </Alert>
              )}

              <Button type="submit" color="brand" fullWidth>
                Sign in
              </Button>
            </Stack>
          </form>
        </Paper>
      </Grid.Col>

      <Grid.Col span={{ base: 12, md: 5 }}>
        <Paper className="auth-secondary-card" radius="md" p="xl" withBorder>
          <Stack gap="md">
            <ThemeIcon className="auth-icon" variant="light" color="brand" radius="xl" size="lg">
              <UserPlusIcon />
            </ThemeIcon>
            <Stack gap="xs">
              <Title order={3} size="h4" c="brand.9">
                New to Event Ticket?
              </Title>
              <Text size="sm" c="dimmed">
                Create an account with your name, email, phone, and password.
              </Text>
            </Stack>
            <Button color="brand" variant="light" onClick={() => navigate("/signup")}>
              Create account
            </Button>
          </Stack>
        </Paper>
      </Grid.Col>
    </Grid>
  );
}
