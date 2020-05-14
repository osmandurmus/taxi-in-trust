package com.xormoti.taxi_in_trust.FireBaseTask.CollectionData;

public class Person_ {


    public String getId() {
        return id;
    }

    private String id;
    private String name;
    private String surname;
    private int age;
    private int phone;

    public Person_(String id, String name, String surname, int age, int phone){
        this.id=id;
        this.name=name;
        this.surname=surname;
        this.age=age;
        this.phone=phone;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getAge() {
        return age;
    }

    public int getPhone() {
        return phone;
    }

}
