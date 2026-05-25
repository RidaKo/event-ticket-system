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
  Pagination,
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
import CheckoutAccountPage from "./pages/CheckoutAccountPage.jsx";
import EventDetailsPage from "./pages/EventDetailsPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import SignUpPage from "./pages/SignUpPage.jsx";
import TicketSelectionPage from "./pages/TicketSelectionPage.jsx";
import PaymentPage from "./pages/PaymentPage.jsx";
import ConfirmationPage from "./pages/ConfirmationPage.jsx";
import { getCatalog } from "./api/catalog.js";
import { getRecommendedEvents } from "./api/recommendations.js";
import { getUserOrders } from "./api/ordersApi.js";
import { CheckoutProvider } from "./state/CheckoutContext.jsx";
import { createEmptyFilters, hasActiveFilters, normalizeFilters } from "./lib/catalog.js";
import { dateRangeToFilters, filtersToDateRange } from "./lib/dateFilters.js";
import { mapRecommendedEvent } from "./lib/recommendations.js";
import { formatDateTime, formatMoney } from "./utils.js";
import { DatePickerInput } from "@mantine/dates";
import { AuthProvider, useAuth } from "./state/AuthContext.jsx";
import UserPreferencesModal from "./components/UserPreferencesModal.jsx";

import CreateEventPage from "./pages/Organizer/CreateEventPage.jsx";
import OrganizerTopbar from "./components/OrganizerTopbar";
import organizerRouteToTab from "./components/OrganizerTabMapper.jsx";

import OrganizerDashboardPage from "./pages/Organizer/OrganizerDashboardPage";
import OrganizerEventsPage from "./pages/Organizer/OrganizerEventsPage.jsx";
import CreateVenuePage from "./pages/Organizer/CreateVenuePage";


const RECOMMENDED_LIMIT = 6;
const EVENTS_PAGE_SIZE = 12;
const ORDERS_PAGE_SIZE = 6;

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

  const account = path.match(/^\/events\/(\d+)\/checkout\/account$/);
  if (account) {
    return { name: "account", eventId: Number(account[1]) };
  }

  const tickets = path.match(/^\/events\/(\d+)\/checkout\/tickets$/);
  if (tickets) {
    return { name: "tickets", eventId: Number(tickets[1]) };
  }

  const eventDetails = path.match(/^\/events\/(\d+)$/);
  if (eventDetails) {
    return { name: "eventDetails", eventId: Number(eventDetails[1]) };
  }

  if (path === "/orders") {
    return { name: "orders" };
  }

  const orderDetail = path.match(/^\/orders\/([^/]+)$/);
  if (orderDetail) {
    return { name: "orderDetail", orderNumber: decodeURIComponent(orderDetail[1]) };
  }

  if (path.startsWith("/organizer/")) {
    const parts = path.split("/");

    return {
      organizerId: parts[2],
      name: parts[3] || "dashboard",
    };
  }

  if (path === "/signin") {
    return { name: "signin" };
  }

  if (path === "/signup") {
    return { name: "signup" };
  }

  return { name: "browse" };
}

function routeToTab(routeName) {
  if (["browse", "eventDetails", "tickets", "account"].includes(routeName)) {
    return "browse";
  }
  if (["orders", "orderDetail", "payment", "confirmation"].includes(routeName)) {
    return "orders";
  }
  return null;
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

function handleCardKeyDown(event, open) {
  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    open();
  }
}

function EventCard({ event, navigate }) {
  const open = () => navigate(`/events/${event.id}`);
  return (
    <Card
      className="event-card event-link-card"
      radius="md"
      padding="md"
      withBorder
      role="link"
      tabIndex={0}
      onClick={open}
      onKeyDown={(keyEvent) => handleCardKeyDown(keyEvent, open)}
    >
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
            onClick={(clickEvent) => clickEvent.stopPropagation()}
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

function BrowseCard({ event, navigate }) {
  const open = () => navigate(`/events/${event.id}`);
  return (
    <Paper
      className="browse-card event-link-card"
      radius="md"
      p="sm"
      withBorder
      role="link"
      tabIndex={0}
      onClick={open}
      onKeyDown={(keyEvent) => handleCardKeyDown(keyEvent, open)}
    >
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
                onClick={(clickEvent) => clickEvent.stopPropagation()}
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

function BrowsePage({ navigate, preferencesVersion, isAuthenticated, onSetPreferences }) {
  const [draftFilters, setDraftFilters] = useState(createEmptyFilters);
  const [appliedFilters, setAppliedFilters] = useState(createEmptyFilters);
  const [catalog, setCatalog] = useState({ categories: [], tags: [] });
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState(false);
  const [eventItems, setEventItems] = useState([]);
  const [eventPage, setEventPage] = useState(1);
  const [eventPageInfo, setEventPageInfo] = useState({
    totalElements: 0,
    totalPages: 0
  });
  const [recommendationsLoading, setRecommendationsLoading] = useState(true);
  const [recommendationsError, setRecommendationsError] = useState(false);
  const [fallbackUsed, setFallbackUsed] = useState(false);
  const [personalized, setPersonalized] = useState(false);

  const showRecommendations = personalized && eventPage === 1;
  const recommendedItems = showRecommendations ? eventItems.slice(0, RECOMMENDED_LIMIT) : [];
  const browseItems = showRecommendations ? eventItems.slice(RECOMMENDED_LIMIT) : eventItems;
  const showBrowseAllAboveHint =
    showRecommendations &&
    !recommendationsLoading &&
    !recommendationsError &&
    browseItems.length === 0 &&
    recommendedItems.length > 0;
  const showPreferencesPrompt =
    isAuthenticated && !personalized && !recommendationsLoading && !recommendationsError;

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
          page: eventPage - 1,
          size: EVENTS_PAGE_SIZE,
        });
        if (!cancelled) {
          setEventItems((response.items ?? []).map(mapRecommendedEvent));
          setEventPageInfo({
            totalElements: response.totalElements ?? response.items?.length ?? 0,
            totalPages: response.totalPages ?? 0
          });
          setFallbackUsed(Boolean(response.fallbackUsed));
          setPersonalized(Boolean(response.personalized));
        }
      } catch {
        if (!cancelled) {
          setRecommendationsError(true);
          setEventItems([]);
          setEventPageInfo({ totalElements: 0, totalPages: 0 });
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
  }, [appliedFilters, preferencesVersion, eventPage]);

  function handleApplyFilters() {
    const normalized = normalizeFilters(draftFilters);
    setDraftFilters(normalized);
    setEventPage(1);
    setAppliedFilters(normalized);
  }

  function handleResetFilters() {
    const empty = createEmptyFilters();
    setDraftFilters(empty);
    setEventPage(1);
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
          {showPreferencesPrompt && (
            <Paper radius="md" p="md" withBorder>
              <Stack gap="sm">
                <Text size="sm" c="brand.9" fw={600}>
                  Personalize your recommendations
                </Text>
                <Text size="sm" c="dimmed">
                  Choose your favorite categories, tags, and home city using the profile icon in the header
                  to see a Recommended for you section.
                </Text>
                <Button variant="light" color="brand" onClick={onSetPreferences} w="fit-content">
                  Set preferences
                </Button>
              </Stack>
            </Paper>
          )}

          {showRecommendations && (
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
                    <EventCard key={item.id} event={item} navigate={navigate} />
                  ))}
                </SimpleGrid>
              )}
            </section>
          )}

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
                    {showRecommendations
                      ? "No additional events match the selected filters."
                      : "No events match the selected filters."}
                  </Text>
                )}

                {!recommendationsLoading &&
                  !recommendationsError &&
                  browseItems.map((item) => (
                    <BrowseCard key={item.id} event={item} navigate={navigate} />
                  ))}

                {!recommendationsLoading && !recommendationsError && eventPageInfo.totalPages > 1 && (
                  <Group justify="center" pt="sm">
                    <Pagination
                      value={eventPage}
                      onChange={setEventPage}
                      total={eventPageInfo.totalPages}
                      color="brand"
                      radius="sm"
                    />
                  </Group>
                )}
              </Stack>
            )}
          </section>
        </Stack>
      </Grid.Col>
    </Grid>
  );
}

function OrdersPage({ navigate, currentUser }) {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(1);
  const [pageInfo, setPageInfo] = useState({
    totalElements: 0,
    totalPages: 0
  });
  const [loading, setLoading] = useState(Boolean(currentUser));
  const [error, setError] = useState("");

  useEffect(() => {
    setPage(1);
  }, [currentUser?.email]);

  useEffect(() => {
    if (!currentUser) {
      setOrders([]);
      setPage(1);
      setPageInfo({ totalElements: 0, totalPages: 0 });
      setLoading(false);
      setError("");
      return undefined;
    }

    let active = true;
    setLoading(true);
    setError("");

    getUserOrders({ page: page - 1, size: ORDERS_PAGE_SIZE })
      .then((data) => {
        if (active) {
          setOrders(data.items ?? []);
          setPageInfo({
            totalElements: data.totalElements ?? data.items?.length ?? 0,
            totalPages: data.totalPages ?? 0
          });
        }
      })
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [currentUser, page]);

  if (!currentUser) {
    return (
      <Paper className="detail-section" radius="md" p="xl" withBorder>
        <Stack gap="md" align="flex-start">
          <Title order={2} c="brand.9">
            Your Orders
          </Title>
          <Text c="dimmed">Sign in to see your completed orders and receipts.</Text>
          <Button color="brand" onClick={() => navigate("/signin")}>
            Sign in
          </Button>
        </Stack>
      </Paper>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="baseline" gap="md">
        <Title order={2} c="brand.9">
          Your Orders
        </Title>
        <Text c="dimmed" size="sm">
          {loading
            ? "Loading orders..."
            : `${pageInfo.totalElements} completed order${pageInfo.totalElements === 1 ? "" : "s"}`}
        </Text>
      </Group>

      {error && (
        <Paper className="detail-section" radius="md" p="lg" withBorder>
          <Text c="red" size="sm">
            {error}
          </Text>
        </Paper>
      )}

      {!error && loading && (
        <Paper className="detail-section" radius="md" p="lg" withBorder>
          <Text c="dimmed" size="sm">
            Loading your orders...
          </Text>
        </Paper>
      )}

      {!error && !loading && orders.length === 0 && (
        <Paper className="detail-section" radius="md" p="xl" withBorder>
          <Stack gap="md" align="flex-start">
            <Text c="dimmed">You do not have any completed orders yet.</Text>
            <Button color="brand" onClick={() => navigate("/")}>
              Browse events
            </Button>
          </Stack>
        </Paper>
      )}

      {!error && !loading && orders.length > 0 && (
        <Stack gap="md">
          <SimpleGrid cols={{ base: 1, md: 2 }} spacing="md">
            {orders.map((order) => (
              <OrderHistoryCard key={order.orderNumber} order={order} navigate={navigate} />
            ))}
          </SimpleGrid>

          {pageInfo.totalPages > 1 && (
            <Group justify="center">
              <Pagination
                value={page}
                onChange={setPage}
                total={pageInfo.totalPages}
                color="brand"
                radius="sm"
              />
            </Group>
          )}
        </Stack>
      )}
    </Stack>
  );
}

function OrderHistoryCard({ order, navigate }) {
  const ticketCount = (order.summary?.items ?? []).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  const open = () => navigate(`/orders/${encodeURIComponent(order.orderNumber)}`);

  return (
    <Paper
      className="browse-card event-link-card"
      radius="md"
      p="lg"
      withBorder
      role="link"
      tabIndex={0}
      onClick={open}
      onKeyDown={(keyEvent) => handleCardKeyDown(keyEvent, open)}
    >
      <Stack gap="md">
        <Group justify="space-between" align="flex-start" gap="md" wrap="nowrap">
          <Box className="browse-copy">
            <Title order={3} size="h4" c="brand.9" lineClamp={2}>
              {order.event.title}
            </Title>
            <Text size="sm" fw="bold" c="brand.7">
              {formatDateTime(order.event.startsAt)}
            </Text>
            <Text size="sm" c="dimmed" lineClamp={1}>
              {order.event.venueName}
            </Text>
          </Box>
          <Badge radius="sm" variant="light" color="brand">
            {order.status}
          </Badge>
        </Group>

        <Group justify="space-between" gap="md" align="flex-end">
          <Stack gap={2}>
            <Text size="xs" c="dimmed">
              {order.orderNumber}
            </Text>
            <Text size="sm" c="dimmed">
              {ticketCount} ticket{ticketCount === 1 ? "" : "s"}
            </Text>
          </Stack>
          <Text fw="bold" c="brand.9">
            {formatMoney(order.summary?.total)}
          </Text>
        </Group>
      </Stack>
    </Paper>
  );
}

function Topbar({ activeTab, currentUser, isAuthRoute, navigate, onSignOut, onProfileClick }) {
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
            {currentUser ? (
              <>
                <Text size="sm" fw="bold" c="brand.9">
                  {currentUser.fullName || currentUser.email}
                </Text>
                <Button variant="default" color="gray" onClick={onSignOut}>
                  Sign out
                </Button>
              </>
            ) : (
              <Button
                variant={isAuthRoute ? "filled" : "default"}
                color={isAuthRoute ? "brand" : "gray"}
                onClick={() => navigate("/signin")}
              >
                Sign in
              </Button>
            )}
            <ActionIcon
              size="lg"
              radius="xl"
              variant="filled"
              color="brand"
              aria-label={currentUser ? "Edit recommendation preferences" : "Sign in"}
              onClick={onProfileClick}
            >
              <ProfileIcon />
            </ActionIcon>
          </Group>
        </Group>
      </Container>
    </Box>
  );
}

function AppContent() {
  const [route, setRoute] = useState(readRoute);
  const [preferencesOpen, setPreferencesOpen] = useState(false);
  const [preferencesVersion, setPreferencesVersion] = useState(0);
  const { logout, user } = useAuth();

  useEffect(() => {
    const onPopState = () => setRoute(readRoute());

    window.addEventListener("popstate", onPopState);

    return () =>
        window.removeEventListener("popstate", onPopState);
  }, []);

  function navigate(path) {
    window.history.pushState(null, "", path);
    setRoute(readRoute());
  }

  const isOrganizerRoute = route.organizerId != null;
  const isAuthRoute = ["signin", "signup"].includes(route.name);

  function signOut() {
    logout();
    navigate("/");
  }

  function handleProfileClick() {
    if (user) {
      setPreferencesOpen(true);
      return;
    }
    navigate("/signin");
  }

  function handlePreferencesSaved() {
    setPreferencesVersion((current) => current + 1);
  }

  return (
    <CheckoutProvider>
      <Box className="app-frame">
        {isOrganizerRoute ? (
          <OrganizerTopbar
            activeTab={organizerRouteToTab(route.name)}
            navigate={navigate}
            organizerId={route.organizerId}
          />
        ) : (
          <Topbar
            activeTab={routeToTab(route.name)}
            currentUser={user}
            navigate={navigate}
            isAuthRoute={isAuthRoute}
            onSignOut={signOut}
            onProfileClick={handleProfileClick}
          />
        )}

        <UserPreferencesModal
          opened={preferencesOpen}
          onClose={() => setPreferencesOpen(false)}
          onSaved={handlePreferencesSaved}
        />

        <Box component="main">
          <Container size="xl" px={{ base: "md", sm: "xl" }} py={{ base: "lg", sm: "xl" }}>
            {!isOrganizerRoute && (
              <>
                {route.name === "browse" && (
                  <BrowsePage
                    navigate={navigate}
                    preferencesVersion={preferencesVersion}
                    isAuthenticated={Boolean(user)}
                    onSetPreferences={() => setPreferencesOpen(true)}
                  />
                )}
                {route.name === "orders" && <OrdersPage navigate={navigate} currentUser={user} />}
                {route.name === "orderDetail" && (
                  <ConfirmationPage orderNumber={route.orderNumber} navigate={navigate} />
                )}
                {route.name === "signin" && <LoginPage navigate={navigate} />}
                {route.name === "signup" && <SignUpPage navigate={navigate} />}
                {route.name === "eventDetails" && (
                  <EventDetailsPage eventId={route.eventId} navigate={navigate} />
                )}
                {route.name === "tickets" && (
                  <TicketSelectionPage eventId={route.eventId} navigate={navigate} />
                )}
                {route.name === "account" && (
                  <CheckoutAccountPage eventId={route.eventId} navigate={navigate} />
                )}
                {route.name === "payment" && (
                  <PaymentPage orderNumber={route.orderNumber} navigate={navigate} />
                )}
                {route.name === "confirmation" && (
                  <ConfirmationPage orderNumber={route.orderNumber} navigate={navigate} />
                )}
              </>
            )}

            {isOrganizerRoute && (
              <>
                {route.name === "dashboard" && (
                  <OrganizerDashboardPage organizerId={route.organizerId} />
                )}
                {route.name === "create-event" && (
                  <CreateEventPage organizerId={route.organizerId} navigate={navigate} />
                )}
                {route.name === "events" && (
                  <OrganizerEventsPage organizerId={route.organizerId} />
                )}
                {route.name === "create-venue" && (
                  <CreateVenuePage organizerId={route.organizerId} />
                )}
              </>
            )}
          </Container>
        </Box>
      </Box>
    </CheckoutProvider>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
