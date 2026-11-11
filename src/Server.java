// Clase que configura el servidor:
import java.io.File;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Server implements Serializable {

	static final int DEFAULT_PORT = 1099; // Puerto predeterminado.
	static final String DEFAULT_IP = "localhost"; // Ip predeterminada.
	static int portNumber;
	static String ipAddress;
	static final String[] startFlags = {"-s", "-p", "start", "port", "--start", "--port"}; // Banderas para pasar el número de puerto como argumento al Server.
	static final String[] ipFlags = {"-ip", "-host", "--ip", "--host", "ip", "host"}; // Banderas para pasar la ip como argumento al Server.
	static String storagePath = "../ServerStorage/";

	public static void main(String[] args) {
		try {
			parseArguments(args);
			setupServer();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static void parseArguments(String[] args) {
		int argsLength = args.length;
		int i = 0;

		while (i < argsLength) {
			if (Arrays.asList(startFlags).contains(args[i]) && i + 1 < argsLength && isInteger(args[i + 1])) {
				// Si se especifica el número de puerto:
				portNumber = Integer.parseInt(args[i + 1]);
				i += 2;
			} else if (Arrays.asList(ipFlags).contains(args[i]) && i + 1 < argsLength) {
				// Si se especifica la IP:
				if (isValidIPAddress(args[i + 1])) {
						ipAddress = args[i + 1];
						i += 2;
				} else {
					// Si no se pasa una IP válida:
					throw new IllegalArgumentException("La dirección IP proporcionada no es válida.");
				}
			} else {
				// Si se pasa algún argumento no válido:
				throw new IllegalArgumentException("El servidor ha fallado debido a que los argumentos no son válidos.");
			}
		}

		if (portNumber == 0) {
				// Si no se especifica un puerto se utiliza el predeterminado:
				portNumber = DEFAULT_PORT;
		} else if (portNumber <= 1024) {
			// Si se utiliza un puerto reservado:
			portNumber = DEFAULT_PORT;
			System.out.println("Warning: Se asignó el puerto 1099 debido a que el puerto ingresado está reservado.");
		}

		if (ipAddress == null) {
				// Si no se especifica una IP se utiliza la predeterminada:
				ipAddress = DEFAULT_IP;
		}
	}

	// Valida la cadena de IP proporcionada:
	private static boolean isValidIPAddress(String ip) {
		String ipAddressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
															"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
															"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
															"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		String localhostPattern = "^localhost$";
		return ip.matches(ipAddressPattern) || ip.matches(localhostPattern);
	}

	// Configura el servidor:
	private static void setupServer() throws Exception {
		createStorageDirectory(); // Se crea el directorio ServerStorage si no existe.
		Registry reg = LocateRegistry.createRegistry(portNumber);
		RmiImplementation imp = new RmiImplementation(storagePath);
		reg.bind("remoteObject", imp);
		System.out.println("El servidor está listo en la dirección " + ipAddress + " y escuchando en el puerto: " + portNumber);
		System.out.println("La ruta del almacenamiento es: " + storagePath);
	}

	// Crea el directorio ServerStorage en el servidor si este no existe:
	private static void createStorageDirectory() {
		File storageDir = new File(storagePath);
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdirs();
			if (!success) {
				throw new RuntimeException("Error al crear el directorio de almacenamiento: " + storagePath);
			}
		}
	}

	// Valida que la cadena sea un entero:
	private static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
