package com.pim.replication.receiver.storage;

import java.util.Collection;

import com.pim.replication.messages.NewDataForReplicationMessage.DataForReplication;

public interface ReplicationStorage<T> {

	public void persistDataForReplication(final Collection<DataForReplication<T>> replicatedData);
}