import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from '@/app/layouts/RootLayout';
import { HomePage } from '@/pages/HomePage';

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [{ index: true, element: <HomePage /> }],
  },
]);
