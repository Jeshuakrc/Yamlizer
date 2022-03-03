package com.jkantrell.pluginCommons.Config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

public abstract class AbstractYamlConfig {

    //FIELDS
    protected String filePath = "";
    protected String subPath = "";

    //CONSTRUCTORS

    /**
     * Creates a new config object, which is an abstraction from a yaml file.
     *
     * @param filePath the Path to look for or create the config yaml file.
     */
    public AbstractYamlConfig(String filePath) {
        this.filePath = filePath;
    }

    //SETTERS
    /**
     * Sets a new path to store the configuration.
     *
     * @param path the path.
     */
    public void setFilePath(String path) {
        this.filePath = path;
    }

    /**
     * Sets a sub-path inside the yaml configuration where all fields in this cass will be stored and deserialized.
     *
     * @param subPath the sub-path.
     */
    public void setSubPath(String subPath) {
        this.filePath = subPath;
    }

    //GETTERS
    /**
     * Gets the path where the config object is currently storing the configuration.
     *
     * @return the file path.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the sub-path in which the configuration is being stored.
     *
     * @return the sub-path.
     */
    public String getSubPath() {
        return subPath;
    }

    //METHODS
    /**
     * Loads the yaml file at the path declared and populates the data into its corresponding fields annotated with @ConfigPopulate.
     * 
     * @throws FileNotFoundException if the path provided doesn't lead to any file.
     */
    public void load() throws FileNotFoundException {
        Yaml yamlConfig = new Yaml();

        InputStream in = new FileInputStream(this.filePath);
        Map<String,Object> yamlMap = yamlConfig.load(in);

        StringBuilder path = new StringBuilder();
        for (Field field : this.getClass().getDeclaredFields()) {
            path.setLength(0);
            path.append(this.subPath).append(field.getName());
            field.setAccessible(true);
            try {
                field.set(this, yamlMap.get(path.toString()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
