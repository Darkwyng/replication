package com.pim.replication.receiver.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.pim.replication.messages.NewDataForReplicationMessage.DataForReplication;

public class InMemoryReplicationStorage<T> implements ReplicationStorage<T> {

	private final Map<String, T> map = new ConcurrentHashMap<>();

	@Override
	public void persistDataForReplication(final Collection<DataForReplication<T>> replicatedData) {
		replicatedData.stream().forEach(entry -> map.put(entry.getId(), entry.getData()));
	}

	public Optional<T> getById(final String id) {
		return Optional.ofNullable(map.get(id));
	}

	public Collection<T> getAll() {
		return new ArrayList<>(map.values());
	}
}
