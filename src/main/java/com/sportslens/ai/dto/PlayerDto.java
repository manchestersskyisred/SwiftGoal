package com.sportslens.ai.dto;

public class PlayerDto {
    private String name;
    private String position;
    private int jerseyNumber;
    private Long teamId;

    // Constructors, getters and setters
    public PlayerDto() {}
    
    public PlayerDto(String name, String position, int jerseyNumber, Long teamId) {
        this.name = name;
        this.position = position;
        this.jerseyNumber = jerseyNumber;
        this.teamId = teamId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public int getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}