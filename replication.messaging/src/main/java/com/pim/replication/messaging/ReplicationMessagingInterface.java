package com.pim.replication.messaging;

import java.util.function.Consumer;

public interface ReplicationMessagingInterface {

	public <T> void createSelectiveListener(final String type, final Consumer<T> handler, final Class<T> messageClass,
			final String listenerId);

	public <T> void createGlobalListener(final String type, final Consumer<T> handler, final Class<T> messageClass, String listenerId);

	public <T> void sendMessageToSelectiveListener(final String type, final T message, final String listenerId);

	public <T> void sendMessageToGlobalListeners(final String type, final T message);
}
