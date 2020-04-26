package com.pim.replication.messages;

import java.io.Serializable;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewDataForReplicationMessage<T> implements Serializable {

	private static final long serialVersionUID = 8614306434573053523L;

	private final String type;
	private final Collection<DataForReplication<T>> data;

	public NewDataForReplicationMessage() {
		this(null, null);
	}

	@Data
	@AllArgsConstructor
	public static class DataForReplication<T2> implements Serializable {

		private static final long serialVersionUID = 6546048366152690797L;

		private final String id;
		private final T2 data;

		public DataForReplication() {
			this(null, null);
		}
	}
}