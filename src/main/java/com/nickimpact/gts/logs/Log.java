package com.nickimpact.gts.logs;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class Log {

	/** The ID of the log */
	private final int id;

	/** The date the log was issued */
	private final Date date;

	/** The individual this log belogns to */
	private final UUID source;

	/** The action that forged the log */
	private final LogAction action;

	/** The actual text to the log itself */
	private final List<String> hover;

	public Log(Builder builder) {
		this.id = builder.id;
		this.date = builder.date;
		this.source = builder.source;
		this.action = builder.action;
		this.hover = builder.log;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the ID of the log element
	 *
	 * @return The log ID
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * States the date a log was issued
	 *
	 * @return The date a log was issued to a user
	 */
	public Date getIssueDate() {
		return this.date;
	}

	/**
	 * States the user, by their {@link UUID}, that owns this log
	 *
	 * @return The uuid of the log recipient
	 */
	public UUID getSource() {
		return this.source;
	}

	/**
	 * Fetches the actual log element for the log
	 *
	 * @return The information pertaining to a log
	 */
	public List<String> getLog() {
		return this.hover;
	}

	public static class Builder {
		private int id = -1;

		private Date date;

		private UUID source;

		private List<String> log;

		private LogAction action;

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder date(Date date) {
			this.date = date;
			return this;
		}

		public Builder source(UUID source) {
			this.source = source;
			return this;
		}

		public Builder log(List<String> log) {
			this.log = log;
			return this;
		}

		public Builder action(LogAction action) {
			this.action = action;
			return this;
		}

		public Log build() {
			if(date == null)
				date = Date.from(Instant.now());

			return new Log(this);
		}
	}
}
