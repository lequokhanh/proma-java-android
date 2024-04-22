package com.nt118.proma.model;

import java.util.Date;

public class Task {
    private String name;
    private Integer id;
    private Date dueDate;
    private Integer status;

    public Task(String name, Integer id, Date dueDate, Integer status) {
        this.name = name;
        this.id = id;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Integer getStatus() {
        return status;
    }
}
