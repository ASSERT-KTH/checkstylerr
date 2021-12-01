package ru.szhernovoy.springbeans;/**
 * Created by Admin on 27.01.2017.
 */

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class User {

    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
