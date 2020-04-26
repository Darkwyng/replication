package com.pim.replication.receiver;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pim.replication.messages.CompleteDataForReplicationRequest;
import com.pim.replication.messages.NewDataForReplicationMessage;
import com.pim.replication.messaging.ReplicationMessagingInterface;
import com.pim.replication.receiver.storage.ReplicationStorage;

import lombok.Builder;

@Builder
public class ReplicationReceiver<REPLICATION> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationReceiver.class);

	private final String type;
	private final String serviceId;
	private final ReplicationMessagingInterface messagingInterface;
	private final ReplicationStorage<REPLICATION> storage;

	public ReplicationReceiver(final String type, final String serviceId,
			final ReplicationMessagingInterface messagingInterface, final ReplicationStorage<REPLICATION> storage) {
		super();

		Objects.requireNonNull(type);
		Objects.requireNonNull(serviceId);
		Objects.requireNonNull(messagingInterface);
		Objects.requireNonNull(storage);

		this.type = type;
		this.serviceId = serviceId;
		this.messagingInterface = messagingInterface;
		this.storage = storage;

		startUp();
	}

	protected void startUp() {
		LOGGER.info("Startup of replication receiver for type {}", type);
		messagingInterface.createSelectiveListener(type, this::handleNewDataForReplicationMessage,
				NewDataForReplicationMessage.class, serviceId);
		messagingInterface.createGlobalListener(type, this::handleNewDataForReplicationMessage,
				NewDataForReplicationMessage.class, serviceId);

		final CompleteDataForReplicationRequest request = new CompleteDataForReplicationRequest(type, serviceId);
		LOGGER.info("Sending CompleteDataForReplicationRequest for type {} and requester {}", type, serviceId);
		LOGGER.debug("Sending {}", request);
		messagingInterface.sendMessageToGlobalListeners(type, request);
	}

	protected void handleNewDataForReplicationMessage(final NewDataForReplicationMessage<REPLICATION> message) {
		if (!type.equals(message.getType())) {
			LOGGER.debug("Ignoring NewDataForReplicationMessage because of type {}: {}", type, message);
		} else {
			LOGGER.debug("Received NewDataForReplicationMessage for type {}: {} ", type, message);
			storage.persistDataForReplication(message.getData());
		}
	}
}
