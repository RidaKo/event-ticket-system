import {
  Alert,
  Button,
  Checkbox,
  Grid,
  Group,
  Loader,
  Paper,
  PasswordInput,
  Radio,
  Stack,
  Text,
  TextInput,
  ThemeIcon,
  Title,
} from "@mantine/core";
import { useEffect, useState } from "react";
import { createOrder, quoteCheckout } from "../api/checkoutApi.js";
import CheckoutStepLayout from "../components/CheckoutStepLayout.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import { useCheckout } from "../state/CheckoutContext.jsx";

const checkoutEventId = 1;
const defaultCheckoutItems = [
  { ticketTypeId: 1, quantity: 1 },
  { ticketTypeId: 2, quantity: 2 },
  { ticketTypeId: 3, quantity: 1 },
];
const defaultDiscountCode = "SAVE10";
const previewSummary = {
  items: [
    { ticketTypeId: 1, name: "General Admission", quantity: 1, lineTotal: 39 },
    { ticketTypeId: 2, name: "VIP Ticket", quantity: 2, lineTotal: 300 },
    { ticketTypeId: 3, name: "Student Ticket", quantity: 1, lineTotal: 30 },
  ],
  subtotal: 369,
  discountCode: defaultDiscountCode,
  discountAmount: 36.9,
  total: 332.1,
};

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

function GuestIcon() {
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
      <rect x="4" y="6" width="16" height="12" rx="2" />
      <path d="m5.5 8 6.5 5 6.5-5" />
    </svg>
  );
}

function AccountChoice({ checked, icon, title, description, onClick }) {
  return (
    <Paper
      component="button"
      type="button"
      className="account-choice"
      data-selected={checked || undefined}
      radius="md"
      p="md"
      withBorder
      onClick={onClick}
    >
      <Group align="flex-start" gap="sm" wrap="nowrap">
        <Radio checked={checked} readOnly aria-hidden="true" tabIndex={-1} />
        <ThemeIcon className="account-choice-icon" variant="light" color="brand" radius="xl">
          {icon}
        </ThemeIcon>
        <Stack gap={2}>
          <Text size="sm" fw={700} c="brand.9">
            {title}
          </Text>
          <Text size="xs" c="dimmed">
            {description}
          </Text>
        </Stack>
      </Group>
    </Paper>
  );
}

export default function AccountPage({ navigate }) {
  const { guest, updateGuest } = useCheckout();
  const [accountMode, setAccountMode] = useState("create");
  const [form, setForm] = useState({
    fullName: guest.guestName || "John Doe",
    email: guest.guestEmail || "john.doe@email.com",
    phone: "(555) 123-4567",
    password: "password123",
    confirmPassword: "password123",
    marketing: true,
    terms: true,
  });
  const [summary, setSummary] = useState(previewSummary);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setSummaryLoading(true);

    quoteCheckout({
      eventId: checkoutEventId,
      items: defaultCheckoutItems,
      discountCode: defaultDiscountCode,
    })
      .then((quotedSummary) => {
        if (active) {
          setSummary(quotedSummary);
        }
      })
      .catch(() => {
        if (active) {
          setSummary(previewSummary);
        }
      })
      .finally(() => active && setSummaryLoading(false));

    return () => {
      active = false;
    };
  }, []);

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function continueToPayment(event) {
    event.preventDefault();

    if (!form.email.trim()) {
      setError("Email address is required");
      return;
    }

    if (accountMode === "create" && form.password !== form.confirmPassword) {
      setError("Passwords must match");
      return;
    }

    if (!form.terms) {
      setError("Accept the terms and privacy policy to continue");
      return;
    }

    const guestName = form.fullName.trim();
    const guestEmail = form.email.trim();
    updateGuest({ guestName, guestEmail, phone: form.phone.trim() });

    setSubmitting(true);
    setError("");

    try {
      const order = await createOrder({
        eventId: checkoutEventId,
        guestName: guestName || null,
        guestEmail,
        discountCode: defaultDiscountCode,
        items: defaultCheckoutItems,
      });
      navigate(`/checkout/${order.orderNumber}/payment`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const createAccount = accountMode === "create";

  const sidebar = (
    <Stack gap="md">
      <OrderSummary summary={summary} />
      {summaryLoading && (
        <Group gap="xs" justify="center">
          <Loader size="xs" color="brand" />
          <Text size="xs" c="dimmed">
            Updating summary
          </Text>
        </Group>
      )}
    </Stack>
  );

  return (
    <CheckoutStepLayout title="Account" sidebar={sidebar}>
      <form onSubmit={continueToPayment}>
        <Stack gap="md">
          <Grid gutter="md">
            <Grid.Col span={{ base: 12, sm: 6 }}>
              <AccountChoice
                checked={createAccount}
                icon={<UserPlusIcon />}
                title="Create Account"
                description="Save your info for faster checkout"
                onClick={() => setAccountMode("create")}
              />
            </Grid.Col>
            <Grid.Col span={{ base: 12, sm: 6 }}>
              <AccountChoice
                checked={!createAccount}
                icon={<GuestIcon />}
                title="Continue as Guest"
                description="Quick checkout without an account"
                onClick={() => setAccountMode("guest")}
              />
            </Grid.Col>
          </Grid>

          <Paper className="form-section account-form" radius="md" p="lg" withBorder>
            <Stack gap="sm">
              <Title order={3} size="h4" c="brand.9">
                {createAccount ? "Create Your Account" : "Guest Checkout"}
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

              {createAccount && (
                <>
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
                </>
              )}

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

          <Button type="submit" color="brand" fullWidth loading={submitting}>
            Continue to Payment
          </Button>
        </Stack>
      </form>
    </CheckoutStepLayout>
  );
}
