package com.jkantrell.yamlizer.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeHandler {

    private final Class<?> clazz_;
    private final Type type_;
    private final TypeHandler[] typeHandlers_;
    private final boolean isArray_;
    private final Type arrayComponent_;

    public TypeHandler(Type type) {
        this.type_ = type;

        if (type instanceof ParameterizedType paramType) {
            Type[] paramTypes = paramType.getActualTypeArguments();
            this.typeHandlers_ = new TypeHandler[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                this.typeHandlers_[i] = new TypeHandler(paramTypes[i]);
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
            this.isArray_ = false;
            this.arrayComponent_ = null;
        } else if (type instanceof GenericArrayType genericArray) {
            this.arrayComponent_ = genericArray.getGenericComponentType();
            TypeHandler handler = new TypeHandler(this.arrayComponent_);
            this.isArray_ = true;
            this.clazz_ = handler.getClazz();
            this.typeHandlers_ = handler.getParameterHandlers();
        } else {
            Class<?> clazz = (Class<?>) type;
            this.clazz_ = clazz;
            this.isArray_ = clazz.isArray();
            this.arrayComponent_ = (this.isArray_) ? clazz.getComponentType() : null;
            this.typeHandlers_ = new TypeHandler[0];
        }
    }

    //GETTERS
    public Class<?> getClazz() {
        return this.clazz_;
    }
    public Type getType() {
        return this.type_;
    }
    public TypeHandler[] getParameterHandlers() {
        return this.typeHandlers_;
    }
    public int howManyParameters() {
        return this.typeHandlers_.length;
    }
    public Type getArrayComponent() {
        return this.arrayComponent_;
    }

    //CHECKS
    public boolean isParametrized() {
        return this.howManyParameters() > 0;
    }
    public boolean isArray() {
        return this.isArray_;
    }
}
