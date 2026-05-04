import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username?: string; password?: string };
    if (body.username === 'ada' && body.password === 'correct') {
      return HttpResponse.json({
        id: 'u1',
        username: 'ada',
        roles: ['USER'],
      });
    }
    return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
  }),

  http.post('/api/auth/logout', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/auth/me', () =>
    HttpResponse.json({ message: 'Unauthenticated' }, { status: 401 }),
  ),
];
