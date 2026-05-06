import { Box, Grid, Stack, Title } from "@mantine/core";

export default function CheckoutStepLayout({ title, children, sidebar }) {
  return (
    <Grid gutter="lg" align="flex-start">
      <Grid.Col span={{ base: 12, md: 8, lg: 9 }}>
        <Stack gap="md">
          <Title order={2} c="brand.9">
            {title}
          </Title>
          {children}
        </Stack>
      </Grid.Col>
      <Grid.Col span={{ base: 12, md: 4, lg: 3 }}>
        <Box className="checkout-sidebar">{sidebar}</Box>
      </Grid.Col>
    </Grid>
  );
}
