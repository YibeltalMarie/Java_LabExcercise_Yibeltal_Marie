package university.db;

import university.common.TeacherDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsibility: Execute all SQL operations for teachers.
 * No business logic, no RMI, no UI — only database access.
 */
public class TeacherDAO {

    public void insert(TeacherDTO t) throws SQLException {
        String sql = "INSERT INTO teacher (id, name, department) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, t.getId());
            ps.setString(2, t.getName());
            ps.setString(3, t.getDepartment());
            ps.executeUpdate();
        }
    }

    public List<TeacherDTO> findAll() throws SQLException {
        String sql = "SELECT id, name, department FROM teacher";
        List<TeacherDTO> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new TeacherDTO(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("department")
                ));
            }
        }
        return list;
    }
}
