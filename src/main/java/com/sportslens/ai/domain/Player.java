package com.sportslens.ai.domain;

import jakarta.persistence.*;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String position;
    private int jerseyNumber;
    
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    // Constructors, getters and setters
    public Player() {}
    
    public Player(String name, String position, int jerseyNumber, Team team) {
        this.name = name;
        this.position = position;
        this.jerseyNumber = jerseyNumber;
        this.team = team;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public int getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}