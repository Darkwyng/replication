package com.pim.replication.example.actor.entity;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class ActorFinder {

	public Collection<Actor> getAllActors() {
		final Actor george = new Actor(4711, "George", "Clooney", Instant.ofEpochMilli(-273196800), "Did some films");
		final Actor brad = new Actor(4712, "Brad", "Pitt", Instant.ofEpochMilli(-190598400),
				"Did several films with George");
		final Actor julia = new Actor(4713, "Julia", "Roberts", Instant.ofEpochMilli(-68774400),
				"Did films without George and Brad too");
		return Arrays.asList(george, brad, julia);
	}
}
