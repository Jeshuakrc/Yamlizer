package com.jkantrell.yamlizer.yaml;

import com.jkantrell.yamlizer.reflect.TypeHandler;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class Yamlizer {

    private final Set<Serialization<?>> serializations_ = new HashSet<>();

    public <T> void addSerializationRule(Class<T> type, YamlDeserializer<T> deserializer) {
        this.serializations_.add(new Serialization<>(type,deserializer));
    }

    public Yamlizer() {
        this.addDeserializers_();
    }

    public Object deserialize(YamlElement src, TypeHandler type) {
        Serialization<?> serialization = this.getSerialization_(type);
        if (serialization != null) {
            try {
                return serialization.deserializer().deserialize(src, type.getType());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        if (type.isArray()) {
            List<YamlElement> yamlElements = src.get(YamlElementType.LIST);
            Type arrayComponent = type.getArrayComponent();
            Object array = Array.newInstance((Class<?>) arrayComponent,yamlElements.size());

            for (int i = 0; i < yamlElements.size(); i++) {
                Array.set(array,i,this.deserialize(yamlElements.get(i),arrayComponent));
            }

            return array;
        }

        if (type.getType() instanceof Class clazz) {
            if (clazz.isEnum()) {
                String val = src.get(YamlElementType.STRING);
                try {
                    return Enum.valueOf(clazz, val);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("'" + val + "' is not a valid value for " + clazz.getSimpleName());
                }
            }
        }

        throw new ClassCastException(
            " Unable to infer deserialization for " + type.getType().getTypeName() +
            " and no deserializer was found for it. Please define a custom deserializer."
        );
    };
    public Object deserialize(YamlElement src, Type type) {
        return this.deserialize(src, new TypeHandler(type));
    }
    public <T> T deserialize(YamlElement src, Class<T> type) {
        return (T) this.deserialize(src, (Type) type);
    }

    //PRIVATE METHODS
    private Serialization<?> getSerialization_(TypeHandler type) {
        Class clazz = type.getClazz();

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

        return null;
    }

    //PRIVATE METHODS
    private void addDeserializers_() {
        HashMap<Class, YamlDeserializer> map = new HashMap<>();
        map.put(
                String.class,
                (e, t) -> e.get(YamlElementType.STRING)
        );
        map.put(
                Double.class,
                (e, t) -> e.get(YamlElementType.DOUBLE)
        );
        map.put(
                Integer.class,
                (e, t) -> e.get(YamlElementType.INT)
        );
        map.put(
                Boolean.class,
                (e, t) -> e.get(YamlElementType.BOOL)
        );
        map.put(
                byte.class,
                (e, t) -> e.get(YamlElementType.INT).byteValue()
        );
        map.put(
                short.class,
                (e, t) -> e.get(YamlElementType.INT).shortValue()
        );
        map.put(
                int.class,
                (e, t) -> e.get(YamlElementType.INT).intValue()
        );
        map.put(
                long.class,
                (e, t) -> e.get(YamlElementType.INT).longValue()
        );
        map.put(
                float.class,
                (e, t) -> e.get(YamlElementType.DOUBLE).floatValue()
        );
        map.put(
                double.class,
                (e, t) -> e.get(YamlElementType.DOUBLE).doubleValue()
        );
        map.put(
                char.class,
                (e, t) -> e.get(YamlElementType.STRING).charAt(0)
        );
        map.put(
                boolean.class,
                (e, t) -> e.get(YamlElementType.BOOL).booleanValue()
        );
        map.put(
                List.class,
                (e, t) -> {
                    TypeHandler typeHandler = new TypeHandler(t);
                    List<YamlElement> yamlElements = e.get(YamlElementType.LIST);
                    List list = new ArrayList();
                    for (YamlElement element : yamlElements) {
                        list.add(this.deserialize(element, typeHandler.getParameterHandlers()[0]));
                    }
                    try {
                        Constructor<?> constructor = typeHandler.getClazz().getConstructor(Collection.class);
                        return constructor.newInstance(list);
                    } catch (NoSuchMethodException ex) {;
                        return list;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }
        );
        for (Map.Entry<Class, YamlDeserializer> entry : map.entrySet()) {
            this.addSerializationRule(entry.getKey(), entry.getValue());
        }
    }

    //CLASSES
    private record Serialization<T> (Class<T> type, YamlDeserializer<T> deserializer) {}
}
