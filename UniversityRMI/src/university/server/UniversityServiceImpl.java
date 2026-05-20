package university.server;

import university.common.UniversityService;
import university.common.StudentDTO;
import university.common.TeacherDTO;
import university.db.StudentDAO;
import university.db.TeacherDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Responsibility: Implement the RMI remote interface.
 * Delegates all data work to DAOs — no SQL lives here.
 */
public class UniversityServiceImpl extends UnicastRemoteObject
                                   implements UniversityService {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    public UniversityServiceImpl() throws RemoteException {
        super();
    }

    // ── Student operations ──────────────────────────────────────────────────

    @Override
    public void addStudent(StudentDTO student) throws RemoteException {
        try {
            studentDAO.insert(student);
            System.out.println("[Server] Student added: " + student.getName());
        } catch (Exception e) {
            throw new RemoteException("Failed to add student: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StudentDTO> getStudents() throws RemoteException {
        try {
            return studentDAO.findAll();
        } catch (Exception e) {
            throw new RemoteException("Failed to fetch students: " + e.getMessage(), e);
        }
    }

    // ── Teacher operations ──────────────────────────────────────────────────

    @Override
    public void addTeacher(TeacherDTO teacher) throws RemoteException {
        try {
            teacherDAO.insert(teacher);
            System.out.println("[Server] Teacher added: " + teacher.getName());
        } catch (Exception e) {
            throw new RemoteException("Failed to add teacher: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TeacherDTO> getTeachers() throws RemoteException {
        try {
            return teacherDAO.findAll();
        } catch (Exception e) {
            throw new RemoteException("Failed to fetch teachers: " + e.getMessage(), e);
        }
    }
}
