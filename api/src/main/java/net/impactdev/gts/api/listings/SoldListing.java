package net.impactdev.gts.api.listings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * A sold listing is one which resembles a listing which, by name, has been sold.
 * What is special about this listing is that it is meant to track as little detail
 * about the listing as possible, as a means to inform the original seller of their
 * sale if not online to receive the notification.
 */
@Getter
@RequiredArgsConstructor
public class SoldListing {

	private UUID id = UUID.randomUUID();

	private final String nameOfEntry;

	private final double moneyReceived;

	public static SoldListingBuilder builder() {
		return new SoldListingBuilder();
	}

	public static class SoldListingBuilder {

		private UUID uuid;
		private String name;
		private double money;

		public SoldListingBuilder id(UUID id) {
			this.uuid = id;
			return this;
		}

		public SoldListingBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SoldListingBuilder money(double money) {
			this.money = money;
			return this;
		}

		public SoldListing build() {
			SoldListing sl = new SoldListing(name, money);
			sl.id = this.uuid;
			return sl;
		}
	}

}
