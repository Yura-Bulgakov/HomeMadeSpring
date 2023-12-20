package ru.example.framework;

import ru.example.annotations.Autowired;
import ru.example.annotations.Component;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Context {
    private Map<String, Class<?>> loadedClasses;

    private Context(Map<String, Class<?>> loadedClasses) {
        this.loadedClasses = loadedClasses;
    }

    public static Context load(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Map<String, Class<?>> clazzes = reflections.getSubTypesOf(Object.class).stream().filter(clazz -> clazz.isAnnotationPresent(Component.class)).collect(Collectors.toMap(clazz -> clazz.getAnnotation(Component.class).value(), clazz -> clazz));

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
        var constructors = clazz.getDeclaredConstructors();
        var annotatedConstructor = Arrays.stream(constructors)
                .filter(con -> con.isAnnotationPresent(Autowired.class))
                .findFirst();

        if (annotatedConstructor.isPresent()) {
            var constructor = annotatedConstructor.get();
            var parameterTypes = constructor.getParameterTypes();
            var params = Arrays.stream(parameterTypes)
                    .map(
                            cl -> {
                                try {
                                    return get(cl.getAnnotation(Component.class).value());
                                } catch (Exception e) {
                                    throw new RuntimeException("Такой тип нельзя подсавлять как параметр");
                                }
                            }

                    ).collect(Collectors.toList());
            return constructor.newInstance(params.toArray());
        } else {
            return clazz.getConstructor().newInstance();
        }
    }

}
