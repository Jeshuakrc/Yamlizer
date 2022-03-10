package com.jkantrell.yamlizer.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericHandler {

    private final Class<?> clazz_;
    private final Type type_;
    private final GenericHandler[] genericHandlers_;

    public GenericHandler(Type type) {
        this.type_ = type;

        if (type instanceof ParameterizedType paramType) {
            Type[] paramTypes = paramType.getActualTypeArguments();
            this.genericHandlers_ = new GenericHandler[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                this.genericHandlers_[i] = new GenericHandler(paramTypes[i]);
            }

            String typeName = type.getTypeName();
            Class<?> typeClass;
            try {
                typeClass = Class.forName(typeName.substring(0,typeName.indexOf("<")));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                typeClass = null;
            }
            this.clazz_ = typeClass;

        } else {
            this.clazz_ = (Class<?>) type;
            this.genericHandlers_ = new GenericHandler[0];
        }
    }

    //GETTERS
    public Class<?> getClazz() {
        return this.clazz_;
    }
    public Type getType() {
        return this.type_;
    }
    public GenericHandler[] getParameterHandlers() {
        return this.genericHandlers_;
    }
    public int howManyParameters() {
        return this.genericHandlers_.length;
    }

    //CHECKS
    public boolean isParametrized() {
        return this.howManyParameters() > 0;
    }
}
