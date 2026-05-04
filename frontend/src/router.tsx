import { createBrowserRouter, Navigate } from 'react-router-dom';
import { RootLayout } from '@/app/layouts/RootLayout';
import { AuthLayout } from '@/app/layouts/AuthLayout';
import { ProtectedRoute } from '@/app/guards/ProtectedRoute';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/features/auth';
import { PublishEventPage } from '@/pages/PublishEventPage';
import DiscoverPage from '@/pages/DiscoverPage.jsx';

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [{ path: '/login', element: <LoginPage /> }],
  },
  {
    // Public shell — discovery / recommendations live here so visitors can
    // browse upcoming events without logging in.
    element: <RootLayout />,
    children: [
      { index: true, element: <Navigate to="/discover" replace /> },
      { path: 'discover', element: <DiscoverPage /> },
      { path: 'publish', element: <PublishEventPage /> },
    ],
  },
  {
    // Auth-gated shell — anything that genuinely requires a session.
    element: <ProtectedRoute />,
    children: [
      {
        element: <RootLayout />,
        children: [{ path: 'home', element: <HomePage /> }],
      },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
]);
