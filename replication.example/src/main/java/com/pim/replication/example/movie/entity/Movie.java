package com.pim.replication.example.movie.entity;

import java.util.Collection;
import java.util.Date;

import com.pim.replication.example.actor.entity.ActorForReplication;

import lombok.Data;

@Data
public class Movie {

	private String name;
	private int length;
	private Date releaseDate;

	private Collection<ActorForReplication> actors;
}
