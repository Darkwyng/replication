# replication
This library can be used to replicate data between two micro services.

## Usage
The example project shows a simple setup, in which `Actors` of the service `Actors` can be replicated into the service `Movie`.

The `Actor` service needs to build a `ReplicationSender` like this:
```
@Bean
public ReplicationSender<Actor, ActorForReplication> actorReplicationSender(final ActorFinder actorFinder,
		final ReplicationMessagingInterface messagingInterface) {
	return ReplicationSender.<Actor, ActorForReplication>builder() 
			.type("Actor") 
			.serviceId("Actor") 
			.idExtractor(actor -> String.valueOf(actor.getKey())) 
			.transformer(actor -> new ActorForReplication(actor.getFirstName(), actor.getLastName())) 
			.messagingInterface(messagingInterface) 
			.completeDataSupplier(actorFinder::getAllActors) 
			.build();
}
```

The 'Movie' service can receive replicated data by building a `ReplicationReceiver`:
```
@Bean
public ReplicationReceiver<ActorForReplication> movie(final ReplicationStorage<ActorForReplication> storage,
		final ReplicationMessagingInterface messagingInterface) {
	return ReplicationReceiver.<ActorForReplication>builder() 
			.type("Actor") 
			.serviceId("Movie") 
			.messagingInterface(messagingInterface) 
			.storage(storage) 
			.build();
}
```
This library does not use Spring itself, only the example project does.

## Features
The two classes will make sure that data of the sender will be sent to the receiver and will be persisted there:
- when the sender starts up, it will broadcast the complete available data (in case a new receiver has started since the sender was running last).
- while the sending service is up, an update can be triggered, e.g. when a new `Actor` is created or an existing one is updated.
This will be sent to the receivers, which will store the new or changed data set.
- when the receiver starts up, it will request the complate available data from the sender (in case it missed any updated during its downtime).
- data is stored in a class implementing the interface `ReplicationStorage` (this interface has one method)
- messages are exchanged by a class implementing `ReplicationMessagingInterface`. A class using AMQP (RabbitMQ) is part of this library.

## Messaging
Both sender and receiver need to follow the same messaging protocol. For the above code to work, they also both need to provide a `ReplicationMessagingInterface`, e.g. like this:
```
@Bean
public AmqpMessagingInterface amqpMessagingInterface(final GenericApplicationContext applicationContext,
		final ConnectionFactory connectionFactory, final AmqpTemplate template, final AmqpAdmin admin) {
	return new AmqpMessagingInterface(applicationContext, connectionFactory, new SimpleMessageConverter(), template,
			admin);
}
```

## Sending updates
The sending service needs to call the `ReplicationSender`, when there is an update:
```
private ReplicationSender<Actor, ActorForReplication> replicationSender;

public void createNewActor(Actor actor) {
	// ...
	replicationSender.broadcastChange(actor);
}
```

## Storage
The receiver needs some way of storing the received data. To achieve this, a class implementing `ReplicationStorage` is needed. 
This library provides an implementation that stores the data in memory.
```
@Bean
public InMemoryReplicationStorage<ActorForReplication> inMemoryReplicationStorage() {
	return new InMemoryReplicationStorage<>();
}
```

## Not yet implemented
- A `ReplicationStorage` that stores data in a database.
- Support for marking replicated data as inactive or deleted (for which the business code of the receiver would decide whether to ignore such entries or not).
- Support for a sender breaking the API (e.g. when the structure of the sent data is changed by adding or removing a field, or by changing the name or data type of a field).
