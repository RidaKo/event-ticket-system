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

const categories = ["Music", "Sports", "Arts", "Technology", "Food"];
const tags = ["Outdoor", "Family", "Networking", "Educational"];
const recommendedItems = [
  {
    id: 1,
    title: "Riverside Jazz Night",
    date: "Fri, Jun 12",
    venue: "Paradise Hall",
    tag: "Music",
    category: "Outdoor",
  },
  {
    id: 2,
    title: "Startup Founders Mixer",
    date: "Sat, Jun 13",
    venue: "North Pier Studio",
    tag: "Networking",
    category: "Technology",
  },
  {
    id: 3,
    title: "Family Food Festival",
    date: "Sun, Jun 14",
    venue: "Central Park",
    tag: "Family",
    category: "Food",
  },
  {
    id: 4,
    title: "Open Air Cinema",
    date: "Thu, Jun 18",
    venue: "Riverfront Lawn",
    tag: "Outdoor",
    category: "Arts",
  },
  {
    id: 5,
    title: "Design Systems Workshop",
    date: "Fri, Jun 19",
    venue: "Creative Campus",
    tag: "Educational",
    category: "Technology",
  },
  {
    id: 6,
    title: "City Arena Finals",
    date: "Sat, Jun 20",
    venue: "City Arena",
    tag: "Family",
    category: "Sports",
  },
];
const browseItems = [
  {
    id: 1,
    title: "Acoustic Sessions",
    date: "Today",
    venue: "Old Town Stage",
    tag: "Music",
  },
  {
    id: 2,
    title: "Modern Art Walk",
    date: "Tomorrow",
    venue: "Gallery District",
    tag: "Arts",
  },
  {
    id: 3,
    title: "Junior Football Camp",
    date: "This weekend",
    venue: "South Field",
    tag: "Sports",
  },
  {
    id: 4,
    title: "Cloud Engineering Forum",
    date: "Next Tuesday",
    venue: "Tech Hub",
    tag: "Technology",
  },
  {
    id: 5,
    title: "Street Food Showcase",
    date: "Next Friday",
    venue: "Market Square",
    tag: "Food",
  },
];

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
          <Badge radius="sm" variant="light" color="brand">
            {event.tag}
          </Badge>
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
            <Group>
              <Badge radius="sm" variant="light" color="brand">
                {event.tag}
              </Badge>
            </Group>
          </Stack>
        </Grid.Col>
      </Grid>
    </Paper>
  );
}

function FilterPanel() {
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
              {categories.map((item) => (
                <Pill key={item} size="sm">
                  {item}
                </Pill>
              ))}
            </Group>
          </Stack>

          <Stack gap="xs">
            <Text className="filter-label" size="xs" fw="bold" c="dimmed" tt="uppercase">
              Interest Tags
            </Text>
            <Group gap="xs">
              {tags.map((item) => (
                <Pill key={item} size="sm">
                  {item}
                </Pill>
              ))}
            </Group>
          </Stack>

          <Stack gap="xs">
            <Text className="filter-label" size="xs" fw="bold" c="dimmed" tt="uppercase">
              Date Range
            </Text>
            <TextInput placeholder="Start date" variant="filled" />
            <TextInput placeholder="End date" variant="filled" />
          </Stack>

          <Stack gap="xs">
            <Text className="filter-label" size="xs" fw="bold" c="dimmed" tt="uppercase">
              Location
            </Text>
            <TextInput placeholder="City or venue" variant="filled" />
          </Stack>

          <Divider color="brand.1" />

          <SimpleGrid cols={{ base: 1, xs: 2 }} spacing="sm">
            <Button variant="light" color="gray">
              Reset
            </Button>
            <Button color="brand">Apply</Button>
          </SimpleGrid>
        </Box>
      </Box>
    </Paper>
  );
}

export default function App() {
  return (
    <Box className="app-frame">
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

            <Tabs defaultValue="browse" color="brand" className="nav-tabs">
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
              <ActionIcon
                size="lg"
                radius="xl"
                variant="filled"
                color="brand"
                aria-label="Profile"
              >
                <svg
                  viewBox="0 0 24 24"
                  width="1em"
                  height="1em"
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
            </Group>
          </Group>
        </Container>
      </Box>

      <Box component="main">
        <Container size="xl" px={{ base: "md", sm: "xl" }} py={{ base: "lg", sm: "xl" }}>
          <Grid gutter="lg" align="flex-start">
            <Grid.Col span={{ base: 12, md: 4, lg: 3 }}>
              <FilterPanel />
            </Grid.Col>

            <Grid.Col span={{ base: 12, md: 8, lg: 9 }}>
              <Stack gap="xl">
                <section>
                  <Group justify="space-between" align="baseline" gap="md" mb="md">
                    <Title order={2} c="brand.9">
                      Recommended for you
                    </Title>
                    <Text c="dimmed" size="sm">
                      Based on your preferences
                    </Text>
                  </Group>

                  <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }} spacing="md">
                    {recommendedItems.map((item) => (
                      <EventCard key={item.id} event={item} />
                    ))}
                  </SimpleGrid>
                </section>

                <section>
                  <Group justify="space-between" align="baseline" gap="md" mb="md">
                    <Title order={2} c="brand.9">
                      Browse Events
                    </Title>
                  </Group>

                  <Stack gap="sm">
                    {browseItems.map((item) => (
                      <BrowseCard key={item.id} event={item} />
                    ))}
                  </Stack>
                </section>
              </Stack>
            </Grid.Col>
          </Grid>
        </Container>
      </Box>
    </Box>
  );
}
