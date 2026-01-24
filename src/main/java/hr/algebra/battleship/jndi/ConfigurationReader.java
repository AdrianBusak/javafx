package hr.algebra.battleship.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationReader {

    private ConfigurationReader() {
    }

    private static Properties properties;

    static {
        properties = new Properties();

        Properties configurationProperties = new Properties();
        configurationProperties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        configurationProperties.put(Context.PROVIDER_URL, "file:./conf/");

        try (InitialDirContextCloseable context = new InitialDirContextCloseable(configurationProperties)) {
            Object configurationObject = context.lookup("config.txt");
            properties.load(new FileReader(configurationObject.toString()));
            System.out.println("✅ Konfiguracija učitana via JNDI FSContext");
        } catch (NamingException | IOException e) {
            System.err.println("❌ Greška pri učitavanju konfiguracije: " + e.getMessage());
            System.err.println("📍 Kreiraj datoteku: ./conf/config.txt");
            System.err.println("Sadržaj:");
            System.err.println("hostname=localhost");
            System.err.println("player1.server.port=6000");
            System.err.println("player2.server.port=6001");
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