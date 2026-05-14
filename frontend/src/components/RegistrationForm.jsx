import {
  Alert,
  Button,
  Checkbox,
  Paper,
  PasswordInput,
  Stack,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import { useState } from "react";

const emptyRegistration = {
  fullName: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
  marketing: true,
  terms: false,
};

export default function RegistrationForm({
  initialValues = {},
  onSubmit,
  submitLabel = "Create account",
  successMessage = "Account details are ready for the registration API.",
}) {
  const [form, setForm] = useState({ ...emptyRegistration, ...initialValues });
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submitRegistration(event) {
    event.preventDefault();
    setError("");
    setNotice("");

    if (!form.fullName.trim()) {
      setError("Full name is required");
      return;
    }

    if (!form.email.trim()) {
      setError("Email address is required");
      return;
    }

    if (form.password.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError("Passwords must match");
      return;
    }

    if (!form.terms) {
      setError("Accept the terms and privacy policy to continue");
      return;
    }

    onSubmit?.({
      ...form,
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
    });
    setNotice(successMessage);
  }

  return (
    <form onSubmit={submitRegistration}>
      <Stack gap="md">
        <Paper className="form-section account-form" radius="md" p="lg" withBorder>
          <Stack gap="sm">
            <Title order={3} size="h4" c="brand.9">
              Create Your Account
            </Title>

            <TextInput
              label="Full Name"
              value={form.fullName}
              onChange={(event) => updateForm("fullName", event.target.value)}
            />
            <TextInput
              label="Email Address"
              type="email"
              value={form.email}
              onChange={(event) => updateForm("email", event.target.value)}
            />
            <TextInput
              label="Phone Number"
              value={form.phone}
              onChange={(event) => updateForm("phone", event.target.value)}
            />
            <PasswordInput
              label="Password"
              description="Must be at least 8 characters"
              value={form.password}
              onChange={(event) => updateForm("password", event.target.value)}
            />
            <PasswordInput
              label="Confirm Password"
              value={form.confirmPassword}
              onChange={(event) => updateForm("confirmPassword", event.target.value)}
            />

            <Stack gap="xs" mt="xs">
              <Checkbox
                checked={form.marketing}
                label="Send me email updates about events and special offers"
                onChange={(event) => updateForm("marketing", event.currentTarget.checked)}
              />
              <Checkbox
                checked={form.terms}
                label="I agree to the Terms of Service and Privacy Policy"
                onChange={(event) => updateForm("terms", event.currentTarget.checked)}
              />
            </Stack>
          </Stack>
        </Paper>

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
          {submitLabel}
        </Button>
      </Stack>
    </form>
  );
}
