package ru.example;

import ru.example.framework.Context;
import ru.example.model.Car;

public class Main {
    public static void main(String[] args) {
        Context context = Context.load("ru.example.model");
        System.out.println(context.getLoadedClasses());

        try {
            Car car = (Car) context.get("Car");
            System.out.println(car.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}