package university.common;

import java.io.Serializable;

/**
 * Data Transfer Object for Teacher.
 * Must be Serializable so RMI can pass it over the network.
 */
public class TeacherDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int    id;
    private final String name;
    private final String department;

    public TeacherDTO(int id, String name, String department) {
        this.id         = id;
        this.name       = name;
        this.department = department;
    }

    public int    getId()         { return id; }
    public String getName()       { return name; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return String.format("%-4d %-20s %-10s", id, name, department);
    }
}
