package com.pim.replication.example.movie;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import com.pim.replication.example.actor.entity.ActorForReplication;
import com.pim.replication.messaging.AmqpMessagingInterface;
import com.pim.replication.messaging.ReplicationMessagingInterface;
import com.pim.replication.receiver.ReplicationReceiver;
import com.pim.replication.receiver.storage.InMemoryReplicationStorage;
import com.pim.replication.receiver.storage.ReplicationStorage;

@Configuration
public class MovieConfiguration {

	@Bean
	public ReplicationReceiver<ActorForReplication> movie(final ReplicationStorage<ActorForReplication> storage,
			final ReplicationMessagingInterface messagingInterface) {
		return ReplicationReceiver.<ActorForReplication>builder() //
				.type("Actor") //
				.serviceId("Movie") //
				.messagingInterface(messagingInterface) //
				.storage(storage) //
				.build();
	}

	@Bean
	public InMemoryReplicationStorage<ActorForReplication> inMemoryReplicationStorage() {
		return new InMemoryReplicationStorage<>();
	}

	@Bean
	public AmqpMessagingInterface amqpMessagingInterface(final GenericApplicationContext applicationContext,
			final ConnectionFactory connectionFactory, final AmqpTemplate template, final AmqpAdmin admin) {
		return new AmqpMessagingInterface(applicationContext, connectionFactory, new SimpleMessageConverter(), template,
				admin);
	}
}
