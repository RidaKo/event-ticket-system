package com.paradise.event_ticket_system.config;

import com.paradise.event_ticket_system.event.Category;
import com.paradise.event_ticket_system.event.Event;
import com.paradise.event_ticket_system.event.EventRepository;
import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.event.TagRepository;
import com.paradise.event_ticket_system.user.User;
import com.paradise.event_ticket_system.user.UserPreferences;
import com.paradise.event_ticket_system.user.UserPreferencesRepository;
import com.paradise.event_ticket_system.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final TagRepository tagRepository;
    private final EventRepository eventRepository;
    private final DemoUserProvider demoUserProvider;

    public DataSeeder(UserRepository userRepository,
                      UserPreferencesRepository preferencesRepository,
                      TagRepository tagRepository,
                      EventRepository eventRepository,
                      DemoUserProvider demoUserProvider) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.tagRepository = tagRepository;
        this.eventRepository = eventRepository;
        this.demoUserProvider = demoUserProvider;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            userRepository.findAll().stream().findFirst()
                    .ifPresent(u -> demoUserProvider.setDemoUserId(u.getId()));
            return;
        }

        Map<String, Tag> tags = seedTags();
        User demo = seedDemoUser();
        demoUserProvider.setDemoUserId(demo.getId());
        seedPreferences(demo, tags);
        seedEvents(tags);
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
        User user = new User("Alex Demo", "alex@demo.local");
        return userRepository.save(user);
    }

    private void seedPreferences(User user, Map<String, Tag> tags) {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setPreferredCategories(new HashSet<>(Set.of(Category.MUSIC, Category.TECHNOLOGY)));
        prefs.setPreferredTags(new HashSet<>(Set.of(tags.get("outdoor"), tags.get("networking"))));
        prefs.setHomeCity("Vilnius");
        preferencesRepository.save(prefs);
    }

    private void seedEvents(Map<String, Tag> tags) {
        LocalDateTime base = LocalDateTime.now().withHour(19).withMinute(0).withSecond(0).withNano(0);

        create("Jazz Night at Loftas",
                "An intimate evening with local and touring jazz quartets.",
                base.plusDays(3), "Vilnius", "Loftas",
                Category.MUSIC, Set.of(tags.get("networking")));

        create("Open-Air Rock Festival",
                "Three stages, a dozen bands, one unforgettable night under the stars.",
                base.plusDays(8), "Kaunas", "Santaka Park",
                Category.MUSIC, Set.of(tags.get("outdoor"), tags.get("family")));

        create("Chamber Music Sunday",
                "A cozy afternoon of Bach, Mozart and Ravel performed by the city quartet.",
                base.plusDays(21), "Vilnius", "Philharmonic Hall",
                Category.MUSIC, Set.of(tags.get("family")));

        create("City Marathon 2026",
                "Join 8,000 runners across the city's most scenic route.",
                base.plusDays(10), "Vilnius", "City Centre",
                Category.SPORTS, Set.of(tags.get("outdoor"), tags.get("family")));

        create("Basketball Derby Night",
                "The rivalry continues — Žalgiris vs. Rytas at full capacity.",
                base.plusDays(5), "Kaunas", "Žalgirio Arena",
                Category.SPORTS, Set.of());

        create("Sunrise Trail Run",
                "A guided 10k trail run through the forest at dawn.",
                base.plusDays(14), "Trakai", "Trakai Forest",
                Category.SPORTS, Set.of(tags.get("outdoor")));

        create("Contemporary Art Opening",
                "Opening night of the new season's contemporary art exhibition.",
                base.plusDays(2), "Vilnius", "MO Museum",
                Category.ARTS, Set.of(tags.get("networking"), tags.get("educational")));

        create("Street Art Walking Tour",
                "A guided stroll through the city's most striking murals and installations.",
                base.plusDays(12), "Vilnius", "Uzupis District",
                Category.ARTS, Set.of(tags.get("outdoor"), tags.get("educational")));

        create("Pottery Workshop for Beginners",
                "Hands-on introduction to the wheel — clay and coffee included.",
                base.plusDays(18), "Kaunas", "Craft House",
                Category.ARTS, Set.of(tags.get("family"), tags.get("educational")));

        create("AI Builders Meetup",
                "Lightning talks and demos from local AI engineers, followed by a networking mixer.",
                base.plusDays(4), "Vilnius", "Tech Park",
                Category.TECHNOLOGY, Set.of(tags.get("networking"), tags.get("educational")));

        create("DevOps Summit 2026",
                "A full day of talks on platform engineering, SRE and observability.",
                base.plusDays(25), "Vilnius", "Radisson Blu",
                Category.TECHNOLOGY, Set.of(tags.get("networking"), tags.get("educational")));

        create("Startup Pitch Night",
                "Ten early-stage startups pitch to a panel of local investors.",
                base.plusDays(9), "Kaunas", "KTU Startup Space",
                Category.TECHNOLOGY, Set.of(tags.get("networking")));

        create("Street Food Festival",
                "Food trucks, craft beer and live music in the old town square.",
                base.plusDays(6), "Vilnius", "Cathedral Square",
                Category.FOOD, Set.of(tags.get("outdoor"), tags.get("family")));

        create("Pasta Masterclass",
                "Learn to make three classic handmade pastas from scratch.",
                base.plusDays(15), "Vilnius", "Culinary Studio",
                Category.FOOD, Set.of(tags.get("educational"), tags.get("family")));

        create("Wine Tasting Evening",
                "A sommelier-led tasting across six regions of Italy.",
                base.plusDays(20), "Klaipeda", "Old Cellar",
                Category.FOOD, Set.of(tags.get("networking")));
    }

    private void create(String title, String description, LocalDateTime startAt,
                        String city, String venue, Category category,
                        Set<Tag> eventTags) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(description);
        e.setStartAt(startAt);
        e.setEndAt(startAt.plusHours(3));
        e.setCity(city);
        e.setVenue(venue);
        e.setCategory(category);
        e.setStatus(EventStatus.PUBLISHED);
        e.setCreatedAt(LocalDateTime.now());
        e.setTags(eventTags == null ? new HashSet<>() : new HashSet<>(eventTags));
        eventRepository.save(e);
    }
}
