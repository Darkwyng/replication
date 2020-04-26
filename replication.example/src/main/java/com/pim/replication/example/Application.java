package com.pim.replication.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.pim.replication.example.actor.entity.ActorForReplication;
import com.pim.replication.receiver.storage.InMemoryReplicationStorage;

@SpringBootApplication
@EnableScheduling
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	@Autowired
	private InMemoryReplicationStorage<ActorForReplication> replicationStorage;

	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Scheduled(fixedDelay = 5000)
	public void logStateOfReplication() {
		final int numberOfActors = replicationStorage.getAll().size();
		LOGGER.info("{} actors have been replicated.", numberOfActors);
	}
}