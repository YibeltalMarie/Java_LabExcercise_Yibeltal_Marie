package university.server;

import university.common.UniversityService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Responsibility: Start the RMI registry and register the service.
 * Run this first, then run the client.
 */
public class ServerMain {

    public static final int    PORT         = 1099;
    public static final String SERVICE_NAME = "UniversityService";

    public static void main(String[] args) {
        try {
            // 1. Create the service implementation
            UniversityService service = new UniversityServiceImpl();

            // 2. Start the RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(PORT);

            // 3. Bind the service under a known name
            registry.rebind(SERVICE_NAME, service);

            System.out.println("========================================");
            System.out.println("  University RMI Server is running...");
            System.out.println("  Port    : " + PORT);
            System.out.println("  Service : " + SERVICE_NAME);
            System.out.println("========================================");
        } catch (Exception e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
