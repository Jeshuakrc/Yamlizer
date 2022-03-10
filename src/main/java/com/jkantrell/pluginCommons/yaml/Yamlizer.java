package com.jkantrell.pluginCommons.yaml;

import com.jkantrell.pluginCommons.reflect.GenericHandler;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class Yamlizer {

    private final Set<Serialization<?>> serializations_ = new HashSet<>();

    public <T> void addSerializationRule(Class<T> type, YamlDeserializer<T> deserializer) {
        this.serializations_.add(new Serialization<>(type,deserializer));
    }

    public Object deserialize(YamlElement src, Type type) {
        try {
            return this.getSerialization_(type).deserializer().deserialize(src,type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    //PRIVATE METHODS
    private Serialization<?> getSerialization_(Type type) throws ClassNotFoundException {
        Class clazz = new GenericHandler(type).getClazz();

        AbstractYamlConfig.LOGGER.info(clazz.getName());

        boolean repeat = false;
        Predicate<Serialization<?>> check = s -> clazz.equals(s.type());
        do {
            for (Serialization<?> serialization : this.serializations_) {
                if (check.test(serialization)) {
                    return serialization;
                }
            }
            check = s -> s.type().isAssignableFrom(clazz);
            repeat = !repeat;
        } while (repeat);

        throw new ClassCastException(
            "Unable to infer deserialization for " + clazz.getName() +
            " and no deserializer was found for it. Please define a custom deserializer."
        );
    }

    //CLASSES
    private record Serialization<T> (Class<T> type, YamlDeserializer<T> deserializer) {}
}
