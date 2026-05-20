import { Alert, Grid, Group, Loader, Paper, Stack, Text, TextInput, Title } from "@mantine/core";
import { useEffect, useState } from "react";
import { getOrder, submitPayment } from "../api/checkoutApi.js";
import CheckoutStepLayout from "../components/CheckoutStepLayout.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import PaymentMethodSelector from "../components/PaymentMethodSelector.jsx";
import { useCheckout } from "../state/CheckoutContext.jsx";

export default function PaymentPage({ orderNumber, navigate }) {
  const { getOrderToken } = useCheckout();
  const orderToken = getOrderToken(orderNumber);
  const [order, setOrder] = useState(null);
  const [methodType, setMethodType] = useState("CARD");
  const [cardNumber, setCardNumber] = useState("4242 4242 4242 4242");
  const [nameOnCard, setNameOnCard] = useState("");
  const [expiry, setExpiry] = useState("12/28");
  const [cvv, setCvv] = useState("123");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");

    getOrder(orderNumber, orderToken)
      .then((data) => {
        if (!active) {
          return;
        }
        setOrder(data);
        setNameOnCard(data.guestName || "");
      })
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [orderNumber, orderToken]);

  async function payNow(event) {
    event.preventDefault();
    if (methodType === "CARD" && cardNumber.replace(/\D/g, "").length < 12) {
      setError("Enter a valid card number");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const result = await submitPayment(
        orderNumber,
        {
          methodType,
          cardNumber: methodType === "CARD" ? cardNumber : "5555 5555 5555 4444",
        },
        orderToken
      );
      if (result.orderStatus === "CONFIRMED") {
        navigate(`/checkout/${orderNumber}/confirmation`);
        return;
      }
      setError("Payment failed");
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const sidebar = (
    <Stack gap="md">
      <OrderSummary
        summary={order?.summary}
        actionLabel="Pay Now"
        onAction={() => document.getElementById("paymentForm")?.requestSubmit()}
        actionDisabled={submitting || loading || !order}
      />
      {error && (
        <Alert color="red" variant="light">
          {error}
        </Alert>
      )}
    </Stack>
  );

  return (
    <CheckoutStepLayout title="Payment" sidebar={sidebar}>
      {loading && (
        <Group gap="sm">
          <Loader size="sm" color="brand" />
          <Text c="dimmed">Loading order...</Text>
        </Group>
      )}

      {order && (
        <form id="paymentForm" onSubmit={payNow}>
          <Stack gap="md">
            <Paper className="form-section" radius="md" p="lg" withBorder>
              <Stack gap="md">
                <Title order={3} size="h4" c="brand.9">
                  Payment Method
                </Title>
                <PaymentMethodSelector value={methodType} onChange={setMethodType} />
              </Stack>
            </Paper>

            {methodType === "CARD" && (
              <Paper className="form-section" radius="md" p="lg" withBorder>
                <Stack gap="md">
                  <Title order={3} size="h4" c="brand.9">
                    Card Details
                  </Title>
                  <TextInput
                    label="Card Number"
                    value={cardNumber}
                    onChange={(e) => setCardNumber(e.target.value)}
                    variant="filled"
                  />
                  <TextInput
                    label="Name on Card"
                    value={nameOnCard}
                    onChange={(e) => setNameOnCard(e.target.value)}
                    variant="filled"
                  />
                  <Grid gutter="sm">
                    <Grid.Col span={{ base: 12, sm: 6 }}>
                      <TextInput
                        label="Expiration Date"
                        value={expiry}
                        onChange={(e) => setExpiry(e.target.value)}
                        variant="filled"
                      />
                    </Grid.Col>
                    <Grid.Col span={{ base: 12, sm: 6 }}>
                      <TextInput
                        label="CVV"
                        value={cvv}
                        onChange={(e) => setCvv(e.target.value)}
                        variant="filled"
                      />
                    </Grid.Col>
                  </Grid>
                </Stack>
              </Paper>
            )}
          </Stack>
        </form>
      )}
    </CheckoutStepLayout>
  );
}
