import { useQuery } from "@tanstack/react-query";
import { getRecommendedEvents } from "../api/recommendations.js";

/**
 * @param {import("../api/recommendations.js").RecommendationFilters} filters
 */
export function useRecommendedEvents(filters) {
  return useQuery({
    queryKey: ["recommendations", filters],
    queryFn: () => getRecommendedEvents(filters),
    keepPreviousData: true,
  });
}
