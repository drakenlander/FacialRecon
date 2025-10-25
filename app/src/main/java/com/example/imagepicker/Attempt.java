package com.example.imagepicker;

public class Attempt {
    private final int id;
    private final Integer personId;
    private final String personName;
    private final String timestamp;

    public Attempt(int id, Integer personId, String personName, String timestamp) {
        this.id = id;
        this.personId = personId;
        this.personName = personName;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public Integer getPersonId() {
        return personId;
    }

    public String getPersonName() {
        return personName;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
