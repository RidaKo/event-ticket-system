import { apiClient } from '@/shared/lib/api-client';
import type { CategoryOption } from '../types';

export function getCategories(): Promise<CategoryOption[]> {
  return apiClient.get<CategoryOption[]>('/categories');
}
