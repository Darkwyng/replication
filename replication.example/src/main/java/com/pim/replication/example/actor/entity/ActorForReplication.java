package com.pim.replication.example.actor.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class ActorForReplication implements Serializable {

	private static final long serialVersionUID = 1702570397901363682L;

	private final String firstName;
	private final String lastName;
}