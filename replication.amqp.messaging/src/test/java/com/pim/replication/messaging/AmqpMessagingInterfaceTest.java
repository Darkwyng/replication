package com.pim.replication.messaging;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;

import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootTest(classes = AmqpMessagingInterfaceTest.TestApplication.class)
@SuppressWarnings("rawtypes")
class AmqpMessagingInterfaceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AmqpMessagingInterfaceTest.class);

	private static final String TYPE = "type";
	private static final String LISTENER_ID = "listener";

	private AmqpMessagingInterface testee = null;

	@Autowired
	private RabbitHealthIndicator rabbitHealthIndicator;
	@Autowired
	private GenericApplicationContext applicationContext;
	@Autowired
	private ConnectionFactory connectionFactory;
	@Autowired
	private AmqpTemplate template;
	@Autowired
	private AmqpAdmin admin;

	@BeforeEach
	public void setUp() {
		assumeThatConnectionIsUp();
		testee = new AmqpMessagingInterface(applicationContext, connectionFactory, new SimpleMessageConverter(),
				template, admin);
	}

	/** The test will be skipped, if the messaging bus cannot be reached. */
	private void assumeThatConnectionIsUp() {
		final Health health = rabbitHealthIndicator.getHealth(false);
		final boolean isUp = Status.UP.equals(health.getStatus());
		assumeTrue(isUp, "The message bus must be available for this test to run. It is " + health.getStatus());
	}

	@Test
	void testThatMessageCanBeSentAndReceivedGlobally() {
		final AtomicReference<MessagePayloadForTest> receivedMessage = new AtomicReference<>();
		final Consumer<MessagePayloadForTest> handler = payload -> {
			LOGGER.info("Received {}", payload);
			receivedMessage.getAndSet(payload);
		};

		testee.createGlobalListener(TYPE, handler, MessagePayloadForTest.class, LISTENER_ID);
		testee.sendMessageToGlobalListeners(TYPE, createMessagePayload());

		assertCorrectlyReceivedPayload(receivedMessage);
	}

	@Test
	void testThatMessageCanBeSentAndReceivedSelectively() {
		final AtomicReference<MessagePayloadForTest> receivedMessage = new AtomicReference<>();
		final Consumer<MessagePayloadForTest> handler = payload -> {
			LOGGER.info("Received {}", payload);
			receivedMessage.getAndSet(payload);
		};

		testee.createSelectiveListener(TYPE, handler, MessagePayloadForTest.class, LISTENER_ID);
		testee.sendMessageToSelectiveListener(TYPE, createMessagePayload(), LISTENER_ID);

		assertCorrectlyReceivedPayload(receivedMessage);
	}

	private Object createMessagePayload() {
		final MessagePayloadForTest<Object> payload = new MessagePayloadForTest<Object>();
		payload.setText("TheMessage");
		payload.getData().add("aString");
		payload.getData().add(17);
		payload.getData().add(17L);
		payload.getData().add(17.1);
		return payload;
	}

	private void assertCorrectlyReceivedPayload(final AtomicReference<MessagePayloadForTest> receivedMessage) {
		await().atMost(ofSeconds(2)).untilAsserted(() -> assertThat(receivedMessage.get(), notNullValue()));
		final MessagePayloadForTest<?> payload = receivedMessage.get();
		assertThat(payload.getText(), is("TheMessage"));
		assertThat(payload.getData(), containsInAnyOrder("aString", 17, 17L, 17.1));
	}

	@SpringBootApplication
	public static class TestApplication {

	}

	@Data
	@NoArgsConstructor
	private static class MessagePayloadForTest<T> implements Serializable {

		private static final long serialVersionUID = -258912216618229946L;

		private String text;
		private Collection<T> data = new ArrayList<>();
	}
}