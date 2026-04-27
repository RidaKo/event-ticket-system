package com.paradise.event_ticket_system.notification.confirmation.domain;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "purchase_confirmation_deliveries",
	uniqueConstraints = @UniqueConstraint(name = "uk_purchase_confirmation_order_id", columnNames = "order_id")
)
public class PurchaseConfirmationDelivery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_id", nullable = false, updatable = false, length = 100)
	private String orderId;

	@Column(name = "order_reference", nullable = false, updatable = false, length = 100)
	private String orderReference;

	@Column(name = "attendee_email", nullable = false, updatable = false, length = 320)
	private String attendeeEmail;

	@Column(name = "event_title", nullable = false, updatable = false)
	private String eventTitle;

	@Column(name = "event_date_time", nullable = false, updatable = false)
	private OffsetDateTime eventDateTime;

	@Column(name = "event_location", nullable = false, updatable = false)
	private String eventLocation;

	@Column(name = "ticket_type", nullable = false, updatable = false)
	private String ticketType;

	@Column(name = "ticket_quantity", nullable = false, updatable = false)
	private int quantity;

	@Column(name = "ticket_url", length = 2048, updatable = false)
	private String ticketUrl;

	@Column(name = "qr_code_image_url", length = 2048, updatable = false)
	private String qrCodeImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmailDeliveryStatus status;

	@Column(name = "failure_reason", length = 1000)
	private String failureReason;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected PurchaseConfirmationDelivery() {
	}

	private PurchaseConfirmationDelivery(PurchaseConfirmationRequest request) {
		this.orderId = request.orderId();
		this.orderReference = request.orderReference();
		this.attendeeEmail = request.attendeeEmail();
		this.eventTitle = request.eventTitle();
		this.eventDateTime = request.eventDateTime();
		this.eventLocation = request.eventLocation();
		this.ticketType = request.ticketType();
		this.quantity = request.quantity();
		this.ticketUrl = request.ticketUrl();
		this.qrCodeImageUrl = request.qrCodeImageUrl();
		this.status = EmailDeliveryStatus.PENDING;
	}

	public static PurchaseConfirmationDelivery pending(PurchaseConfirmationRequest request) {
		return new PurchaseConfirmationDelivery(request);
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void markSent() {
		this.status = EmailDeliveryStatus.SENT;
		this.sentAt = Instant.now();
		this.failureReason = null;
	}

	public void markFailed(String failureReason) {
		this.status = EmailDeliveryStatus.FAILED;
		this.failureReason = failureReason == null ? "Unknown email delivery failure" : failureReason;
	}

	public Long getId() {
		return id;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getOrderReference() {
		return orderReference;
	}

	public String getAttendeeEmail() {
		return attendeeEmail;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public OffsetDateTime getEventDateTime() {
		return eventDateTime;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public String getTicketType() {
		return ticketType;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getTicketUrl() {
		return ticketUrl;
	}

	public String getQrCodeImageUrl() {
		return qrCodeImageUrl;
	}

	public EmailDeliveryStatus getStatus() {
		return status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
