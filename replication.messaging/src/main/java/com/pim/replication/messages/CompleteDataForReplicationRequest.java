package com.pim.replication.messages;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompleteDataForReplicationRequest implements Serializable {

	private static final long serialVersionUID = -2192568014693542218L;

	private final String type;
	private final String requesterId;

	public CompleteDataForReplicationRequest() {
		this(null, null);
	}
}