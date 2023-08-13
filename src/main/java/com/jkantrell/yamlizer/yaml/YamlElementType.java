package com.jkantrell.yamlizer.yaml;

import java.util.List;
import java.util.Map;

/**
 * A fake-enum class that represents all possible primitive data types in Yaml, including Scalars, Lists and Dictionaries.
 */
public class YamlElementType<E> {

    //FAKE ENUM VALUES
    public static final YamlElementType<Integer> INT = new YamlElementType<>(Integer.class);
    public static final YamlElementType<Double> DOUBLE = new YamlElementType<>(Double.class);
    public static final YamlElementType<String> STRING = new YamlElementType<>(String.class);
    public static final YamlElementType<Boolean> BOOL = new YamlElementType<>(Boolean.class);
    public static final YamlElementType<List<YamlElement>> LIST = new YamlElementType<>(List.class);
    public static final YamlElementType<YamlMap> MAP = new YamlElementType<>(Map.class);

    private static final YamlElementType<?>[] values_ = {INT, DOUBLE,STRING,BOOL,LIST,MAP};

    /**
     * Gets all the values of the fake-enum. Each one representing a type of primitive data in Yaml.
     *
     * @return the values as an array.
     */
    public static YamlElementType<?>[] values() {
        return values_;
    }

    private static final Map<Class<?>, YamlElementType<?>> PRIMITIVE_MAP = Map.ofEntries(
            Map.entry(int.class,INT),
            Map.entry(float.class, DOUBLE),
            Map.entry(long.class, INT),
            Map.entry(double.class, DOUBLE),
            Map.entry(short.class, INT),
            Map.entry(char.class,STRING),
            Map.entry(boolean.class,BOOL)
    );
    private final Class<?> type_;

    private YamlElementType(Class<?> type) {
        this.type_ = type;
    }

    /**
     * Gets the java Class mapped to this Yaml type.
     *
     * @return the Class object.
     */
    public Class<?> getType(){
        return this.type_;
    }

    /**
     * Gets the YamlElementType object that can be assigned to the object provided.
     *
     * @param object the object to assign.
     * @return the YamlElementType object. Null if non-assignable.
     */
    public static YamlElementType<?> assign(Object object) {
        YamlElementType<?> r = null;
        Class<?> clazz = object.getClass();

        if (object.getClass().isArray()) {
            return YamlElementType.LIST;
        }
        if (YamlElementType.PRIMITIVE_MAP.containsKey(clazz)) {
            return YamlElementType.PRIMITIVE_MAP.get(clazz);
        }
        for (YamlElementType<?> type : YamlElementType.values()) {
            if (type.getType().isAssignableFrom(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException(object.getClass().getSimpleName() + " is not YAML type assignable.");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (object == null) { return false; }
        if (object.getClass().equals(this.getClass())) {
            return ((YamlElementType) object).getType().equals(this.getType());
        }
        return false;
    }
}
