import {
  Alert,
  Button,
  Checkbox,
  Group,
  Paper,
  PasswordInput,
  Stack,
  TextInput,
  Title,
} from "@mantine/core";
import { useState } from "react";

const emptyLogin = {
  email: "",
  password: "",
  remember: true,
};

export default function LoginForm({
  initialValues = {},
  onSubmit,
  submitLabel = "Sign in",
  successMessage = "Sign in is ready for the authentication API.",
  sectionTitle,
  showRemember = true,
  showForgotPassword = true,
}) {
  const [form, setForm] = useState({ ...emptyLogin, ...initialValues });
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submitLogin(event) {
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

    setSubmitting(true);
    try {
      await onSubmit?.({
        ...form,
        email: form.email.trim(),
      });
      if (successMessage) {
        setNotice(successMessage);
      }
    } catch (err) {
      setError(err.message || "Unable to sign in");
    } finally {
      setSubmitting(false);
    }
  }

  const fields = (
    <Stack gap="md">
      {sectionTitle && (
        <Title order={3} size="h4" c="brand.9">
          {sectionTitle}
        </Title>
      )}
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

      {(showRemember || showForgotPassword) && (
        <Group justify="space-between" align="center" gap="sm">
          {showRemember && (
            <Checkbox
              checked={form.remember}
              label="Remember me"
              onChange={(event) => updateForm("remember", event.currentTarget.checked)}
            />
          )}
          {showForgotPassword && (
            <Button type="button" variant="subtle" color="brand" size="xs">
              Forgot password
            </Button>
          )}
        </Group>
      )}
    </Stack>
  );

  return (
    <form onSubmit={submitLogin}>
      <Stack gap="md">
        {sectionTitle ? (
          <Paper className="form-section account-form" radius="md" p="lg" withBorder>
            {fields}
          </Paper>
        ) : (
          fields
        )}

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

        <Button type="submit" color="brand" fullWidth loading={submitting}>
          {submitLabel}
        </Button>
      </Stack>
    </form>
  );
}
