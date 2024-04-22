package com.nt118.proma.model;

public class Project {
    private String name;
    private String description;
    private String id;

    public Project(String name, String description, String id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }
}
