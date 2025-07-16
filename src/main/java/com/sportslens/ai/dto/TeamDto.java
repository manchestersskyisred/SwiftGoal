package com.sportslens.ai.dto;

public class TeamDto {
    private String name;
    private String league;
    private String city;

    // Constructors, getters and setters
    public TeamDto() {}
    
    public TeamDto(String name, String league, String city) {
        this.name = name;
        this.league = league;
        this.city = city;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}