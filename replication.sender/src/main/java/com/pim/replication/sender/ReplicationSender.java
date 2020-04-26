package com.pim.replication.sender;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pim.replication.messages.CompleteDataForReplicationRequest;
import com.pim.replication.messages.NewDataForReplicationMessage;
import com.pim.replication.messages.NewDataForReplicationMessage.DataForReplication;
import com.pim.replication.messaging.ReplicationMessagingInterface;

import lombok.Builder;

@Builder
public class ReplicationSender<ORIGINAL, REPLICATION> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationSender.class);

	private final String type;
	private final String serviceId;
	private final Function<ORIGINAL, String> idExtractor;
	private final Function<ORIGINAL, REPLICATION> transformer;
	private final ReplicationMessagingInterface messagingInterface;
	private final Supplier<Collection<ORIGINAL>> completeDataSupplier;

	protected ReplicationSender(final String type, final String serviceId, final Function<ORIGINAL, String> idExtractor,
			final Function<ORIGINAL, REPLICATION> transformer, final ReplicationMessagingInterface messagingInterface,
			final Supplier<Collection<ORIGINAL>> completeDataSupplier) {
		super();

		Objects.requireNonNull(type);
		Objects.requireNonNull(serviceId);
		Objects.requireNonNull(idExtractor);
		Objects.requireNonNull(transformer);
		Objects.requireNonNull(messagingInterface);
		Objects.requireNonNull(completeDataSupplier);

		this.type = type;
		this.serviceId = serviceId;
		this.idExtractor = idExtractor;
		this.transformer = transformer;
		this.messagingInterface = messagingInterface;
		this.completeDataSupplier = completeDataSupplier;

		startUp();
	}

	protected void startUp() {
		LOGGER.info("Startup of replication sender for type {}", type);
		messagingInterface.createGlobalListener(type, this::handleCompleteDataForReplicationRequest,
				CompleteDataForReplicationRequest.class, serviceId);

		final NewDataForReplicationMessage<REPLICATION> message = createNewDataForReplicationMessageWithCompleteData();

		LOGGER.info("Sending complete data for type {} with {} data sets", type, message.getData().size());
		LOGGER.debug("Sending {}", message);
		messagingInterface.sendMessageToGlobalListeners(type, message);
	}

	public void broadcastChange(final ORIGINAL original) {

		final DataForReplication<REPLICATION> dataForReplication = mapToDataForReplication(original);

		final NewDataForReplicationMessage<REPLICATION> message = new NewDataForReplicationMessage<>(type,
				Arrays.asList(dataForReplication));

		LOGGER.debug("Broadcasting change: {}", message);
		messagingInterface.sendMessageToGlobalListeners(type, message);
	}

	protected void handleCompleteDataForReplicationRequest(final CompleteDataForReplicationRequest request) {

		if (!type.equals(request.getType())) {
			LOGGER.debug("Ignoring CompleteDataForReplicationRequest because of type {}: {}", type, request);
		} else {
			final NewDataForReplicationMessage<REPLICATION> message = createNewDataForReplicationMessageWithCompleteData();
			final String requesterId = request.getRequesterId();
			LOGGER.info(
					"Responding to CompleteDataForReplicationRequest for type {} and requester {} with {} data sets",
					type, requesterId, message.getData().size());
			LOGGER.debug("Responding to CompleteDataForReplicationRequest for type {} and requester {}: {} ",
					requesterId, message);
			messagingInterface.sendMessageToSelectiveListener(type, message, requesterId);
		}
	}

	protected NewDataForReplicationMessage<REPLICATION> createNewDataForReplicationMessageWithCompleteData() {
		final List<DataForReplication<REPLICATION>> completeDataForReplication = completeDataSupplier.get().stream()
				.map(this::mapToDataForReplication).collect(Collectors.toList());

		return new NewDataForReplicationMessage<>(type, completeDataForReplication);
	}

	protected DataForReplication<REPLICATION> mapToDataForReplication(final ORIGINAL original) {
		final String id = idExtractor.apply(original);
		final REPLICATION replication = transformer.apply(original);
		return new DataForReplication<>(id, replication);
	}
}
