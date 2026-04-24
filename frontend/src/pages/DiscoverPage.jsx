import { useState } from "react";
import PreferencesFilter from "../components/discover/PreferencesFilter.jsx";
import RecommendedForYou from "../components/discover/RecommendedForYou.jsx";
import BrowseEvents from "../components/discover/BrowseEvents.jsx";
import { useCatalog } from "../hooks/useCatalog.js";
import { useRecommendedEvents } from "../hooks/useRecommendedEvents.js";
import { buildCategoryLabelMap, EMPTY_FILTERS } from "../lib/catalog.js";

const RECOMMENDED_COUNT = 6;
const TOTAL_FETCH_LIMIT = 20;

export default function DiscoverPage() {
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const catalog = useCatalog();

  const categories = catalog.data?.categories ?? [];
  const tags = catalog.data?.tags ?? [];
  const categoryLabelMap = buildCategoryLabelMap(categories);

  const events = useRecommendedEvents({ ...filters, limit: TOTAL_FETCH_LIMIT });

  const allItems = events.data?.items ?? [];
  const recommendedItems = allItems.slice(0, RECOMMENDED_COUNT);
  const browseItems = allItems.slice(RECOMMENDED_COUNT);

  const showBrowseAllAboveHint =
    !events.isLoading &&
    !events.isError &&
    browseItems.length === 0 &&
    recommendedItems.length > 0;

  function handleReset() {
    setFilters(EMPTY_FILTERS);
  }

  return (
    <div className="grid grid-cols-1 gap-8 lg:grid-cols-[280px_minmax(0,1fr)]">
      <PreferencesFilter
        value={filters}
        onApply={setFilters}
        onReset={handleReset}
        categories={categories}
        tags={tags}
        isCatalogLoading={catalog.isLoading}
        isCatalogError={catalog.isError}
      />

      <div>
        <RecommendedForYou
          items={recommendedItems}
          isLoading={events.isLoading}
          isError={events.isError}
          fallbackUsed={events.data?.fallbackUsed ?? false}
          onReset={handleReset}
          categoryLabelMap={categoryLabelMap}
        />

        {showBrowseAllAboveHint ? (
          <p className="mt-10 text-center text-sm text-ink-muted">
            All matching events are shown above.
          </p>
        ) : (
          <BrowseEvents
            items={browseItems}
            isLoading={events.isLoading}
            isError={events.isError}
          />
        )}
      </div>
    </div>
  );
}
