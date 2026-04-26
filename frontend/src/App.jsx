import {
  ActionIcon,
  Badge,
  Box,
  Button,
  Card,
  Divider,
  Group,
  Paper,
  Pill,
  Stack,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import ticketLogo from "./assets/ticket_small.png";

const categories = ["Music", "Sports", "Arts", "Technology", "Food"];
const tags = ["Outdoor", "Family", "Networking", "Educational"];
const recommendedItems = Array.from({ length: 6 }, (_, index) => ({
  id: index + 1,
}));
const browseItems = Array.from({ length: 5 }, (_, index) => ({
  id: index + 1,
}));

function TicketLogo() {
  return (
    <div className="logo-badge">
      <img src={ticketLogo} alt="Event Ticket logo" className="logo-image" />
    </div>
  );
}

function BookmarkIcon() {
  return (
    <svg
      className="bookmark-icon"
      viewBox="0 0 24 24"
      width="18"
      height="18"
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

function EventCard() {
  return (
    <Card className="event-card" radius="md" padding="md">
      <Stack gap="sm" h="100%">
        <div className="media-placeholder">Image</div>
        <Stack gap={8}>
          <Box className="event-line-dark" h={12} w="76%" />
          <Box className="event-line-light" h={10} w="48%" />
          <Box className="event-line-light" h={10} w="68%" />
        </Stack>
        <Group justify="space-between" mt="auto" align="center">
          <Group gap={8}>
            <Badge radius="sm" variant="light" color="gray">
              Tag
            </Badge>
            <Badge radius="sm" variant="light" color="gray">
              Category
            </Badge>
          </Group>
          <BookmarkIcon />
        </Group>
      </Stack>
    </Card>
  );
}

function BrowseCard() {
  return (
    <Paper className="browse-card" radius="md" withBorder>
      <div className="media-placeholder">Img</div>
      <Stack gap={10} style={{ flex: 1 }}>
        <Box className="event-line-dark" h={13} w="52%" />
        <Box className="event-line-light" h={10} w="28%" />
        <Box className="event-line-light" h={10} w="41%" />
        <Group justify="space-between" mt={4} align="center">
          <Badge radius="sm" variant="light" color="gray">
            Tag
          </Badge>
          <BookmarkIcon />
        </Group>
      </Stack>
    </Paper>
  );
}

function FilterPanel() {
  return (
    <Paper className="filter-card" radius="md" withBorder>
      <Stack gap={0}>
        <Box px="lg" py="md">
          <Text fw={700} size="sm" c="#2f3950">
            Filter by Preferences
          </Text>
        </Box>
        <Divider color="#d6ddeb" />
        <Box px="lg" py="xl" className="filter-body">
          <div className="filter-section">
            <div className="filter-label">Category</div>
            <Group gap={6}>
              {categories.map((item) => (
                <Pill key={item} size="sm">
                  {item}
                </Pill>
              ))}
            </Group>
          </div>

          <div className="filter-section">
            <div className="filter-label">Interest Tags</div>
            <Group gap={6}>
              {tags.map((item) => (
                <Pill key={item} size="sm">
                  {item}
                </Pill>
              ))}
            </Group>
          </div>

          <div className="filter-section">
            <div className="filter-label">Date Range</div>
            <TextInput placeholder="Start date" variant="unstyled" />
            <TextInput placeholder="End date" variant="unstyled" />
          </div>

          <div className="filter-section">
            <div className="filter-label">Location</div>
            <TextInput placeholder="City or venue" variant="unstyled" />
          </div>

          <Divider className="divider-soft" />

          <div className="button-row">
            <Button variant="light" color="gray">
              Reset
            </Button>
            <Button color="brand">Apply</Button>
          </div>
        </Box>
      </Stack>
    </Paper>
  );
}

export default function App() {
  return (
    <div className="app-stage">
      <div className="app-frame">
        <header className="topbar">
          <div className="topbar-inner">
            <div className="logo-group">
              <TicketLogo />
              <div className="logo-copy">
                <Title order={1} size="2rem" c="#33415f" fw={800} lh={1}>
                  Event
                </Title>
                <Title order={1} size="2rem" c="#33415f" fw={800} lh={1}>
                  Ticket
                </Title>
              </div>
            </div>

            <div className="topbar-nav">
              <Text className="nav-link active">Browse Events</Text>
              <Text className="nav-link">Your Orders</Text>
            </div>

            <div className="topbar-actions">
              <Button variant="default" color="gray">
                Button
              </Button>
              <Button color="brand">Button</Button>
              <ActionIcon
                size={42}
                radius="xl"
                variant="filled"
                color="dark"
                aria-label="Profile"
              >
                <svg
                  viewBox="0 0 24 24"
                  width="18"
                  height="18"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="12" cy="8" r="3.5" />
                  <path d="M5 20c1.2-3.2 3.8-5 7-5s5.8 1.8 7 5" />
                </svg>
              </ActionIcon>
            </div>
          </div>
        </header>

        <main className="content-shell">
          <div className="content-grid">
            <FilterPanel />

            <Stack gap="xl">
              <section>
                <div className="section-heading">
                  <Title order={2} size="1.65rem" fw={500} c="#38445d">
                    Recommended for you
                  </Title>
                  <Text c="#b1b9ca" size="sm">
                    Based on your preferences
                  </Text>
                </div>

                <div className="recommend-grid">
                  {recommendedItems.map((item) => (
                    <EventCard key={item.id} />
                  ))}
                </div>
              </section>

              <section>
                <div className="section-heading">
                  <Title order={2} size="1.65rem" fw={500} c="#38445d">
                    Browse Events
                  </Title>
                </div>

                <div className="browse-list">
                  {browseItems.map((item) => (
                    <BrowseCard key={item.id} />
                  ))}
                </div>
              </section>
            </Stack>
          </div>
        </main>
      </div>
    </div>
  );
}
