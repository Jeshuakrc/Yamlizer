package com.jkantrell.yamlizer.yaml;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a Yaml data element, including Scalars, Maps and Lists.
 */
public class YamlElement {

    //FIELDS
    protected final YamlElementType type;
    protected final Object element;

    //CONSTRUCTORS

    /**
     * Creates a new YamlElement from a Java object. Only objects that can be assigned as a Yaml primitive, such as Java primitives
     * and Strings will succeed, otherwise, an exception will be thrown.
     *
     * @param element The object to represent as Yaml type.
     *
     * @throws IllegalArgumentException if the passed object cannot be interpreted as a Yaml type.
     */
    public YamlElement(Object element) {
        this.element = element;
        YamlElementType<?> type = YamlElementType.assign(element);
        AbstractYamlConfig.LOGGER.info(element.getClass().toString());
        if (type == null) { throw new IllegalArgumentException("Not assignable to Yaml scalar"); }
        this.type = type;
    }

    /**
     * Checks if the element is of a given YamlType.
     *
     * @param type the type to check
     * @return true if the type provided matches the element's type, false otherwise.
     */
    public boolean is(YamlElementType<?> type) {
        AbstractYamlConfig.LOGGER.info(this.type.getType().toString() + "\n" + type.getType().toString());
        return this.type.equals(type);
    }

    /**
     * Gets the element casted as the Java class represented by the specified Yaml type, if the object is actually of that type.
     *
     * @param type The Yaml type to get the element as.
     * @return The element. Null if the element is not of the specified type.
     */
    public <T> T get(YamlElementType<T> type) {
        if (this.is(type)) {
            return (T) this.element;
        }
        return null;
    }

    /**
     * Gets the raw object contained in this YamlElement.
     *
     * @return the object.
     */
    public Object get() {
        return element;
    }

    /**
     * Gets a list of all the sub-Yaml elements contained in this Yaml element, all casted to the Java class represented by the
     * specified Yaml type.
     *
     * Useful under the certainty of this element being a list and all the elements in it are of the same Yaml primitive type.
     *
     * @param type The type to get all elements in the list as.
     * @return The list.
     * @throws ClassCastException If this element is not a list or any of the elements inside is not of the specified type.
     */
    public <T> List<T> getListOf(YamlElementType<T> type) throws ClassCastException {
        if (!this.is(YamlElementType.LIST)) { throw new ClassCastException("Not a list."); }
        List<YamlElement> list = this.get(YamlElementType.LIST);
        List<T> tList = new ArrayList<>();
        list.forEach(elm -> {
            T object = elm.get(type);
            if (object != null) {
                tList.add(object);
            } else {
                throw new ClassCastException(
                        "Element of index " + list.indexOf(elm) + " is " + elm.type.getType().getSimpleName() +
                        ", expected all to be of type " + type.getType().getSimpleName() + ". Unable to parse list."
                );
            }
        });

        return tList;
    }

    @Override
    public String toString() {
        return "[Type: " + this.type.getType().getSimpleName() + ", Value: " + element.toString() + "]";
    }

}
