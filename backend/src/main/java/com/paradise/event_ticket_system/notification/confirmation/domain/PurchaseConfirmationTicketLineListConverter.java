package com.paradise.event_ticket_system.notification.confirmation.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PurchaseConfirmationTicketLineListConverter
	implements AttributeConverter<List<PurchaseConfirmationTicketLine>, String> {

	@Override
	public String convertToDatabaseColumn(List<PurchaseConfirmationTicketLine> attribute) {
		List<PurchaseConfirmationTicketLine> ticketLines = attribute == null ? List.of() : attribute;
		return ticketLines.stream()
			.map(ticketLine -> escape(ticketLine.ticketType()) + "\t" + ticketLine.quantity())
			.reduce((left, right) -> left + "\n" + right)
			.orElse("");
	}

	@Override
	public List<PurchaseConfirmationTicketLine> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return List.of();
		}

		List<PurchaseConfirmationTicketLine> ticketLines = new ArrayList<>();
		for (String line : dbData.split("\n", -1)) {
			if (line.isEmpty()) {
				continue;
			}
			int separatorIndex = line.lastIndexOf('\t');
			if (separatorIndex < 0) {
				throw new IllegalStateException("Invalid purchase confirmation ticket line: " + line);
			}

			String ticketType = unescape(line.substring(0, separatorIndex));
			int quantity = Integer.parseInt(line.substring(separatorIndex + 1));
			ticketLines.add(new PurchaseConfirmationTicketLine(ticketType, quantity));
		}
		return ticketLines;
	}

	private String escape(String value) {
		return value
			.replace("\\", "\\\\")
			.replace("\t", "\\t")
			.replace("\n", "\\n");
	}

	private String unescape(String value) {
		StringBuilder builder = new StringBuilder();
		boolean escaping = false;

		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (escaping) {
				switch (current) {
					case 't' -> builder.append('\t');
					case 'n' -> builder.append('\n');
					case '\\' -> builder.append('\\');
					default -> builder.append(current);
				}
				escaping = false;
			}
			else if (current == '\\') {
				escaping = true;
			}
			else {
				builder.append(current);
			}
		}

		if (escaping) {
			builder.append('\\');
		}

		return builder.toString();
	}
}
