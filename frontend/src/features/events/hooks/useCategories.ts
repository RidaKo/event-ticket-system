import { useQuery } from '@tanstack/react-query';
import { getCategories } from '../api/catalog.api';
import { catalogKeys } from '../api/events.keys';
import type { CategoryOption } from '../types';

export function useCategories() {
  return useQuery<CategoryOption[]>({
    queryKey: catalogKeys.categories(),
    queryFn: getCategories,
    staleTime: 5 * 60_000,
  });
}
