package com.paradise.event_ticket_system.checkout;

import java.util.List;

public record UserOrdersResponse(
        List<UserOrderListItemResponse> items
) {
}
