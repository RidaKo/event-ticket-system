import { useEffect, useState } from "react";
import {
    Box,
    Button,
    Card,
    Container,
    Group,
    Select,
    SimpleGrid,
    Text,
    TextInput,
    Textarea,
    Title,
    NumberInput,
} from "@mantine/core";
import { DateTimePicker } from "@mantine/dates";
import {createEvent, getCategories, getVenues} from "../api/eventsApi.js";

export default function CreateEventPage() {
    const [venues, setVenues] = useState([]);
    const [categories, setCategories] = useState([]);

    const [form, setForm] = useState({
        venueId: null,
        organizerId: 1,
        categoryId: null,
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

    useEffect(() => {
        async function loadData() {
            try {
                const [venuesData, categoriesData] = await Promise.all([
                    getVenues(),
                    getCategories(),
                ]);
                console.log("VENUES DATA:", venuesData);

                setVenues(venuesData);
                console.log(venues);
                setCategories(categoriesData);
            } catch (err) {
                console.error("Failed to load venues/categories", err);
            }
        }

        loadData();
    }, []);

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
                venueId: null,
                organizerId: 1,
                categoryId: null,
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

    return (
        <Box bg="#f8f9fa" mih="100vh">
            <Container size="lg" py="xl">
                <Card radius="xl" shadow="sm" p="xl" withBorder>
                    <Title order={2} mb="sm">
                        Create Event
                    </Title>

                    <Text c="dimmed" mb="xl">
                        Fill in the event details
                    </Text>

                    <form onSubmit={handleSubmit}>
                        <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg">
                            <TextInput
                                label="Event Title"
                                required
                                value={form.title}
                                onChange={(e) => handleChange("title", e.currentTarget.value)}
                            />

                            <TextInput
                                label="Slug"
                                required
                                value={form.slug}
                                onChange={(e) => handleChange("slug", e.currentTarget.value)}
                            />

                            {/* VENUE SELECT */}
                            <Select
                                label="Venue"
                                placeholder="Select venue"
                                data={venues.map((v) => ({ value: String(v.id), label: `${v.name} (${v.addressLine1}, ${v.city})` }))}
                                value={form.venueId ? String(form.venueId) : null}
                                onChange={(value) => handleChange("venueId", value)}
                                required
                            />

                            {/* CATEGORY SELECT */}
                            <Select
                                label="Category"
                                placeholder="Select category"
                                data={categories.map((c) => ({ value: String(c.id), label: c.name }))}
                                value={form.categoryId ? String(form.categoryId) : null}
                                onChange={(value) => handleChange("categoryId", value)}
                                required
                            />

                            <NumberInput
                                label="Organizer ID"
                                value={form.organizerId}
                                onChange={(value) => handleChange("organizerId", value)}
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
                            minRows={4}
                            value={form.description}
                            onChange={(e) => handleChange("description", e.currentTarget.value)}
                        />

                        <Group mt="lg" grow>
                            <DateTimePicker
                                label="Start"
                                value={form.startDatetime}
                                onChange={(value) => handleChange("startDatetime", value)}
                            />

                            <DateTimePicker
                                label="End"
                                value={form.endDatetime}
                                onChange={(value) => handleChange("endDatetime", value)}
                            />
                        </Group>

                        <NumberInput
                            mt="lg"
                            label="Minimum Age"
                            value={form.minAge}
                            onChange={(value) => handleChange("minAge", value)}
                        />

                        <TextInput
                            mt="lg"
                            label="Cover Photo URL"
                            value={form.coverPhotoUrl}
                            onChange={(e) => handleChange("coverPhotoUrl", e.currentTarget.value)}
                        />

                        <Textarea
                            mt="lg"
                            label="Photo URLs"
                            description="comma separated"
                            value={form.photoUrls}
                            onChange={(e) => handleChange("photoUrls", e.currentTarget.value)}
                        />

                        <Group justify="flex-end" mt="xl">
                            <Button type="submit">Create Event</Button>
                        </Group>
                    </form>
                </Card>
            </Container>
        </Box>
    );
}
