package com.jkantrell.yamlizer.yaml;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;

/**
 * This is an implementation of Map<String, YamlElement>, and it's a YamlElement at the same time.
 * Represents a Yaml dictionary, it contains Yaml elements keyed by a string, which might be scalars, list or mote YamlMaps.
 */
public class YamlMap implements Map<String,YamlElement> {

    private final static Logger LOGGER = LoggerFactory.getLogger(YamlMap.class);
    protected final HashMap<String,YamlElement> map_ = new HashMap<>();

    /**
     * Creates a new YamlMap from an InputStream providing Yaml data.
     *
     * @param inputStream the InputStream to load.
     */
    public YamlMap(InputStream inputStream) {
        this((Map<String, Object>) new Yaml().load(inputStream));
    }

    /**
     * Creates a Yaml map out of a Map with String keys and Object values.
     * Objects must be YamlElementType assignable.
     *
     * @param map the Map.
     */
    public YamlMap(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            try {
                this.put(entry.getKey(), this.getElement_(entry.getValue()));
            } catch (IllegalArgumentException ex) {
                YamlMap.LOGGER.warn("Unable to load {}, as it doesn't represent a YAML primitive data type",entry.getKey());
            }
        }
    }

    @Override
    public int size() {
        return this.map_.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map_.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map_.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map_.containsValue(value);
    }

    /**
     * Gets the YamlElement associated with a given String key.
     *
     * @param key The key to look for
     * @return The YamlElement. Null if the Key doesn't exist.
     */
    @Override
    public YamlElement get(Object key) {
        return this.map_.get(key);
    }

    /**
     * Gets the YamlElement linked to a Sting path into the Map.
     *
     * @param path The strings that define the Path to dive in.
     * @return The YamlElement associated to the deepest found path key. Null if the top-level key doesn't exist.
     */
    public YamlElement get(String... path) {
        YamlMap map = this;
        YamlElement element = null;
        for (String s : path) {
            element = map.get(s);
            if (element == null) { break; }
            map = element.get(YamlElementType.MAP);
            if (map == null) { break; }
        }
        return element;
    }

    /**
     * Gets the YamlElement linked to a Sting path into the Map.
     *
     * @param path The Yaml path to look into (Elements separated by '.').
     * @return The YamlElement associated to the deepest found path key. Null if the top-level key doesn't exist.
     */
    public YamlElement gerFromPath(String path) {
        String[] keys = StringUtils.split(path,'.');
        return this.get(keys);
    }

    /**
     * Gets a list of all the sub-Yaml elements contained in a String key, all casted to the Java class represented by the
     * specified Yaml type.
     *
     * Useful under the certainty of the element being a list and all the elements in it are of the same Yaml primitive type.
     *
     * @param key The String key to look for to get the list.
     * @param type The type to get all elements in the list as.
     * @return The list, null if no element under the specified key exists.
     * @throws ClassCastException If the element queried is not a list or any of the elements inside is not of the specified type.
     */
    public <T> List<T> getListOf(String key, YamlElementType<T> type) throws ClassCastException {
        YamlElement element = this.map_.get(key);
        if (element == null) { return null; }
        if (!element.is(YamlElementType.LIST)) { throw new ClassCastException("Not a list."); }
        return element.getListOf(type);
    }

    @Override
    public YamlElement put(String key, YamlElement value) {
        return this.map_.put(key,value);
    }

    @Override
    public YamlElement remove(Object key) {
        return this.map_.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends YamlElement> m) {
        this.map_.putAll(m);
    }

    @Override
    public void clear() {
        this.map_.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.map_.keySet();
    }

    @Override
    public Collection<YamlElement> values() {
        return this.map_.values();
    }

    @Override
    public Set<Entry<String, YamlElement>> entrySet() {
        return this.map_.entrySet();
    }

    private List<YamlElement> createYamlList_(YamlElement element) {
        if (!element.is(YamlElementType.LIST)) {
            throw new IllegalArgumentException("Not a list");
        }
        List<Object> list = (List<Object>) element.element;
        List<YamlElement> r = new ArrayList<>();

        for (Object o : list) {
            r.add(getElement_(o));
        }

        return r;
    }

    private YamlElement getElement_(Object object) {
        YamlElement element = new YamlElement(object);
        if (element.is(YamlElementType.LIST)) {
            element = new YamlElement(this.createYamlList_(element));
        } else if (element.is(YamlElementType.MAP)) {
            element = new YamlElement(new YamlMap((Map<String, Object>) element.element));
        }
        return element;
    }

    @Override
    public String toString() {
        return this.map_.toString();
    }
}
