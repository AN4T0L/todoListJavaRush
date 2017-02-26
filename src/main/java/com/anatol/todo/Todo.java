package com.anatol.todo;




public class Todo {
    private int id;
    private String description;
    private double progress;


    public Todo(String description, double progress) {
        this.description = description;
        this.progress = progress;
    }

    public Todo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
}
