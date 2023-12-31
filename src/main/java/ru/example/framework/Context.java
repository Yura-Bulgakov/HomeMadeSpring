package ru.example.framework;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import ru.example.annotations.Autowired;
import ru.example.annotations.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Context {
    private final Map<String, Class<?>> loadedClasses;

    private Context(Map<String, Class<?>> loadedClasses) {
        this.loadedClasses = loadedClasses;
    }

    public static Context load(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Map<String, Class<?>> clazzes = reflections.getSubTypesOf(Object.class).stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .collect(toMap(clazz -> clazz.getAnnotation(Component.class).value(), clazz -> clazz));

        return new Context(clazzes);
    }

    public Map<String, Class<?>> getLoadedClasses() {
        return loadedClasses;
    }

    public Object get(String className) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!loadedClasses.containsKey(className)) {
            throw new RuntimeException("Нет такого объекта");
        }

        Class<?> clazz = loadedClasses.get(className);
        var annotatedConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(con -> con.isAnnotationPresent(Autowired.class))
                .findFirst();

        if (annotatedConstructor.isPresent()) {
            return getByParameterizedConstructor(annotatedConstructor.get());
        } else {
            return getByDefaultConstructor(clazz.getConstructor());
        }
    }

    private Object getByParameterizedConstructor(Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var parameterTypes = constructor.getParameterTypes();
        return constructor.newInstance(Arrays.stream(parameterTypes)
                .map(clazz -> {
                    try {
                        return get(clazz.getAnnotation(Component.class).value());
                    } catch (Exception e) {
                        throw new RuntimeException("Такой тип нельзя подставлять как параметр");
                    }
                }).toArray());
    }

    private Object getByDefaultConstructor(Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object instance = constructor.newInstance();
        Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        field.set(instance, get(field.getType().getAnnotation(Component.class).value()));
                    } catch (Exception e) {
                        throw new RuntimeException("Нельзя подставить значение в данное поле" + field.getName());
                    }
                });
        return instance;
    }

}
