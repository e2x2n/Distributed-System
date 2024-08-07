package e2x2n.service;

import e2x2n.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageService {

    public static void sendMessage(int senderId, Integer recipientId, Integer groupId, String message) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "INSERT INTO messages " +
                    "(sender_id, recipient_id, group_id, message, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, senderId);
            if (recipientId != null) {
                statement.setInt(2, recipientId);
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            if (groupId != null) {
                statement.setInt(3, groupId);
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setString(4, message);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}