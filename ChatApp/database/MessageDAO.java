package database;

import model.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    
    // Save message to database
    public static boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_type, content, file_name) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, message.getSenderId());
            
            if (message.getReceiverId() == 0) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, message.getReceiverId());
            }
            
            pstmt.setString(3, message.getType());
            pstmt.setString(4, message.getContent() != null ? message.getContent() : "");
            pstmt.setString(5, message.getFileName());
            
            int result = pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                message.setId(rs.getInt(1));
            }
            
            System.out.println("✓ Message saved to DB. ID: " + message.getId());
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("✗ Save message error: " + e.getMessage());
            return false;
        }
    }
    
    // Get recent messages for a user with proper sender/receiver names
    public static List<Message> getRecentMessages(int userId, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "s.username as sender_name, " +
                     "r.username as receiver_name " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.id " +
                     "LEFT JOIN users r ON m.receiver_id = r.id " +
                     "WHERE m.receiver_id IS NULL OR m.receiver_id = ? OR m.sender_id = ? " +
                     "ORDER BY m.timestamp ASC LIMIT ?";

        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, limit);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setType(rs.getString("message_type"));
                msg.setContent(rs.getString("content"));
                msg.setFileName(rs.getString("file_name"));
                msg.setTimestamp(rs.getTimestamp("timestamp"));
                
                msg.setSenderName(rs.getString("sender_name"));
                
                String receiverName = rs.getString("receiver_name");
                if (receiverName != null) {
                    msg.setReceiverName(receiverName);
                } else {
                    msg.setReceiverName("Broadcast");
                }
                
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("Get recent messages error: " + e.getMessage());
        }
        return messages;
    }
    
    // Get conversation between two users
    public static List<Message> getConversation(int userId1, int userId2, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u1.username as sender_name, u2.username as receiver_name " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.id " +
                     "LEFT JOIN users u2 ON m.receiver_id = u2.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.timestamp ASC LIMIT ?";
        
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);
            pstmt.setInt(5, limit);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get conversation error: " + e.getMessage());
        }
        return messages;
    }
    
    private static Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getInt("id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setSenderName(rs.getString("sender_name"));
        
        int receiverId = rs.getInt("receiver_id");
        if (!rs.wasNull()) {
            msg.setReceiverId(receiverId);
            msg.setReceiverName(rs.getString("receiver_name"));
        } else {
            msg.setReceiverId(0);
        }
        
        msg.setType(rs.getString("message_type"));
        msg.setContent(rs.getString("content"));
        msg.setFileName(rs.getString("file_name"));
        msg.setTimestamp(rs.getTimestamp("timestamp"));
        
        return msg;
    }
}