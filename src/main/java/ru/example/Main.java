package ru.example;

import ru.example.framework.Context;
import ru.example.model.AutoBot;
import ru.example.model.Car;

public class Main {
    public static void main(String[] args) {
        Context context = Context.load("ru.example.model");
        System.out.println(context.getLoadedClasses());

        System.out.println("@Autowired target = FIELD");
        try {
            Car car = (Car) context.get("Car");
            System.out.println(car.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("@Autowired target = CONSTRUCTOR");
        try {
            AutoBot autoBot = (AutoBot) context.get("AutoBot");
            System.out.println(autoBot.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}