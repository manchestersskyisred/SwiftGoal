package com.sportslens.ai.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String league;
    private String city;
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> players;

    // Constructors, getters and setters
    public Team() {}
    
    public Team(String name, String league, String city) {
        this.name = name;
        this.league = league;
        this.city = city;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
}