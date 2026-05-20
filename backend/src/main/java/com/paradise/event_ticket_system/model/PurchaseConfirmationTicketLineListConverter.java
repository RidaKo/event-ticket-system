package com.paradise.event_ticket_system.model;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PurchaseConfirmationTicketLineListConverter
	implements AttributeConverter<List<PurchaseConfirmationTicketLine>, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<List<PurchaseConfirmationTicketLine>> TICKET_LINES_TYPE =
		new TypeReference<>() {
		};

	@Override
	public String convertToDatabaseColumn(List<PurchaseConfirmationTicketLine> attribute) {
		List<PurchaseConfirmationTicketLine> ticketLines = attribute == null ? List.of() : attribute;
		try {
			return OBJECT_MAPPER.writeValueAsString(ticketLines);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Unable to serialize purchase confirmation ticket lines", ex);
		}
	}

	@Override
	public List<PurchaseConfirmationTicketLine> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return List.of();
		}

		try {
			return OBJECT_MAPPER.readValue(dbData, TICKET_LINES_TYPE);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to deserialize purchase confirmation ticket lines", ex);
		}
	}
}
