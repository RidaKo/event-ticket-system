export const eventKeys = {
  all: ['events'] as const,
  mine: () => [...eventKeys.all, 'mine'] as const,
};

export const catalogKeys = {
  all: ['catalog'] as const,
  categories: () => [...catalogKeys.all, 'categories'] as const,
};
