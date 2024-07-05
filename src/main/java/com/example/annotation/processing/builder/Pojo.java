package com.example.annotation.processing.builder;


import com.example.annotation.processing.annotation.Builder;

@Builder
public class Pojo {

    public String name;
    public String address;

    public Pojo() {
        super();
    }
    public Pojo(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
