import { Grid, Stack } from "@mantine/core";
import {
  AuthHeader,
  AuthSwitchCard,
  SignInFormSection,
  SignInIcon,
  UserPlusIcon,
} from "../components/AuthShared.jsx";

export default function LoginPage({ navigate }) {
  return (
    <Grid className="auth-shell" gutter="lg" align="flex-start">
      <Grid.Col span={{ base: 12, md: 7 }}>
        <Stack gap="md">
          <AuthHeader
            icon={<SignInIcon />}
            title="Sign in"
            description="Use your account to manage orders and events."
          />
          <SignInFormSection />
        </Stack>
      </Grid.Col>

      <Grid.Col span={{ base: 12, md: 5 }}>
        <AuthSwitchCard
          icon={<UserPlusIcon />}
          title="New to Event Ticket?"
          description="Create an account with your name, email, phone, and password."
          actionLabel="Create account"
          onAction={() => navigate("/signup")}
        />
      </Grid.Col>
    </Grid>
  );
}
