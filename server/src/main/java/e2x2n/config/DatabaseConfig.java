package e2x2n.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final String PROPERTY_FILE = "db.properties";
    private static String url;
    private static String username;
    private static String password;

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(PROPERTY_FILE)) {
            Properties properties = new Properties();
            if (input == null) {
                throw new IOException("Sorry, unable to find " + PROPERTY_FILE);
            }
            properties.load(input);
            url = properties.getProperty("db.url");
            username = properties.getProperty("db.user");
            password = properties.getProperty("db.password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}