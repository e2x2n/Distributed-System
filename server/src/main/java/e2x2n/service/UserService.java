package e2x2n.service;

import e2x2n.config.DatabaseConfig;
import e2x2n.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public static void registerUser(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        } else if (username.isEmpty() || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        } else if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "INSERT INTO users (username, password, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean authenticateUser(String username, String rawPassword) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT password FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                return PasswordUtil.verifyPassword(rawPassword, hashedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUserExists(String username) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUsernameTaken(String username) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getUserId(String username) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
