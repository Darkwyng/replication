package com.pim.replication.example.actor.entity;

import org.springframework.stereotype.Component;

import com.pim.replication.sender.ReplicationSender;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActorService {

	private final ReplicationSender<Actor, ActorForReplication> replicationSender;

	public void createNewActor(final Actor actor) {
		// ...

		replicationSender.broadcastChange(actor);
	}

	public void updateActor(final Actor previousData, final Actor newData) {
		// ...

		replicationSender.broadcastChange(newData);
	}
}