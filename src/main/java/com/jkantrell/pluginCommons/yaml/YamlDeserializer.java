package com.jkantrell.pluginCommons.yaml;

import java.lang.reflect.Type;

/**
 * Functional interface to define the deserialization process of an object of a given type.
 */
@FunctionalInterface
public interface YamlDeserializer<T> {

    /**
     * Deserializes the object from a YamlElement.
     *
     * @param src The YamlElement containing the source data.
     * @param objectType The type of the Object to deserialize.
     * @return The object.
     */
    T deserialize(YamlElement src, Type objectType);

}
