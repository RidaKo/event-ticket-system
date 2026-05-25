import { Alert, Group, Loader, Paper, Stack, Text, Title } from "@mantine/core";
import { useEffect, useMemo, useState } from "react";
import { quoteCheckout } from "../api/checkoutApi.js";
import { getEvent, getTicketTypes } from "../api/eventsApi.js";
import CheckoutStepLayout from "../components/CheckoutStepLayout.jsx";
import DiscountCodeInput from "../components/DiscountCodeInput.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import TicketQuantitySelector from "../components/TicketQuantitySelector.jsx";
import { useCheckout } from "../state/CheckoutContext.jsx";

export default function TicketSelectionPage({ eventId, navigate }) {
  const { setCheckoutDraft } = useCheckout();
  const [event, setEvent] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [quantities, setQuantities] = useState({});
  const [appliedDiscountCode, setAppliedDiscountCode] = useState("");
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [discountError, setDiscountError] = useState("");

  const selectedItems = useMemo(
    () =>
      Object.entries(quantities)
        .filter(([, quantity]) => quantity > 0)
        .map(([ticketTypeId, quantity]) => ({ ticketTypeId: Number(ticketTypeId), quantity })),
    [quantities]
  );
  const noTicketsAvailable = !loading && !error && tickets.length === 0;

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
        if (appliedDiscountCode) {
          setDiscountError("");
        }
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
      if (appliedDiscountCode) {
        setSummary(null);
      }
      setAppliedDiscountCode("");
      setDiscountError(err.message || "Discount code is invalid");
    }
  }

  function continueToAccount() {
    if (!summary || selectedItems.length === 0) {
      setError("Select at least one ticket");
      return;
    }

    setError("");
    setCheckoutDraft({
      eventId,
      event,
      items: selectedItems,
      discountCode: appliedDiscountCode || null,
      summary,
    });
    navigate(`/events/${eventId}/checkout/account`);
  }

  const sidebar = (
    <Stack gap="md">
      <OrderSummary
        summary={summary}
        actionLabel="Continue to Account"
        onAction={continueToAccount}
        actionDisabled={!summary || selectedItems.length === 0}
        footer={
          <Stack gap="md">
            <DiscountCodeInput
              value={appliedDiscountCode}
              appliedCode={appliedDiscountCode}
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
        {noTicketsAvailable && (
          <Alert color="yellow" variant="light">
            No tickets are available for this event yet.
          </Alert>
        )}

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
