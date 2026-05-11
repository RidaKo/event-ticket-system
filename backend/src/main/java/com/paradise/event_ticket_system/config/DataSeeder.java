package com.paradise.event_ticket_system.config;

import com.paradise.event_ticket_system.viewEvent.domain.CategoryRepository;
import com.paradise.event_ticket_system.viewEvent.domain.EventRepository;
import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.event.TagRepository;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.user.UserPreferences;
import com.paradise.event_ticket_system.user.UserPreferencesRepository;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Seeds a demo user, organizer, venues, tags, events and preferences on first
 * boot so the discover/recommendations page has something to render.
 *
 * Categories are seeded via Flyway (V2) so they exist by the time we reference
 * them here.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final String DEFAULT_TIMEZONE = DEFAULT_ZONE.getId();
    private static final String COUNTRY_LT = "LT";

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final EntityManager entityManager;
    private final DemoUserProvider demoUserProvider;

    public DataSeeder(UserRepository userRepository,
                      UserPreferencesRepository preferencesRepository,
                      TagRepository tagRepository,
                      CategoryRepository categoryRepository,
                      EventRepository eventRepository,
                      EntityManager entityManager,
                      DemoUserProvider demoUserProvider) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
        this.entityManager = entityManager;
        this.demoUserProvider = demoUserProvider;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            userRepository.findAll().stream().findFirst()
                    .ifPresent(u -> demoUserProvider.setDemoUserId(u.getId()));
            return;
        }

        Map<String, Category> categories = loadCategoriesBySlug();
        Map<String, Tag> tags = seedTags();
        User demo = seedDemoUser();
        demoUserProvider.setDemoUserId(demo.getId());
        Organizer organizer = seedOrganizer(demo);
        seedPreferences(demo, categories, tags);
        seedEvents(organizer, categories, tags);
    }

    private Map<String, Category> loadCategoriesBySlug() {
        Map<String, Category> map = new HashMap<>();
        for (Category c : categoryRepository.findAll()) {
            map.put(c.getSlug(), c);
        }
        return map;
    }

    private Map<String, Tag> seedTags() {
        Map<String, Tag> tags = new LinkedHashMap<>();
        tags.put("outdoor", new Tag("outdoor", "Outdoor"));
        tags.put("family", new Tag("family", "Family"));
        tags.put("networking", new Tag("networking", "Networking"));
        tags.put("educational", new Tag("educational", "Educational"));
        tagRepository.saveAll(tags.values());
        return tags;
    }

    private User seedDemoUser() {
        User user = new User();
        user.setEmail("alex@demo.local");
        user.setFullName("Alex Demo");
        user.setIsGuest(false);
        return userRepository.save(user);
    }

    private Organizer seedOrganizer(User demo) {
        Organizer organizer = new Organizer();
        organizer.setUser(demo);
        organizer.setBusinessName("Paradise Events");
        organizer.setVerified(true);
        entityManager.persist(organizer);
        return organizer;
    }

    private void seedPreferences(User user, Map<String, Category> categories, Map<String, Tag> tags) {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setPreferredCategories(new HashSet<>(Set.of(
                categories.get("music"),
                categories.get("technology")
        )));
        prefs.setPreferredTags(new HashSet<>(Set.of(
                tags.get("outdoor"),
                tags.get("networking")
        )));
        prefs.setHomeCity("Vilnius");
        preferencesRepository.save(prefs);
    }

    private void seedEvents(Organizer organizer, Map<String, Category> categories, Map<String, Tag> tags) {
        LocalDateTime base = LocalDateTime.now().withHour(19).withMinute(0).withSecond(0).withNano(0);
        Map<String, Venue> venues = new HashMap<>();

        create(organizer, categories.get("music"), venues,
                "Jazz Night at Loftas",
                "An intimate evening with local and touring jazz quartets.",
                base.plusDays(3), "Vilnius", "Loftas",
                Set.of(tags.get("networking")));

        create(organizer, categories.get("music"), venues,
                "Open-Air Rock Festival",
                "Three stages, a dozen bands, one unforgettable night under the stars.",
                base.plusDays(8), "Kaunas", "Santaka Park",
                Set.of(tags.get("outdoor"), tags.get("family")));

        create(organizer, categories.get("music"), venues,
                "Chamber Music Sunday",
                "A cozy afternoon of Bach, Mozart and Ravel performed by the city quartet.",
                base.plusDays(21), "Vilnius", "Philharmonic Hall",
                Set.of(tags.get("family")));

        create(organizer, categories.get("sports"), venues,
                "City Marathon 2026",
                "Join 8,000 runners across the city's most scenic route.",
                base.plusDays(10), "Vilnius", "City Centre",
                Set.of(tags.get("outdoor"), tags.get("family")));

        create(organizer, categories.get("sports"), venues,
                "Basketball Derby Night",
                "The rivalry continues — Žalgiris vs. Rytas at full capacity.",
                base.plusDays(5), "Kaunas", "Žalgirio Arena",
                Set.of());

        create(organizer, categories.get("sports"), venues,
                "Sunrise Trail Run",
                "A guided 10k trail run through the forest at dawn.",
                base.plusDays(14), "Trakai", "Trakai Forest",
                Set.of(tags.get("outdoor")));

        create(organizer, categories.get("arts"), venues,
                "Contemporary Art Opening",
                "Opening night of the new season's contemporary art exhibition.",
                base.plusDays(2), "Vilnius", "MO Museum",
                Set.of(tags.get("networking"), tags.get("educational")));

        create(organizer, categories.get("arts"), venues,
                "Street Art Walking Tour",
                "A guided stroll through the city's most striking murals and installations.",
                base.plusDays(12), "Vilnius", "Uzupis District",
                Set.of(tags.get("outdoor"), tags.get("educational")));

        create(organizer, categories.get("arts"), venues,
                "Pottery Workshop for Beginners",
                "Hands-on introduction to the wheel — clay and coffee included.",
                base.plusDays(18), "Kaunas", "Craft House",
                Set.of(tags.get("family"), tags.get("educational")));

        create(organizer, categories.get("technology"), venues,
                "AI Builders Meetup",
                "Lightning talks and demos from local AI engineers, followed by a networking mixer.",
                base.plusDays(4), "Vilnius", "Tech Park",
                Set.of(tags.get("networking"), tags.get("educational")));

        create(organizer, categories.get("technology"), venues,
                "DevOps Summit 2026",
                "A full day of talks on platform engineering, SRE and observability.",
                base.plusDays(25), "Vilnius", "Radisson Blu",
                Set.of(tags.get("networking"), tags.get("educational")));

        create(organizer, categories.get("technology"), venues,
                "Startup Pitch Night",
                "Ten early-stage startups pitch to a panel of local investors.",
                base.plusDays(9), "Kaunas", "KTU Startup Space",
                Set.of(tags.get("networking")));

        create(organizer, categories.get("food"), venues,
                "Street Food Festival",
                "Food trucks, craft beer and live music in the old town square.",
                base.plusDays(6), "Vilnius", "Cathedral Square",
                Set.of(tags.get("outdoor"), tags.get("family")));

        create(organizer, categories.get("food"), venues,
                "Pasta Masterclass",
                "Learn to make three classic handmade pastas from scratch.",
                base.plusDays(15), "Vilnius", "Culinary Studio",
                Set.of(tags.get("educational"), tags.get("family")));

        create(organizer, categories.get("food"), venues,
                "Wine Tasting Evening",
                "A sommelier-led tasting across six regions of Italy.",
                base.plusDays(20), "Klaipeda", "Old Cellar",
                Set.of(tags.get("networking")));
    }

    private void create(Organizer organizer,
                        Category category,
                        Map<String, Venue> venues,
                        String title,
                        String description,
                        LocalDateTime startAt,
                        String city,
                        String venueName,
                        Set<Tag> eventTags) {
        Venue venue = venues.computeIfAbsent(venueKey(city, venueName),
                key -> createVenue(organizer, city, venueName));

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategory(category);
        event.setTitle(title);
        event.setSlug(slugify(title));
        event.setDescription(description);
        event.setStatus(EventStatus.PUBLISHED.name());
        event.setStartDatetime(startAt.atZone(DEFAULT_ZONE).toInstant());
        event.setEndDatetime(startAt.plusHours(3).atZone(DEFAULT_ZONE).toInstant());
        event.setTimezone(DEFAULT_TIMEZONE);
        event.setTags(eventTags == null ? new HashSet<>() : new HashSet<>(eventTags));
        eventRepository.save(event);
    }

    private Venue createVenue(Organizer organizer, String city, String name) {
        Venue venue = new Venue();
        venue.setOrganizer(organizer);
        venue.setName(name);
        venue.setAddressLine1(name);
        venue.setCity(city);
        venue.setCountry(COUNTRY_LT);
        entityManager.persist(venue);
        return venue;
    }

    private static String venueKey(String city, String name) {
        return city.toLowerCase(Locale.ROOT) + "|" + name.toLowerCase(Locale.ROOT);
    }

    private static String slugify(String input) {
        return input
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
