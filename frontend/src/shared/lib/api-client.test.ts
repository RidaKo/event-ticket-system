import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './api-client';
import { ApiError } from './api-error';

describe('apiClient', () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    fetchMock.mockReset();
    vi.unstubAllGlobals();
  });

  it('prefixes paths with VITE_API_BASE_URL and includes credentials', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    );

    await apiClient.get('/auth/me');

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/auth/me');
    expect(init.credentials).toBe('include');
    expect(init.method).toBe('GET');
  });

  it('parses JSON responses', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ id: 1, name: 'Ada' }), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    );

    const data = await apiClient.get<{ id: number; name: string }>('/user');

    expect(data).toEqual({ id: 1, name: 'Ada' });
  });

  it('returns undefined for 204 No Content', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const data = await apiClient.post('/auth/logout');

    expect(data).toBeUndefined();
  });

  it('throws ApiError on non-2xx with parsed JSON body', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ message: 'Invalid credentials' }), {
        status: 401,
        headers: { 'content-type': 'application/json' },
      }),
    );

    await expect(apiClient.post('/auth/login', { u: 'x' })).rejects.toMatchObject({
      status: 401,
      message: 'Invalid credentials',
    });
  });

  it('throws ApiError on non-2xx with non-JSON body', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response('Server down', {
        status: 500,
        headers: { 'content-type': 'text/plain' },
      }),
    );

    const rejection = apiClient.get('/anything');
    await expect(rejection).rejects.toBeInstanceOf(ApiError);
    await expect(rejection).rejects.toMatchObject({ status: 500 });
  });

  it('sends JSON body on POST', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    await apiClient.post('/auth/login', { username: 'ada', password: 'pw' });

    const [, init] = fetchMock.mock.calls[0];
    expect(init.method).toBe('POST');
    expect(init.headers).toMatchObject({ 'content-type': 'application/json' });
    expect(JSON.parse(init.body as string)).toEqual({ username: 'ada', password: 'pw' });
  });

  it('omits body and content-type when post is called without a body', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    await apiClient.post('/auth/logout');

    const [, init] = fetchMock.mock.calls[0];
    expect(init.method).toBe('POST');
    expect(init.body).toBeUndefined();
    expect(init.headers).toEqual({});
  });
});
