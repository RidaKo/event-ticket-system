import { useState } from "react";
import {
    Button,
    Card,
    Stack,
    TextInput,
    Title,
} from "@mantine/core";

import { createVenue } from "../../api/venuesApi";

export default function CreateVenuePage({ organizerId }) {
    const [form, setForm] = useState({
        name: "",
        addressLine1: "",
        city: "",
        country: "",
    });

    function handleChange(field, value) {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    }

    async function handleSubmit() {
        try {
            await createVenue({
                ...form,
                organizerId: Number(organizerId),
            });

            alert("Venue created successfully!");
        } catch (err) {
            console.error(err);
            alert("Failed to create venue");
        }
    }

    return (
        <>
            <Title mb="lg">Create Venue</Title>

            <Card withBorder p="lg">
                <Stack>
                    <TextInput
                        label="Venue Name"
                        placeholder="Grand Hall"
                        value={form.name}
                        onChange={(e) =>
                            handleChange("name", e.target.value)
                        }
                    />

                    <TextInput
                        label="Address"
                        placeholder="Gedimino pr. 1"
                        value={form.addressLine1}
                        onChange={(e) =>
                            handleChange("addressLine1", e.target.value)
                        }
                    />

                    <TextInput
                        label="City"
                        placeholder="Vilnius"
                        value={form.city}
                        onChange={(e) =>
                            handleChange("city", e.target.value)
                        }
                    />

                    <TextInput
                        label="Country"
                        placeholder="LT"
                        value={form.country}
                        onChange={(e) =>
                            handleChange("country", e.target.value)
                        }
                    />

                    <Button onClick={handleSubmit}>
                        Create Venue
                    </Button>
                </Stack>
            </Card>
        </>
    );
}