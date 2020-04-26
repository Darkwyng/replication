
package com.pim.replication.messaging;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.support.GenericApplicationContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AmqpMessagingInterface implements ReplicationMessagingInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(AmqpMessagingInterface.class);

	private static final String ROUTING_KEY_PREFIX = "route.";

	private final GenericApplicationContext applicationContext;
	private final ConnectionFactory connectionFactory;
	private final MessageConverter messageConverter;
	private final AmqpTemplate template;
	private final AmqpAdmin admin;

	@Override
	public <T> void createSelectiveListener(final String type, final Consumer<T> handler, final Class<T> messageClass,
			final String listenerId) {
		final String exchangeName = getExchangeName(type, messageClass);
		final String queueName = exchangeName + "_Selective_" + listenerId;
		final String routingKey = getRoutingKeyForSelectiveListener(listenerId);
		LOGGER.debug("Creating listener for exchange {}, queue {} and routing key {}", exchangeName, queueName,
				routingKey);

		declareForListener(exchangeName, queueName, routingKey);
		createListener(queueName + "_Listener", queueName, handler);
	}

	@Override
	public <T> void createGlobalListener(final String type, final Consumer<T> handler, final Class<T> messageClass,
			final String listenerId) {
		final String exchangeName = getExchangeName(type, messageClass);
		final String queueName = exchangeName + "_Global_" + listenerId;
		final String routingKey = getRoutingKeyForGlobalListener();
		LOGGER.debug("Creating listener for exchange {}, queue {} and routing key {}", exchangeName, queueName,
				routingKey);

		declareForListener(exchangeName, queueName, routingKey);
		createListener(queueName + "_Listener", queueName, handler);
	}

	@Override
	public <T> void sendMessageToSelectiveListener(final String type, final T payload, final String listenerId) {
		LOGGER.debug("Sending selectively for type {} and listener {}: {}", type, listenerId, payload);

		convertAndSend(type, getRoutingKeyForSendingSelectively(listenerId), payload);
	}

	@Override
	public <T> void sendMessageToGlobalListeners(final String type, final T payload) {
		LOGGER.debug("Sending globally for type {}: {}", type, payload);
		convertAndSend(type, getRoutingKeyForSendingGlobally(), payload);
	}

	private void convertAndSend(final String type, final String routingKey, final Object payload) {
		final Message message = messageConverter.toMessage(payload, null);
		template.send(getExchangeName(type, payload.getClass()), routingKey, message);
	}

	private String getExchangeName(final String type, final Class<?> messageClass) {
		return "Replication_" + type + "_" + messageClass.getSimpleName();
	}

	private String getRoutingKeyForSelectiveListener(final String listenerId) {
		return ROUTING_KEY_PREFIX + listenerId;
	}

	private String getRoutingKeyForGlobalListener() {
		return ROUTING_KEY_PREFIX + "*";
	}

	private String getRoutingKeyForSendingSelectively(final String listenerId) {
		return ROUTING_KEY_PREFIX + listenerId;
	}

	private String getRoutingKeyForSendingGlobally() {
		return ROUTING_KEY_PREFIX + "global";
	}

	private void declareForListener(final String exchangeName, final String queueName, final String routingKey) {
		final Exchange exchange = new TopicExchange(exchangeName);
		final Queue queue = new Queue(queueName);
		final Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();

		admin.declareExchange(exchange);
		admin.declareQueue(queue);
		admin.declareBinding(binding);
	}

	@SuppressWarnings("unchecked")
	private <T> void createListener(final String beanName, final String queueName, final Consumer<T> handler) {
		final Consumer<Message> messageHandler = message -> {
			LOGGER.debug("Received {}", message);
			final Object payload = messageConverter.fromMessage(message);
			handler.accept((T) payload);
		};

		final Supplier<SimpleMessageListenerContainer> beanSupplier = () -> createMessageListenerContainer(queueName,
				messageHandler);
		applicationContext.registerBean(beanName, SimpleMessageListenerContainer.class, beanSupplier);

		// Triggers the supplier, so that the container is actually created:
		final SimpleMessageListenerContainer container = applicationContext.getBean(beanName,
				SimpleMessageListenerContainer.class);
		container.start();
	}

	private SimpleMessageListenerContainer createMessageListenerContainer(final String queueName,
			final Consumer<Message> handler) {
		final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		final SimpleMessageListenerContainer container = factory.createListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(handler::accept);
		return container;
	}
}