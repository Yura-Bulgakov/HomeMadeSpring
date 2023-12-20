package ru.example.model;

import ru.example.annotations.Autowired;
import ru.example.annotations.Component;

@Component("AutoBot")
public class AutoBot {
    private Car car;
    private Body body;

    @Autowired
    public AutoBot(Car car, Body body) {
        this.car = car;
        this.body = body;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "AutoBot{" +
                "car=" + car +
                ", body=" + body +
                '}';
    }
}
