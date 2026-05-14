import { Alert, Group, Loader, Paper, Stack, Text, Title } from "@mantine/core";
import { useEffect, useMemo, useState } from "react";
import { createOrder, quoteCheckout } from "../api/checkoutApi.js";
import { getEvent, getTicketTypes } from "../api/eventsApi.js";
import CheckoutStepLayout from "../components/CheckoutStepLayout.jsx";
import DiscountCodeInput from "../components/DiscountCodeInput.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import TicketQuantitySelector from "../components/TicketQuantitySelector.jsx";

const MVP_GUEST_EMAIL = "guest@event-ticket.local";

export default function TicketSelectionPage({ eventId, navigate }) {
  const [event, setEvent] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [quantities, setQuantities] = useState({});
  const [appliedDiscountCode, setAppliedDiscountCode] = useState("");
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [discountError, setDiscountError] = useState("");

  const selectedItems = useMemo(
    () =>
      Object.entries(quantities)
        .filter(([, quantity]) => quantity > 0)
        .map(([ticketTypeId, quantity]) => ({ ticketTypeId: Number(ticketTypeId), quantity })),
    [quantities]
  );

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");

    Promise.all([getEvent(eventId), getTicketTypes(eventId)])
      .then(([eventData, ticketData]) => {
        if (!active) {
          return;
        }
        setEvent(eventData);
        setTickets(ticketData);
      })
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [eventId]);

  useEffect(() => {
    if (selectedItems.length === 0) {
      setSummary(null);
      setDiscountError("");
      return;
    }

    let active = true;
    quoteCheckout({ eventId, items: selectedItems, discountCode: appliedDiscountCode || null })
      .then((quotedSummary) => {
        if (!active) {
          return;
        }
        setSummary(quotedSummary);
        setError("");
        setDiscountError("");
      })
      .catch((err) => {
        if (!active) {
          return;
        }
        setSummary(null);
        setError(err.message);
      });

    return () => {
      active = false;
    };
  }, [eventId, selectedItems, appliedDiscountCode]);

  function updateQuantity(ticketId, quantity) {
    setQuantities((current) => ({ ...current, [ticketId]: quantity }));
  }

  async function applyDiscountCode(code) {
    const nextCode = code.trim();
    setDiscountError("");

    if (selectedItems.length === 0) {
      setDiscountError("Select at least one ticket before applying a promo code.");
      return;
    }

    if (!nextCode) {
      setAppliedDiscountCode("");
      return;
    }

    try {
      setError("");
      const quotedSummary = await quoteCheckout({ eventId, items: selectedItems, discountCode: nextCode });
      setSummary(quotedSummary);
      setAppliedDiscountCode(quotedSummary.discountCode || nextCode);
      setError("");
      setDiscountError("");
    } catch (err) {
      setDiscountError(err.message || "Discount code is invalid");
    }
  }

  async function continueToPayment() {
    if (!summary || selectedItems.length === 0) {
      setError("Select at least one ticket");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const order = await createOrder({
        eventId,
        guestName: null,
        guestEmail: MVP_GUEST_EMAIL,
        discountCode: appliedDiscountCode || null,
        items: selectedItems,
      });
      navigate(`/checkout/${order.orderNumber}/payment`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const sidebar = (
    <Stack gap="md">
      <OrderSummary
        summary={summary}
        actionLabel="Continue to Payment"
        onAction={continueToPayment}
        actionDisabled={submitting || !summary || selectedItems.length === 0}
        footer={
          <Stack gap="md">
            <DiscountCodeInput
              value={appliedDiscountCode}
              appliedCode={summary?.discountCode}
              error={discountError}
              onApply={applyDiscountCode}
              disabled={selectedItems.length === 0}
            />
          </Stack>
        }
      />
      {error && (
        <Alert color="red" variant="light">
          {error}
        </Alert>
      )}
    </Stack>
  );

  return (
    <CheckoutStepLayout title="Ticket Selection" sidebar={sidebar}>
      {loading && (
        <Group gap="sm">
          <Loader size="sm" color="brand" />
          <Text c="dimmed">Loading tickets...</Text>
        </Group>
      )}

      {event && (
        <Paper className="event-strip" radius="md" p="md" withBorder>
          <Stack gap={2}>
            <Title order={3} size="h4" c="brand.9">
              {event.title}
            </Title>
            <Text size="sm" c="dimmed">
              {event.venueName}
            </Text>
          </Stack>
        </Paper>
      )}

      <Stack gap="sm">
        {tickets.map((ticket) => (
          <TicketQuantitySelector
            key={ticket.id}
            ticket={ticket}
            quantity={quantities[ticket.id] || 0}
            onChange={(quantity) => updateQuantity(ticket.id, quantity)}
          />
        ))}
      </Stack>
    </CheckoutStepLayout>
  );
}
