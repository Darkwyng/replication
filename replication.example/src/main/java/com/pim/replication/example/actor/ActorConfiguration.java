package com.pim.replication.example.actor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pim.replication.example.actor.entity.Actor;
import com.pim.replication.example.actor.entity.ActorFinder;
import com.pim.replication.example.actor.entity.ActorForReplication;
import com.pim.replication.messaging.ReplicationMessagingInterface;
import com.pim.replication.sender.ReplicationSender;

@Configuration
public class ActorConfiguration {

	@Bean
	public ReplicationSender<Actor, ActorForReplication> actorReplicationSender(final ActorFinder actorFinder,
			final ReplicationMessagingInterface messagingInterface) {
		return ReplicationSender.<Actor, ActorForReplication>builder() //
				.type("Actor") //
				.serviceId("Actor") //
				.idExtractor(actor -> String.valueOf(actor.getKey())) //
				.transformer(actor -> new ActorForReplication(actor.getFirstName(), actor.getLastName())) //
				.messagingInterface(messagingInterface) //
				.completeDataSupplier(actorFinder::getAllActors) //
				.build();
	}
}