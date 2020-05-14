package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

public class Person_ {


    public String getId() {
        return id;
    }

    private String id;
    private String fullName;

    public boolean isDriver() {
        return isDriver;
    }

    public boolean isPassenger() {
        return isPassenger;
    }

    private boolean isDriver=false;
    private boolean isPassenger=false;
    public Person_(String id, String name){
        this.id=id;
        this.fullName =name;

    }

    public Person_(String id, String name,boolean driver, boolean passenger){
        this.id=id;
        this.fullName =name;
        this.isDriver=driver;
        this.isPassenger=passenger;
    }

    public String getFullName() {
        return fullName;
    }
}
