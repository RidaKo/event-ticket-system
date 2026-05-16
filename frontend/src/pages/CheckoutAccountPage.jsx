import {
  Alert,
  Box,
  Button,
  Checkbox,
  Group,
  Paper,
  SimpleGrid,
  Stack,
  Text,
  TextInput,
  ThemeIcon,
  Title,
} from "@mantine/core";
import { useState } from "react";
import { createOrder } from "../api/checkoutApi.js";
import { MailIcon, SignInFormSection, SignInIcon, UserPlusIcon } from "../components/AuthShared.jsx";
import CheckoutStepLayout from "../components/CheckoutStepLayout.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import RegistrationForm from "../components/RegistrationForm.jsx";
import { useCheckout } from "../state/CheckoutContext.jsx";

function AccountChoice({ description, icon, label, onClick, selected }) {
  return (
    <Paper
      component="button"
      type="button"
      className="account-choice"
      radius="md"
      p="md"
      withBorder
      data-selected={selected || undefined}
      onClick={onClick}
    >
      <Group gap="sm" align="flex-start" wrap="nowrap">
        <Box className="account-choice-radio" data-selected={selected || undefined} aria-hidden="true" />
        <ThemeIcon className="account-choice-icon" variant="subtle" color="brand" radius="xl" size="sm">
          {icon}
        </ThemeIcon>
        <Stack gap={2}>
          <Text size="sm" fw="bold" c="brand.9">
            {label}
          </Text>
          <Text size="xs" c="dimmed">
            {description}
          </Text>
        </Stack>
      </Group>
    </Paper>
  );
}

export default function CheckoutAccountPage({ eventId, navigate }) {
  const { checkoutDraft, guest, updateGuest } = useCheckout();
  const [mode, setMode] = useState("create");
  const [guestForm, setGuestForm] = useState({
    email: guest.guestEmail || "",
    phone: guest.guestPhone || "",
    marketing: true,
  });
  const [guestError, setGuestError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const hasCheckoutDraft =
    checkoutDraft?.eventId === eventId &&
    checkoutDraft?.summary &&
    Array.isArray(checkoutDraft.items) &&
    checkoutDraft.items.length > 0;

  const sidebar = <OrderSummary summary={hasCheckoutDraft ? checkoutDraft.summary : null} />;

  if (!hasCheckoutDraft) {
    return (
      <CheckoutStepLayout title="Account" sidebar={sidebar}>
        <Alert color="brand" variant="light">
          Select tickets before choosing an account option.
        </Alert>
        <Group>
          <Button color="brand" onClick={() => navigate(`/events/${eventId}/checkout/tickets`)}>
            Back to Tickets
          </Button>
        </Group>
      </CheckoutStepLayout>
    );
  }

  function updateGuestForm(field, value) {
    setGuestForm((current) => ({ ...current, [field]: value }));
  }

  async function createPendingOrder(account) {
    setSubmitting(true);
    try {
      updateGuest({
        guestName: account.guestName || "",
        guestEmail: account.guestEmail,
        guestPhone: account.guestPhone || "",
        accountMode: account.accountMode,
      });

      const order = await createOrder({
        eventId: checkoutDraft.eventId,
        guestName: account.guestName || null,
        guestEmail: account.guestEmail,
        discountCode: checkoutDraft.discountCode || null,
        items: checkoutDraft.items,
      });

      navigate(`/checkout/${order.orderNumber}/payment`);
    } finally {
      setSubmitting(false);
    }
  }

  async function continueAsGuest(event) {
    event.preventDefault();
    setGuestError("");

    if (!guestForm.email.trim()) {
      setGuestError("Email address is required");
      return;
    }

    try {
      await createPendingOrder({
        guestName: "",
        guestEmail: guestForm.email.trim(),
        guestPhone: guestForm.phone.trim(),
        accountMode: "guest",
      });
    } catch (err) {
      setGuestError(err.message || "Unable to continue as guest");
    }
  }

  return (
    <CheckoutStepLayout title="Account" sidebar={sidebar}>
      <Stack gap="md">
        <SimpleGrid cols={{ base: 1, sm: 3 }} spacing="sm">
          <AccountChoice
            label="Sign in"
            description="Use your existing account"
            icon={<SignInIcon />}
            selected={mode === "signin"}
            onClick={() => setMode("signin")}
          />
          <AccountChoice
            label="Create Account"
            description="Save your info for faster checkout"
            icon={<UserPlusIcon />}
            selected={mode === "create"}
            onClick={() => setMode("create")}
          />
          <AccountChoice
            label="Continue as Guest"
            description="Quick checkout without an account"
            icon={<MailIcon />}
            selected={mode === "guest"}
            onClick={() => setMode("guest")}
          />
        </SimpleGrid>

        {checkoutDraft.event?.title && (
          <Paper className="event-strip" radius="md" p="md" withBorder>
            <Stack gap={2}>
              <Title order={3} size="h4" c="brand.9">
                {checkoutDraft.event.title}
              </Title>
              <Text size="sm" c="dimmed">
                {checkoutDraft.event.venueName || checkoutDraft.event.venue?.name}
              </Text>
            </Stack>
          </Paper>
        )}

        {mode === "signin" && (
          <SignInFormSection
            title="Sign in to Continue"
            initialValues={{ email: guest.guestEmail || "" }}
            submitLabel="Sign in and Continue"
            successMessage=""
            onSubmit={(values) =>
              createPendingOrder({
                guestName: "",
                guestEmail: values.email,
                guestPhone: "",
                accountMode: "signin",
              })
            }
          />
        )}

        {mode === "create" && (
          <RegistrationForm
            initialValues={{ email: guest.guestEmail || "", phone: guest.guestPhone || "" }}
            submitLabel="Create Account and Continue"
            successMessage=""
            onSubmit={(values) =>
              createPendingOrder({
                guestName: values.fullName,
                guestEmail: values.email,
                guestPhone: values.phone,
                accountMode: "created",
              })
            }
          />
        )}

        {mode === "guest" && (
          <form onSubmit={continueAsGuest}>
            <Stack gap="md">
              <Paper className="form-section account-form" radius="md" p="lg" withBorder>
                <Stack gap="sm">
                  <Title order={3} size="h4" c="brand.9">
                    Guest Checkout
                  </Title>
                  <TextInput
                    label="Email Address"
                    description="Order confirmation will be sent to this email"
                    type="email"
                    value={guestForm.email}
                    onChange={(event) => updateGuestForm("email", event.target.value)}
                  />
                  <TextInput
                    label="Phone Number (Optional)"
                    description="For order updates and notifications"
                    value={guestForm.phone}
                    onChange={(event) => updateGuestForm("phone", event.target.value)}
                  />
                  <Checkbox
                    mt="md"
                    checked={guestForm.marketing}
                    label="Send me email updates about events and special offers"
                    onChange={(event) => updateGuestForm("marketing", event.currentTarget.checked)}
                  />
                </Stack>
              </Paper>

              {guestError && (
                <Alert color="red" variant="light">
                  {guestError}
                </Alert>
              )}

              <Button type="submit" color="brand" fullWidth loading={submitting}>
                Continue to Payment
              </Button>
            </Stack>
          </form>
        )}
      </Stack>
    </CheckoutStepLayout>
  );
}
