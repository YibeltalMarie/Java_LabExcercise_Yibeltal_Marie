package university.db;

import university.common.StudentDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsibility: Execute all SQL operations for students.
 * No business logic, no RMI, no UI — only database access.
 */
public class StudentDAO {

    public void insert(StudentDTO s) throws SQLException {
        String sql = "INSERT INTO students (id, name, department, section, year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getDepartment());
            ps.setString(4, s.getSection());
            ps.setInt(5, s.getYear());
            ps.executeUpdate();
        }
    }

    public List<StudentDTO> findAll() throws SQLException {
        String sql = "SELECT id, name, department, section, year FROM students";
        List<StudentDTO> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new StudentDTO(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getString("section"),
                    rs.getInt("year")
                ));
            }
        }
        return list;
    }
}
