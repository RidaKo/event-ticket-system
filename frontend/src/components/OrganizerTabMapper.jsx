export default function organizerRouteToTab(routeName) {
    switch (routeName) {
        case "dashboard":
            return "dashboard";

        case "create-event":
            return "create-event";

        case "events":
            return "events";

        case "create-venue":
            return "create-venue";

        default:
            return "dashboard";
    }
}