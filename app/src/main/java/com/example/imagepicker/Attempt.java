package com.example.imagepicker;

public class Attempt {
    private final int id;
    private final Integer personId;
    private final String personName;
    private final String timestamp;
    private final String idCard;
    private final String intent;

    public Attempt(int id, Integer personId, String personName, String timestamp, String idCard, String intent) {
        this.id = id;
        this.personId = personId;
        this.personName = personName;
        this.timestamp = timestamp;
        this.idCard = idCard;
        this.intent = intent;
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

    public String getIdCard() {
        return idCard;
    }

    public String getIntent() {
        return intent;
    }
}
