package hr.algebra.battleship.jndi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationReader {

    private static Properties properties;

    static {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.txt")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringValueForKey(ConfigurationKey key) {
        return (String) properties.get(key.getKey());
    }

    public static Integer getIntegerValueForKey(ConfigurationKey key) {
        return Integer.valueOf((String) properties.get(key.getKey()));
    }
}
