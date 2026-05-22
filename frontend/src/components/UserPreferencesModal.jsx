import {
  Alert,
  Button,
  Group,
  Modal,
  Pill,
  Stack,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import { useEffect, useState } from "react";
import { getCatalog } from "../api/catalog.js";
import { getMyPreferences, saveMyPreferences } from "../api/preferencesApi.js";

function normalizeSlug(value) {
  return String(value ?? "").trim().toLowerCase();
}

function categorySlugFromOption(category) {
  return normalizeSlug(category.value);
}

export default function UserPreferencesModal({ opened, onClose, onSaved }) {
  const [catalog, setCatalog] = useState({ categories: [], tags: [] });
  const [categorySlugs, setCategorySlugs] = useState([]);
  const [tagSlugs, setTagSlugs] = useState([]);
  const [homeCity, setHomeCity] = useState("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!opened) {
      return undefined;
    }

    let cancelled = false;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const [catalogData, preferences] = await Promise.all([getCatalog(), getMyPreferences()]);
        if (cancelled) {
          return;
        }
        setCatalog(catalogData);
        setCategorySlugs(preferences.categorySlugs ?? []);
        setTagSlugs(preferences.tagSlugs ?? []);
        setHomeCity(preferences.homeCity ?? "");
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "Could not load preferences");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [opened]);

  function toggleCategory(slug) {
    setCategorySlugs((current) =>
      current.includes(slug) ? current.filter((value) => value !== slug) : [...current, slug]
    );
  }

  function toggleTag(slug) {
    setTagSlugs((current) =>
      current.includes(slug) ? current.filter((value) => value !== slug) : [...current, slug]
    );
  }

  async function handleSave() {
    setSaving(true);
    setError("");
    try {
      await saveMyPreferences({
        categorySlugs,
        tagSlugs,
        homeCity,
      });
      onSaved?.();
      onClose();
    } catch (err) {
      setError(err.message || "Could not save preferences");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title="Your recommendation preferences"
      size="lg"
      centered
      classNames={{ content: "preferences-modal" }}
    >
      <Stack gap="md">
        <Text size="sm" c="dimmed">
          These choices personalize the &quot;Recommended for you&quot; section. Browse filters on the left are
          separate and apply to the full list.
        </Text>

        <Stack gap="xs">
          <Title order={4} size="h5" c="brand.9">
            Favorite categories
          </Title>
          <Group gap="xs">
            {(catalog.categories ?? []).map((category) => {
              const slug = categorySlugFromOption(category);
              return (
                <Pill
                  key={slug}
                  size="sm"
                  style={{ cursor: loading ? "default" : "pointer" }}
                  onClick={() => !loading && toggleCategory(slug)}
                  bg={categorySlugs.includes(slug) ? "var(--mantine-color-brand-1)" : undefined}
                >
                  {category.label}
                </Pill>
              );
            })}
          </Group>
        </Stack>

        <Stack gap="xs">
          <Title order={4} size="h5" c="brand.9">
            Interest tags
          </Title>
          <Group gap="xs">
            {(catalog.tags ?? []).map((tag) => {
              const slug = normalizeSlug(tag.slug);
              return (
                <Pill
                  key={slug}
                  size="sm"
                  style={{ cursor: loading ? "default" : "pointer" }}
                  onClick={() => !loading && toggleTag(slug)}
                  bg={tagSlugs.includes(slug) ? "var(--mantine-color-brand-1)" : undefined}
                >
                  {tag.label}
                </Pill>
              );
            })}
          </Group>
        </Stack>

        <TextInput
          label="Home city"
          description="Events in this city score higher in recommendations"
          placeholder="e.g. Vilnius"
          value={homeCity}
          onChange={(event) => setHomeCity(event.currentTarget.value)}
          disabled={loading}
        />

        {error && (
          <Alert color="red" variant="light">
            {error}
          </Alert>
        )}

        <Group justify="flex-end" gap="sm">
          <Button variant="default" color="gray" onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button color="brand" onClick={handleSave} loading={saving} disabled={loading}>
            Save preferences
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
}
