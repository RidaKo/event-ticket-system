import {
    Box,
    Button,
    Container,
    Group,
    Tabs,
    Title,
} from "@mantine/core";

export default function OrganizerTopbar({
                                            activeTab,
                                            navigate,
                                            organizerId,
                                        }) {
    return (
        <Box component="header" className="topbar">
            <Container size="xl" py="sm">
                <Group justify="space-between" align="center">
                    <Group>
                        <Title order={2}>Organizer Dashboard</Title>
                    </Group>

                    <Tabs
                        value={activeTab}
                        onChange={(value) =>
                            navigate(`/organizer/${organizerId}/${value}`)
                        }
                    >
                        <Tabs.List>
                            <Tabs.Tab value="dashboard">
                                Dashboard
                            </Tabs.Tab>

                            <Tabs.Tab value="create-event">
                                Create Event
                            </Tabs.Tab>

                            <Tabs.Tab value="events">
                                My Events
                            </Tabs.Tab>

                            <Tabs.Tab value="create-venue">
                                Create Venue
                            </Tabs.Tab>
                        </Tabs.List>
                    </Tabs>

                    <Button
                        variant="light"
                        onClick={() => navigate("/")}
                    >
                        Public Site
                    </Button>
                </Group>
            </Container>
        </Box>
    );
}