import { useEffect, useState } from "react";
import {
  ActionIcon,
  AspectRatio,
  Badge,
  Box,
  Button,
  Card,
  Container,
  Divider,
  Grid,
  Group,
  Image,
  Paper,
  Pill,
  SimpleGrid,
  Stack,
  Tabs,
  Text,
  TextInput,
  ThemeIcon,
  Title,
} from "@mantine/core";
import ticketLogo from "./assets/ticket_small.png";
import TicketSelectionPage from "./pages/TicketSelectionPage.jsx";
import PaymentPage from "./pages/PaymentPage.jsx";
import ConfirmationPage from "./pages/ConfirmationPage.jsx";
import { getCatalog } from "./api/catalog.js";
import { getRecommendedEvents } from "./api/recommendations.js";
import { CheckoutProvider } from "./state/CheckoutContext.jsx";
import { createEmptyFilters, hasActiveFilters, normalizeFilters } from "./lib/catalog.js";
import { dateRangeToFilters, filtersToDateRange } from "./lib/dateFilters.js";
import { mapRecommendedEvent } from "./lib/recommendations.js";
import { DatePickerInput } from "@mantine/dates";

const checkoutEventId = 1;
const RECOMMENDED_LIMIT = 6;
const TOTAL_FETCH_LIMIT = 20;

function readRoute() {
  const path = window.location.pathname;
  const payment = path.match(/^\/checkout\/([^/]+)\/payment$/);
  if (payment) {
    return { name: "payment", orderNumber: payment[1] };
  }

  const confirmation = path.match(/^\/checkout\/([^/]+)\/confirmation$/);
  if (confirmation) {
    return { name: "confirmation", orderNumber: confirmation[1] };
  }

  const tickets = path.match(/^\/events\/(\d+)\/checkout\/tickets$/);
  if (tickets) {
    return { name: "tickets", eventId: Number(tickets[1]) };
  }

  if (path === "/orders") {
    return { name: "orders" };
  }

  return { name: "browse" };
}

function routeToTab(routeName) {
  return routeName === "browse" ? "browse" : "orders";
}

function TicketLogo() {
  return (
    <ThemeIcon className="logo-badge" size="xl" radius="md" variant="light" color="brand">
      <Image src={ticketLogo} alt="Event Ticket logo" className="logo-image" />
    </ThemeIcon>
  );
}

function BookmarkIcon() {
  return (
    <svg
      className="bookmark-icon"
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
      <path d="M6 4.5h12a1 1 0 0 1 1 1V21l-7-4-7 4V5.5a1 1 0 0 1 1-1z" />
    </svg>
  );
}

function ProfileIcon() {
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
      <circle cx="12" cy="8" r="3.5" />
      <path d="M5 20c1.2-3.2 3.8-5 7-5s5.8 1.8 7 5" />
    </svg>
  );
}

function EventCard({ event }) {
  return (
    <Card className="event-card" radius="md" padding="md" withBorder>
      <Card.Section inheritPadding pt="md">
        <AspectRatio ratio={16 / 9}>
          <Box className="media-placeholder">
            <Text size="xs" fw="bold" tt="uppercase" c="brand.5">
              Image
            </Text>
          </Box>
        </AspectRatio>
      </Card.Section>

      <Stack gap="xs" mt="md" className="event-card-body">
        <Group justify="space-between" align="flex-start" gap="xs" wrap="nowrap">
          <Title order={3} size="h4" c="brand.9" lineClamp={2}>
            {event.title}
          </Title>
          <ActionIcon
            variant="subtle"
            color="brand"
            radius="xl"
            aria-label={`Bookmark ${event.title}`}
          >
            <BookmarkIcon />
          </ActionIcon>
        </Group>

        <Text size="sm" fw="bold" c="brand.7">
          {event.date}
        </Text>
        <Text size="sm" c="dimmed" lineClamp={1}>
          {event.venue}
        </Text>

        <Group gap="xs" mt="auto">
          {(event.tags?.length ? event.tags : [event.tag]).map((label) => (
            <Badge key={`${event.id}-${label}`} radius="sm" variant="light" color="brand">
              {label}
            </Badge>
          ))}
          <Badge radius="sm" variant="light" color="gray">
            {event.category}
          </Badge>
        </Group>
      </Stack>
    </Card>
  );
}

function BrowseCard({ event }) {
  return (
    <Paper className="browse-card" radius="md" p="sm" withBorder>
      <Grid gutter="md" align="center">
        <Grid.Col span={{ base: 12, xs: 4, sm: 3 }}>
          <AspectRatio ratio={4 / 3}>
            <Box className="media-placeholder">
              <Text size="xs" fw="bold" tt="uppercase" c="brand.5">
                Image
              </Text>
            </Box>
          </AspectRatio>
        </Grid.Col>
        <Grid.Col span={{ base: 12, xs: 8, sm: 9 }}>
          <Stack gap="xs">
            <Group justify="space-between" align="flex-start" gap="xs" wrap="nowrap">
              <Box className="browse-copy">
                <Title order={3} size="h4" c="brand.9" lineClamp={2}>
                  {event.title}
                </Title>
                <Text size="sm" fw="bold" c="brand.7">
                  {event.date}
                </Text>
                <Text size="sm" c="dimmed" lineClamp={1}>
                  {event.venue}
                </Text>
              </Box>
              <ActionIcon
                variant="subtle"
                color="brand"
                radius="xl"
                aria-label={`Bookmark ${event.title}`}
              >
                <BookmarkIcon />
              </ActionIcon>
            </Group>
            <Group gap="xs">
              {(event.tags?.length ? event.tags : [event.tag]).map((label) => (
                <Badge key={`${event.id}-${label}`} radius="sm" variant="light" color="brand">
                  {label}
                </Badge>
              ))}
            </Group>
          </Stack>
        </Grid.Col>
      </Grid>
    </Paper>
  );
}

function FilterPanel({ draft, onDraftChange, onApply, onReset, catalog, catalogLoading, catalogError }) {
  function toggleInArray(key, item) {
    onDraftChange((current) => {
      const exists = current[key].includes(item);
      return {
        ...current,
        [key]: exists ? current[key].filter((value) => value !== item) : [...current[key], item],
      };
    });
  }

  function setField(key, value) {
    onDraftChange((current) => ({ ...current, [key]: value }));
  }

  return (
    <Paper className="filter-card" radius="md" withBorder>
      <Box>
        <Box px="lg" py="md">
          <Text fw="bold" size="sm" c="brand.9">
            Filter by Preferences
          </Text>
        </Box>
        <Divider color="brand.2" />
        <Box px="lg" py="xl" className="filter-body">
          <Stack gap="xs">
            <Text className="filter-label" size="xs" fw="bold" c="dimmed" tt="uppercase">
              Category
            </Text>
            <Group gap="xs">
              {(catalog.categories ?? []).map((item) => (
                <Pill
                  key={item.value}
                  size="sm"
                  style={{ cursor: "pointer" }}
                  onClick={() => toggleInArray("categories", item.value)}
                  bg={draft.categories.includes(item.value) ? "var(--mantine-color-brand-1)" : undefined}
                >
                  {item.label}
                </Pill>
              ))}
            </Group>
            {catalogLoading && (
              <Text size="xs" c="dimmed">
                Loading categories...
              </Text>
            )}
            {catalogError && (
              <Text size="xs" c="red">
                Could not load categories.
              </Text>
            )}
          </Stack>

          <Stack gap="xs">
            <Text className="filter-label" size="xs" fw="bold" c="dimmed" tt="uppercase">
              Interest Tags
            </Text>
            <Group gap="xs">
              {(catalog.tags ?? []).map((item) => (
                <Pill
                  key={item.slug}
                  size="sm"
                  style={{ cursor: "pointer" }}
                  onClick={() => toggleInArray("tags", item.slug)}
                  bg={draft.tags.includes(item.slug) ? "var(--mantine-color-brand-1)" : undefined}
                >
                  {item.label}
                </Pill>
              ))}
            </Group>
            {catalogLoading && (
              <Text size="xs" c="dimmed">
                Loading tags...
              </Text>
            )}
            {catalogError && (
              <Text size="xs" c="red">
                Could not load tags.
              </Text>
            )}
          </Stack>

          <Stack gap="xs" className="filter-date-range">
            <DatePickerInput
              type="range"
              label="Select event date"
              placeholder="Select event date"
              value={filtersToDateRange(draft)}
              onChange={(range) =>
                onDraftChange((current) => ({
                  ...current,
                  ...dateRangeToFilters(range),
                }))
              }
              valueFormat="MMM D, YYYY"
              numberOfColumns={1}
              clearable
              allowSingleDateInRange
              popoverProps={{ withinPortal: true, classNames: { dropdown: "filter-date-dropdown" } }}
              classNames={{ input: "filter-date-input" }}
              styles={{
                label: {
                  fontSize: "var(--mantine-font-size-xs)",
                  fontWeight: 700,
                  textTransform: "uppercase",
                  color: "var(--mantine-color-dimmed)",
                },
              }}
            />
          </Stack>

          <Stack gap="xs">
            <Text className="filter-label" size="xs" fw="bold" c="dimmed" tt="uppercase">
              Location
            </Text>
            <TextInput
              value={draft.location}
              onChange={(event) => setField("location", event.currentTarget.value)}
              placeholder="City or venue"
              variant="filled"
            />
          </Stack>

          <Divider color="brand.1" />

          <SimpleGrid cols={{ base: 1, xs: 2 }} spacing="sm">
            <Button variant="light" color="gray" onClick={onReset}>
              Reset
            </Button>
            <Button color="brand" onClick={onApply}>
              Apply
            </Button>
          </SimpleGrid>
        </Box>
      </Box>
    </Paper>
  );
}

function BrowsePage() {
  const [draftFilters, setDraftFilters] = useState(createEmptyFilters);
  const [appliedFilters, setAppliedFilters] = useState(createEmptyFilters);
  const [catalog, setCatalog] = useState({ categories: [], tags: [] });
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState(false);
  const [eventItems, setEventItems] = useState([]);
  const [recommendationsLoading, setRecommendationsLoading] = useState(true);
  const [recommendationsError, setRecommendationsError] = useState(false);
  const [fallbackUsed, setFallbackUsed] = useState(false);

  const recommendedItems = eventItems.slice(0, RECOMMENDED_LIMIT);
  const browseItems = eventItems.slice(RECOMMENDED_LIMIT);
  const showBrowseAllAboveHint =
    !recommendationsLoading &&
    !recommendationsError &&
    browseItems.length === 0 &&
    recommendedItems.length > 0;

  useEffect(() => {
    let cancelled = false;

    async function loadCatalog() {
      setCatalogLoading(true);
      setCatalogError(false);
      try {
        const data = await getCatalog();
        if (!cancelled) {
          setCatalog(data);
        }
      } catch {
        if (!cancelled) {
          setCatalogError(true);
        }
      } finally {
        if (!cancelled) {
          setCatalogLoading(false);
        }
      }
    }

    loadCatalog();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function loadRecommendations() {
      setRecommendationsLoading(true);
      setRecommendationsError(false);
      try {
        const response = await getRecommendedEvents({
          ...appliedFilters,
          limit: TOTAL_FETCH_LIMIT,
        });
        if (!cancelled) {
          setEventItems((response.items ?? []).map(mapRecommendedEvent));
          setFallbackUsed(Boolean(response.fallbackUsed));
        }
      } catch {
        if (!cancelled) {
          setRecommendationsError(true);
          setEventItems([]);
        }
      } finally {
        if (!cancelled) {
          setRecommendationsLoading(false);
        }
      }
    }

    loadRecommendations();
    return () => {
      cancelled = true;
    };
  }, [appliedFilters]);

  function handleApplyFilters() {
    const normalized = normalizeFilters(draftFilters);
    setDraftFilters(normalized);
    setAppliedFilters(normalized);
  }

  function handleResetFilters() {
    const empty = createEmptyFilters();
    setDraftFilters(empty);
    setAppliedFilters(empty);
  }

  return (
    <Grid gutter="lg" align="flex-start">
      <Grid.Col span={{ base: 12, md: 4, lg: 3 }}>
        <FilterPanel
          draft={draftFilters}
          onDraftChange={setDraftFilters}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
          catalog={catalog}
          catalogLoading={catalogLoading}
          catalogError={catalogError}
        />
      </Grid.Col>

      <Grid.Col span={{ base: 12, md: 8, lg: 9 }}>
        <Stack gap="xl">
          <section>
            <Group justify="space-between" align="baseline" gap="md" mb="md">
              <Title order={2} c="brand.9">
                Recommended for you
              </Title>
              {!hasActiveFilters(appliedFilters) && (
                <Text c="dimmed" size="sm">
                  {fallbackUsed ? "Showing popular upcoming events" : "Based on your preferences"}
                </Text>
              )}
            </Group>

            {recommendationsLoading && (
              <Text c="dimmed" size="sm">
                Loading recommendations...
              </Text>
            )}

            {recommendationsError && !recommendationsLoading && (
              <Text c="red" size="sm">
                Could not load recommendations. Please try again.
              </Text>
            )}

            {!recommendationsLoading && !recommendationsError && recommendedItems.length === 0 && (
              <Text c="dimmed" size="sm">
                No matches for the selected filters.
              </Text>
            )}

            {!recommendationsLoading && !recommendationsError && recommendedItems.length > 0 && (
              <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }} spacing="md">
                {recommendedItems.map((item) => (
                  <EventCard key={item.id} event={item} />
                ))}
              </SimpleGrid>
            )}
          </section>

          <section>
            <Group justify="space-between" align="baseline" gap="md" mb="md">
              <Title order={2} c="brand.9">
                Browse Events
              </Title>
            </Group>

            {showBrowseAllAboveHint ? (
              <Text c="dimmed" size="sm" ta="center">
                All matching events are shown above.
              </Text>
            ) : (
              <Stack gap="sm">
                {recommendationsLoading && (
                  <Text c="dimmed" size="sm">
                    Loading events...
                  </Text>
                )}

                {!recommendationsLoading && !recommendationsError && browseItems.length === 0 && (
                  <Text c="dimmed" size="sm">
                    No additional events match the selected filters.
                  </Text>
                )}

                {!recommendationsLoading &&
                  !recommendationsError &&
                  browseItems.map((item) => <BrowseCard key={item.id} event={item} />)}
              </Stack>
            )}
          </section>
        </Stack>
      </Grid.Col>
    </Grid>
  );
}

function OrdersPage({ navigate }) {
  return (
    <Stack gap="lg">
      <Group justify="space-between" align="baseline" gap="md">
        <Title order={2} c="brand.9">
          Your Orders
        </Title>
        <Text c="dimmed" size="sm">
          Pending purchase
        </Text>
      </Group>

      <SimpleGrid cols={{ base: 1, md: 2 }} spacing="md">
        <Paper className="browse-card" radius="md" p="lg" withBorder>
          <Stack gap="md">
            <Group justify="space-between" align="flex-start" gap="md" wrap="nowrap">
              <Box className="browse-copy">
                <Title order={3} size="h4" c="brand.9" lineClamp={2}>
                  Music Festival 2026
                </Title>
                <Text size="sm" fw="bold" c="brand.7">
                  Central Park Amphitheater
                </Text>
                <Text size="sm" c="dimmed">
                  General, VIP, and student tickets
                </Text>
              </Box>
              <Badge radius="sm" variant="light" color="brand">
                Open
              </Badge>
            </Group>

            <Group justify="flex-end">
              <Button color="brand" onClick={() => navigate(`/events/${checkoutEventId}/checkout/tickets`)}>
                Checkout
              </Button>
            </Group>
          </Stack>
        </Paper>
      </SimpleGrid>
    </Stack>
  );
}

function Topbar({ activeTab, navigate }) {
  return (
    <Box component="header" className="topbar">
      <Container size="xl" px={{ base: "md", sm: "xl" }} py="sm">
        <Group justify="space-between" align="center" gap="md" wrap="wrap">
          <Group gap="sm" wrap="nowrap">
            <TicketLogo />
            <Box>
              <Title order={1} size="h2" c="brand.9">
                Event
              </Title>
              <Title order={1} size="h2" c="brand.9">
                Ticket
              </Title>
            </Box>
          </Group>

          <Tabs
            value={activeTab}
            onChange={(value) => navigate(value === "orders" ? "/orders" : "/")}
            color="brand"
            className="nav-tabs"
          >
            <Tabs.List>
              <Tabs.Tab value="browse">Browse Events</Tabs.Tab>
              <Tabs.Tab value="orders">Your Orders</Tabs.Tab>
            </Tabs.List>
          </Tabs>

          <Group gap="sm" wrap="wrap">
            <Button variant="default" color="gray">
              Sign in
            </Button>
            <Button color="brand">Create event</Button>
            <ActionIcon size="lg" radius="xl" variant="filled" color="brand" aria-label="Profile">
              <ProfileIcon />
            </ActionIcon>
          </Group>
        </Group>
      </Container>
    </Box>
  );
}

export default function App() {
  const [route, setRoute] = useState(readRoute);

  useEffect(() => {
    const onPopState = () => setRoute(readRoute());
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  function navigate(path) {
    window.history.pushState(null, "", path);
    setRoute(readRoute());
  }

  return (
    <CheckoutProvider>
      <Box className="app-frame">
        <Topbar activeTab={routeToTab(route.name)} navigate={navigate} />

        <Box component="main">
          <Container size="xl" px={{ base: "md", sm: "xl" }} py={{ base: "lg", sm: "xl" }}>
            {route.name === "browse" && <BrowsePage />}
            {route.name === "orders" && <OrdersPage navigate={navigate} />}
            {route.name === "tickets" && (
              <TicketSelectionPage eventId={route.eventId} navigate={navigate} />
            )}
            {route.name === "payment" && (
              <PaymentPage orderNumber={route.orderNumber} navigate={navigate} />
            )}
            {route.name === "confirmation" && (
              <ConfirmationPage orderNumber={route.orderNumber} navigate={navigate} />
            )}
          </Container>
        </Box>
      </Box>
    </CheckoutProvider>
  );
}
