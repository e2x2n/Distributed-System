package e2x2n.service;

import e2x2n.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class GroupService {

    public static void createGroup(String groupName) {
        if (groupName == null) {
            throw new IllegalArgumentException("Group name cannot be null");
        } else if (groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        } else if (isGroupExists(groupName)) {
            throw new IllegalArgumentException("Group name is already taken");
        }

        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "INSERT INTO chat_groups (name, created_at) VALUES (?, CURRENT_TIMESTAMP)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUserToGroup(String groupName, String username) {
        if (groupName == null || username == null) {
            throw new IllegalArgumentException("Group name and username cannot be null");
        } else if (groupName.isEmpty() || username.isEmpty()) {
            throw new IllegalArgumentException("Group name and username cannot be empty");
        } else if (!isGroupExists(groupName)) {
            throw new IllegalArgumentException("Group does not exist");
        } else if (!new UserService().isUserExists(username)) {
            throw new IllegalArgumentException("User does not exist");
        } else if (isUserInGroup(groupName, username)) {
            throw new IllegalArgumentException("User is already in the group");
        }

        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "INSERT INTO group_memberships " +
                    "(user_id, group_id, username, group_name, joined_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, UserService.getUserId(username));
            statement.setInt(2, getGroupId(groupName));
            statement.setString(3, username);
            statement.setString(4, groupName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeUserFromGroup(String groupName, String username) {
        if (groupName == null || username == null) {
            throw new IllegalArgumentException("Group name and username cannot be null");
        } else if (groupName.isEmpty() || username.isEmpty()) {
            throw new IllegalArgumentException("Group name and username cannot be empty");
        } else if (!isGroupExists(groupName)) {
            throw new IllegalArgumentException("Group does not exist");
        } else if (!new UserService().isUserExists(username)) {
            throw new IllegalArgumentException("User does not exist");
        } else if (!isUserInGroup(groupName, username)) {
            throw new IllegalArgumentException("User is not in the group");
        }

        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "DELETE FROM group_memberships WHERE user_id = ? AND group_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, UserService.getUserId(username));
            statement.setInt(2, getGroupId(groupName));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> getGroupMembers(String groupName) {
        Set<String> members = new HashSet<>();
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT username FROM group_memberships WHERE group_name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                members.add(resultSet.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public static boolean isGroupExists(String groupName) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT * FROM chat_groups WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUserInGroup(String groupName, String username) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT * FROM group_memberships WHERE user_id = ? AND group_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, UserService.getUserId(username));
            statement.setInt(2, getGroupId(groupName));
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getGroupId(String groupName) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "SELECT id FROM chat_groups WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
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
