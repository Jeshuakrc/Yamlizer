package com.jkantrell.yamlizer.yaml;

import com.jkantrell.yamlizer.reflect.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public abstract class AbstractYamlConfig {

    //FIELDS
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractYamlConfig.class);

    protected String filePath;
    protected String subPath = "";
    protected final Yamlizer yamlizer = new Yamlizer();

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
    public Logger getLogger() {
        return AbstractYamlConfig.LOGGER;
    }

    //METHODS
    /**
     * Loads the yaml file at the path declared and populates the data into its corresponding fields annotated with @ConfigPopulate.
     *
     * @throws FileNotFoundException if the path provided doesn't lead to any file.
     */
    public void load() throws FileNotFoundException {
        InputStream in = new FileInputStream(this.filePath);
        YamlMap map = new YamlMap(in);

        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) { continue; }

            try {

                Type type = field.getGenericType();
                Object val = this.yamlizer.deserialize(map.get(field.getName()),type);

                field.set(this,val);
                try {
                    LOGGER.info("Setting " + field.getName() + " to " + val.toString());
                } catch (NullPointerException ignore) {}
            } catch (Exception e) {
                LOGGER.warn(
                    "Unable to load " + field.getName() + " due to " + e.getClass().toString() + ". Using default."
                );
                e.printStackTrace();
            }
        }


    }

    /**
     * Saves the current configuration values of this object into the .yaml file defined in the filePath field.
     * If the file doesn't exist, creates it.
     */
    public void save() {
        Map<String, Object> config = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                config.put(field.getName(),field.get(this));
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        try {
            File file = new File(this.filePath);
            if (!file.exists()) {
                file = this.createFile();
            }
            FileWriter writer = new FileWriter(file);
            new Yaml().dump(config,writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new empty yaml file at the declared file path.
     */
    public File createFile() throws IOException {
        File file = new File(this.filePath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }
    /**
     * Places an existing file in the declared file path.
     *
     * @param inputStream The imputeStream from the existing file.
     */
    public void copyFile(InputStream inputStream) throws IOException {
        File outFile = new File(this.filePath);
        outFile.getParentFile().mkdirs();

        OutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
    }

    /**
     * Gets a file from the .jar and copies to the file in the declared file path.
     *
     * @param path the path leading to the file to look for in the .jar.
     */
    public void copyFromResource(String path) throws IOException {
        URL url = this.getClass().getClassLoader().getResource(path);
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        copyFile(connection.getInputStream());
    }

    /**
     * @return true if there's a file corresponding to the declared file path, false otherwise.
     */
    public boolean fileExists() {
        return new File(this.filePath).exists();
    }

    /**
     * Creates a new blank file declared in the declared file path if the file doesn't exist already.
     *
     * @return true if the file was created, false if there was already a file.
     */
    public boolean createIfNonExistent() throws IOException {
        if (this.fileExists()) {
            return false;
        }
        this.createFile();
        return true;
    }

    /**
     * If a file corresponding to the declared file path doesn't exist, then it'll be created and written based on the provided inputStream.
     *
     * @param inputStream The inputStream to read from.
     * @return true if the file was coped, false if there was already a file.
     */
    public boolean cupyIfNonExistent(InputStream inputStream) throws IOException {
        if (this.fileExists()) {
            return false;
        }
        this.copyFile(inputStream);
        return true;
    }

    /**
     * If a file corresponding to the declared file path doesn't exist, then it'll be created and written based on an existent resource in the .jar.
     *
     * @param path the path in the .jar leading to the resource to copy from.
     * @return true if the file was coped, false if there was already a file.
     */
    public boolean fromResourceIfNonExistent(String path) throws IOException {
        if (this.fileExists()) {
            return false;
        }
        this.copyFromResource(path);
        return true;
    }

    public Map<String,Object> values() {
        Map<String,Object> map = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                map.put(field.getName(),field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
