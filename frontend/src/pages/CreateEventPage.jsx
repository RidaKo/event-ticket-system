import { useState } from "react";
import { createEvent } from "../api/eventsApi.js";
import {
    ActionIcon,
    Box,
    Button,
    Card,
    Container,
    Grid,
    Group,
    Select,
    SimpleGrid,
    Tabs,
    Text,
    TextInput,
    Textarea,
    Title,
    NumberInput,
} from "@mantine/core";
import { DateTimePicker } from "@mantine/dates";

function TicketLogo() {
    return (
        <Box
            w={48}
            h={48}
            style={{
                borderRadius: "16px",
                background: "linear-gradient(135deg, #5f3dc4, #845ef7)",
            }}
        />
    );
}

function ProfileIcon() {
    return (
        <Box
            w={18}
            h={18}
            style={{
                borderRadius: "50%",
                backgroundColor: "white",
            }}
        />
    );
}

function Topbar({ navigate }) {
    return (
        <Box
            component="header"
            style={{
                borderBottom: "1px solid #e9ecef",
                backgroundColor: "white",
                position: "sticky",
                top: 0,
                zIndex: 100,
            }}
        >
            <Container size="xl" px={{ base: "md", sm: "xl" }} py="sm">
                <Group justify="space-between" align="center" gap="md" wrap="wrap">
                    <Group gap="sm" wrap="nowrap">
                        <TicketLogo />
                        <Box>
                            <Title order={1} size="h2" c="violet.9">
                                Event
                            </Title>
                            <Title order={1} size="h2" c="violet.9">
                                Ticket
                            </Title>
                        </Box>
                    </Group>

                    <Tabs
                        value="create"
                        color="violet"
                    >
                        <Tabs.List>
                            <Tabs.Tab value="browse" onClick={() => navigate("/")}>
                                Browse Events
                            </Tabs.Tab>
                            <Tabs.Tab value="orders" onClick={() => navigate("/orders")}>
                                Your Orders
                            </Tabs.Tab>
                            <Tabs.Tab value="create">
                                Create Event
                            </Tabs.Tab>
                        </Tabs.List>
                    </Tabs>

                    <Group gap="sm" wrap="wrap">
                        <Button variant="default" color="gray">
                            Sign in
                        </Button>
                        <ActionIcon
                            size="lg"
                            radius="xl"
                            variant="filled"
                            color="violet"
                            aria-label="Profile"
                        >
                            <ProfileIcon />
                        </ActionIcon>
                    </Group>
                </Group>
            </Container>
        </Box>
    );
}

export default function CreateEventPage() {
    const [form, setForm] = useState({
        venueId: "",
        organizerId: "",
        categoryId: "",
        title: "",
        slug: "",
        description: "",
        status: "DRAFT",
        startDatetime: null,
        endDatetime: null,
        timezone: "Europe/Vilnius",
        minAge: 0,
        coverPhotoUrl: "",
        photoUrls: "",
    });

    const handleChange = (field, value) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            ...form,
            venueId: Number(form.venueId),
            organizerId: Number(form.organizerId),
            categoryId: Number(form.categoryId),
            minAge: Number(form.minAge),
        };

        try {
            await createEvent(payload);

            alert("Event created successfully!");

            setForm({
                venueId: "",
                organizerId: "",
                categoryId: "",
                title: "",
                slug: "",
                description: "",
                status: "DRAFT",
                startDatetime: null,
                endDatetime: null,
                timezone: "Europe/Vilnius",
                minAge: 0,
                coverPhotoUrl: "",
                photoUrls: "",
            });
        } catch (error) {
            console.error(error);
            alert("Could not create event");
        }
    };

    const navigate = (path) => {
        window.location.href = path;
    };

    return (
        <Box bg="#f8f9fa" mih="100vh">
            <Topbar navigate={navigate} />

            <Container size="lg" py="xl">
                <Card
                    radius="xl"
                    shadow="sm"
                    p="xl"
                    withBorder
                >
                    <Group justify="space-between" align="flex-start" mb="xl">
                        <Box>
                            <Title order={2} mb={4}>
                                Create New Event
                            </Title>
                            <Text c="dimmed">
                                Fill in the event details and publish your event.
                            </Text>
                        </Box>
                    </Group>

                    <form onSubmit={handleSubmit}>
                        <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg">
                            <TextInput
                                label="Event Title"
                                placeholder="Summer Music Festival"
                                required
                                value={form.title}
                                onChange={(e) => handleChange("title", e.currentTarget.value)}
                            />

                            <TextInput
                                label="Slug"
                                placeholder="summer-music-festival"
                                required
                                value={form.slug}
                                onChange={(e) => handleChange("slug", e.currentTarget.value)}
                            />

                            <NumberInput
                                label="Venue ID"
                                placeholder="1"
                                required
                                value={form.venueId}
                                onChange={(value) => handleChange("venueId", value)}
                            />

                            <NumberInput
                                label="Organizer ID"
                                placeholder="1"
                                required
                                value={form.organizerId}
                                onChange={(value) => handleChange("organizerId", value)}
                            />

                            <NumberInput
                                label="Category ID"
                                placeholder="1"
                                required
                                value={form.categoryId}
                                onChange={(value) => handleChange("categoryId", value)}
                            />

                            <Select
                                label="Status"
                                data={["DRAFT", "PUBLISHED", "CANCELLED"]}
                                value={form.status}
                                onChange={(value) => handleChange("status", value)}
                            />
                        </SimpleGrid>

                        <Textarea
                            mt="lg"
                            label="Description"
                            placeholder="Describe your event..."
                            minRows={5}
                            required
                            value={form.description}
                            onChange={(e) => handleChange("description", e.currentTarget.value)}
                        />

                        <Grid mt="lg">
                            <Grid.Col span={{ base: 12, md: 6 }}>
                                <DateTimePicker
                                    label="Start Date & Time"
                                    placeholder="Pick start date"
                                    required
                                    value={form.startDatetime}
                                    onChange={(value) => handleChange("startDatetime", value)}
                                />
                            </Grid.Col>

                            <Grid.Col span={{ base: 12, md: 6 }}>
                                <DateTimePicker
                                    label="End Date & Time"
                                    placeholder="Pick end date"
                                    required
                                    value={form.endDatetime}
                                    onChange={(value) => handleChange("endDatetime", value)}
                                />
                            </Grid.Col>
                        </Grid>

                        <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg" mt="lg">
                            <TextInput
                                label="Timezone"
                                placeholder="Europe/Vilnius"
                                value={form.timezone}
                                onChange={(e) => handleChange("timezone", e.currentTarget.value)}
                            />

                            <NumberInput
                                label="Minimum Age"
                                placeholder="18"
                                value={form.minAge}
                                onChange={(value) => handleChange("minAge", value)}
                            />
                        </SimpleGrid>

                        <TextInput
                            mt="lg"
                            label="Cover Photo URL"
                            placeholder="https://example.com/cover.jpg"
                            value={form.coverPhotoUrl}
                            onChange={(e) => handleChange("coverPhotoUrl", e.currentTarget.value)}
                        />

                        <Textarea
                            mt="lg"
                            label="Photo URLs"
                            description="Separate multiple URLs with commas"
                            placeholder="https://example.com/1.jpg, https://example.com/2.jpg"
                            minRows={3}
                            value={form.photoUrls}
                            onChange={(e) => handleChange("photoUrls", e.currentTarget.value)}
                        />

                        <Group justify="flex-end" mt="xl">
                            <Button variant="default">
                                Cancel
                            </Button>

                            <Button type="submit" color="violet">
                                Create Event
                            </Button>
                        </Group>
                    </form>
                </Card>
            </Container>
        </Box>
    );
}
