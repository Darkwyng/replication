package com.pim.replication.example.actor.entity;

import java.time.Instant;

import lombok.Data;

@Data
public class Actor {

	private final long key;
	private final String firstName;
	private final String lastName;

	private final Instant birthday;
	private final String biography;
}
