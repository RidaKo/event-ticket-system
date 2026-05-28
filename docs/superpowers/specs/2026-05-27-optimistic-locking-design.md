# Optimistic Locking — Design Spec

**Date:** 2026-05-27  
**Scope:** `Event` and `UserPreferences` entities — the only two with real update endpoints  
**Approach:** Version field in JSON body (Option A)

---

## Problem

Two users (or one user in two tabs) can edit the same `Event` or `UserPreferences` record simultaneously. The last write silently wins, discarding the other user's change with no warning.

---

## Goals

- Detect when a client submits a change based on stale data
- Return `409 Conflict` with a clear message
- Let the user choose: **Refresh data** (reload latest, re-decide) or **Force overwrite** (re-fetch current version, resubmit their change on top)

---

## Backend

### 1. Entities — add `@Version`

**`Event.java`** (`model/Event.java`):
```java
@Version
@Column(name = "row_version", nullable = false)
private Long version;
```

**`UserPreferences.java`** (`user/UserPreferences.java`):
```java
@Version
@Column(name = "row_version", nullable = false)
private Long version;
```

Follows the exact pattern already used in `TicketType.java` (line 69–71).

---

### 2. Database migrations

**`V11__add_event_version.sql`**:
```sql
ALTER TABLE events
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
```

**`V12__add_user_preferences_version.sql`**:
```sql
ALTER TABLE user_preferences
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
```

---

### 3. Response DTOs — expose `version`

**`EventResponse.java`** — add `Long version` field to the record.  
**`EventMapper.toEventResponse()`** — pass `event.getVersion()` in the constructor call.

**`UserPreferencesResponse.java`** — add `Long version` field to the record.  
**`UserPreferencesResponse.from()`** — pass `preferences.getVersion()`.  
**`UserPreferencesResponse.empty()`** — pass `null` (no existing record → no version → first save is a create, version check is skipped).

---

### 4. Request DTOs — accept `version` from client

**`UpdateEventStatusRequest.java`**:
```java
public record UpdateEventStatusRequest(EventStatus status, Long version) {}
```

**`UpdateUserPreferencesRequest.java`**:
```java
public record UpdateUserPreferencesRequest(
    List<@Size(max = 64) String> categorySlugs,
    List<@Size(max = 64) String> tagSlugs,
    @Size(max = 255) String homeCity,
    Long version
) {}
```

---

### 5. Service layer — version check before update

**`EventService.updateStatus()`**:

1. Load `Event` from DB (`findById`) — this gives us the current DB version.
2. If `request.version() != null` and `!request.version().equals(event.getVersion())` → throw `ResponseStatusException(HttpStatus.CONFLICT, "This event was modified by someone else. Refresh and try again.")`.
3. Apply status change and save. JPA's `@Version` auto-increments on flush and acts as DB-level safety net for true races.

**`UserPreferencesService.saveForEmail()`**:

1. Load or create preferences (existing upsert logic unchanged).
2. If `preferences` was loaded from DB (not freshly created) AND `request.version() != null` AND `!request.version().equals(preferences.getVersion())` → throw `ResponseStatusException(HttpStatus.CONFLICT, "Your preferences were modified elsewhere. Refresh and try again.")`.
3. Apply changes and save.

Version check is **skipped** when `request.version()` is `null` (backwards-compatible) or when preferences are being created for the first time.

---

### 6. Global exception handler — safety net

New file: **`config/GlobalExceptionHandler.java`**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return Map.of("message", "This record was modified by someone else. Refresh and try again.");
    }
}
```

Catches `ObjectOptimisticLockingFailureException` (Spring's wrapper for JPA's `OptimisticLockException`) for any races that slip past the manual check (e.g. two requests checking version simultaneously before either commits).

---

## Frontend

### 1. `client.js` — attach HTTP status to thrown errors

Minimal change — no new classes, no change to existing error handling elsewhere:

```js
// after: const body = await response.json();
const err = new Error(message);
err.status = response.status;   // ← add this line
throw err;
```

Call sites check `err.status === 409`. All other error paths remain unchanged.

---

### 2. `ConflictDialog.jsx` — new shared component

Located at `src/components/ConflictDialog.jsx`.

A Mantine `Modal` (same as `UserPreferencesModal`) with:
- Title: `"Edit conflict"`
- Body text: `"Someone else modified this record while you were editing. What would you like to do?"`
- Two buttons in a `Group justify="flex-end"`:
  - `<Button variant="default">Refresh data</Button>` → calls `onRefresh`
  - `<Button color="brand">Force overwrite</Button>` → calls `onOverwrite`

Props: `opened`, `onClose`, `onRefresh`, `onOverwrite`.

---

### 3. `eventsApi.js` — pass version in status update

```js
export function updateEventStatus(organizerId, eventId, status, version) {
  return apiFetch(`/organizers/${organizerId}/events/${eventId}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status, version }),
  });
}
```

---

### 4. `OrganizerEventsPage.jsx`

**State additions:**
```js
const [conflictInfo, setConflictInfo] = useState(null);
// conflictInfo shape: { eventId, intendedStatus } | null
```

**`handleStatusChange(eventId, status, version)`** — add `version` parameter:
- Optimistically updates local state (existing behaviour).
- Calls `updateEventStatus(organizerId, eventId, status, version)`.
- On `err.status === 409`: `setConflictInfo({ eventId, intendedStatus: status })` (opens dialog).
- On other errors: `console.error` (existing behaviour).

**`ConflictDialog` wired up:**
- `opened={conflictInfo !== null}`
- `onClose={() => setConflictInfo(null)}`
- **`onRefresh`**: calls `loadEvents()` (existing function), then `setConflictInfo(null)`.
- **`onOverwrite`**: calls `getEvent(conflictInfo.eventId)` to get fresh version, then calls `updateEventStatus(organizerId, conflictInfo.eventId, conflictInfo.intendedStatus, freshEvent.version)`, then `setConflictInfo(null)`. On error: falls back to `loadEvents()`.

**`events` state** already receives `EventResponse` objects which will include `version` once the DTO is updated — no structural change to state needed.

---

### 5. `preferencesApi.js` — pass version

```js
export async function saveMyPreferences(preferences) {
  return apiFetch("/users/me/preferences", {
    method: "PUT",
    body: JSON.stringify({
      categorySlugs: preferences.categorySlugs ?? [],
      tagSlugs: preferences.tagSlugs ?? [],
      homeCity: preferences.homeCity?.trim() || null,
      version: preferences.version ?? null,   // ← add
    }),
  });
}
```

---

### 6. `UserPreferencesModal.jsx`

**State additions:**
```js
const [version, setVersion] = useState(null);
const [conflictOpened, setConflictOpened] = useState(false);
```

**`load()` function** — after loading preferences, set version:
```js
setVersion(preferences.version ?? null);
```

**`handleSave()`** — pass version:
```js
await saveMyPreferences({ categorySlugs, tagSlugs, homeCity, version });
```
On `err.status === 409`: `setConflictOpened(true)` instead of `setError(...)`.

**`ConflictDialog` wired up:**
- `opened={conflictOpened}`
- `onClose={() => setConflictOpened(false)}`
- **`onRefresh`**: re-runs `load()` (refreshes form state + version from server), closes dialog.
- **`onOverwrite`**: calls `getMyPreferences()` to get current version, calls `saveMyPreferences({ categorySlugs, tagSlugs, homeCity, version: freshPrefs.version })`, calls `onSaved?.()` and `onClose()` on success.

---

## Data Flow Summary

```
Client loads Event/Preferences
  → response includes { ..., version: 5 }
  → client stores version in state

Client submits change
  → sends { status/fields..., version: 5 }
  → server: load entity (DB version = 5)
  → version match → apply change → save → DB version becomes 6
  → response: 200 OK

Concurrent edit scenario:
  → User B committed first → DB version is now 6
  → User A sends { ..., version: 5 }
  → server: load entity (DB version = 6) → mismatch → 409 Conflict
  → frontend: ConflictDialog opens
    → "Refresh data": reload from server, user sees version 6
    → "Force overwrite": fetch version 6, resubmit with version: 6 → success
```

---

## Pre-existing URL discrepancy (fix included)

The frontend calls `PATCH /api/organizers/{organizerId}/events/{eventId}/status`, but
the only existing PATCH mapping lives in `EventController` under `/api/events/...`, producing
a different path that never matches. The status-update endpoint has therefore never worked
end-to-end. This implementation fixes it by adding the PATCH to `OrganizerController`
(which is already at `/api/organizers`) and removing the stale mapping from `EventController`.

---

## Files Changed

| File | Change |
|------|--------|
| `model/Event.java` | Add `@Version Long version` |
| `user/UserPreferences.java` | Add `@Version Long version` |
| `db/migration/V11__add_event_version.sql` | New migration |
| `db/migration/V12__add_user_preferences_version.sql` | New migration |
| `viewEvent/api/DTO/EventResponse.java` | Add `Long version` field |
| `viewEvent/api/EventMapper.java` | Pass `event.getVersion()` |
| `viewEvent/api/DTO/UpdateEventStatusRequest.java` | Add `Long version` field |
| `organizer/OrganizerController.java` | Add `PATCH /{organizerId}/events/{eventId}/status` (correct path); remove stale PATCH from `EventController` |
| `viewEvent/api/EventController.java` | Remove stale `updateStatus` PATCH mapping |
| `viewEvent/service/EventService.java` | Version check in `updateStatus()` |
| `user/dto/UserPreferencesResponse.java` | Add `Long version` field + update factory methods |
| `user/dto/UpdateUserPreferencesRequest.java` | Add `Long version` field |
| `user/UserPreferencesService.java` | Version check in `saveForEmail()` |
| `config/GlobalExceptionHandler.java` | New — catches `ObjectOptimisticLockingFailureException` |
| `frontend/src/api/client.js` | Attach `err.status` to thrown errors |
| `frontend/src/api/eventsApi.js` | Add `version` param to `updateEventStatus` |
| `frontend/src/api/preferencesApi.js` | Add `version` to request body |
| `frontend/src/components/ConflictDialog.jsx` | New shared Mantine Modal component |
| `frontend/src/pages/Organizer/OrganizerEventsPage.jsx` | Import `getEvent`; `conflictInfo` state; dialog wiring |
| `frontend/src/components/UserPreferencesModal.jsx` | `version` + `conflictOpened` state; dialog wiring |
