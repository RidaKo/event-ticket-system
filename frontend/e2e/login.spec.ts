import { test, expect } from '@playwright/test';

test('successful login lands on the home page', async ({ page }) => {
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ id: 'u1', username: 'ada', roles: ['USER'] }),
    });
  });
  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ id: 'u1', username: 'ada', roles: ['USER'] }),
    });
  });

  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);

  await page.getByLabel(/username/i).fill('ada');
  await page.getByLabel(/password/i).fill('correct');
  await page.getByRole('button', { name: /sign in/i }).click();

  await expect(page).toHaveURL('/');
  await expect(page.getByRole('heading', { name: /welcome/i })).toBeVisible();
});

test('failed login shows server error', async ({ page }) => {
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Invalid credentials' }),
    });
  });

  await page.goto('/login');
  await page.getByLabel(/username/i).fill('ada');
  await page.getByLabel(/password/i).fill('wrong');
  await page.getByRole('button', { name: /sign in/i }).click();

  await expect(page.getByText(/invalid credentials/i)).toBeVisible();
});
