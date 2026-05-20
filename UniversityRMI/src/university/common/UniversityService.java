package university.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI Remote Interface — the shared contract between client and server.
 * Both sides depend only on this interface, never on each other directly.
 */
public interface UniversityService extends Remote {

    // ── Student operations ──────────────────────────────────────────────────
    void addStudent(StudentDTO student) throws RemoteException;
    List<StudentDTO> getStudents()       throws RemoteException;

    // ── Teacher operations ──────────────────────────────────────────────────
    void addTeacher(TeacherDTO teacher)  throws RemoteException;
    List<TeacherDTO> getTeachers()       throws RemoteException;
}
