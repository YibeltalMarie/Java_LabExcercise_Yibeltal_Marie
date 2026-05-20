package university.common;

import java.io.Serializable;

/**
 * Data Transfer Object for Student.
 * Must be Serializable so RMI can pass it over the network.
 */
public class StudentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int    id;
    private final String name;
    private final String department;
    private final String section;
    private final int    year;

    public StudentDTO(int id, String name, String department, String section, int year) {
        this.id         = id;
        this.name       = name;
        this.department = department;
        this.section    = section;
        this.year       = year;
    }

    public int    getId()         { return id; }
    public String getName()       { return name; }
    public String getDepartment() { return department; }
    public String getSection()    { return section; }
    public int    getYear()       { return year; }

    @Override
    public String toString() {
        return String.format("%-4d %-15s %-10s %-8s %d", id, name, department, section, year);
    }
}
