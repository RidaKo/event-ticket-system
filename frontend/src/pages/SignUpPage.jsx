import { Grid, Stack } from "@mantine/core";
import { AuthHeader, AuthSwitchCard, SignInIcon, UserPlusIcon } from "../components/AuthShared.jsx";
import RegistrationForm from "../components/RegistrationForm.jsx";

export default function SignUpPage({ navigate }) {
  return (
    <Grid className="auth-shell" gutter="lg" align="flex-start">
      <Grid.Col span={{ base: 12, md: 7 }}>
        <Stack gap="md">
          <AuthHeader
            icon={<UserPlusIcon />}
            title="Create Account"
            description="Save your info for faster checkout and event updates."
          />

          <RegistrationForm />
        </Stack>
      </Grid.Col>

      <Grid.Col span={{ base: 12, md: 5 }}>
        <AuthSwitchCard
          icon={<SignInIcon />}
          title="Already have an account?"
          description="Sign in with your email and password."
          actionLabel="Sign in"
          onAction={() => navigate("/signin")}
        />
      </Grid.Col>
    </Grid>
  );
}
