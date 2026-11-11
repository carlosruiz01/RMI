// Esta clase conecta con el servidor y acepta comandos desde el usuario.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
	private static final String SERVER_PATH = "../ServerStorage/";
	private static final String UPLOAD = "upload";
	private static final String DOWNLOAD = "download";
	private static final String LS = "ls";
	private static final String DIR = "dir";
	private static final String MKDIR = "mkdir";
	private static final String RMDIR = "rmdir";
	private static final String RM = "rm";
	private static final String SHUTDOWN = "shutdown";
	private static ArrayList<String> LISTFILE = new ArrayList<String>();
	private static String hostname = "localhost";
	private static int portnumber = 1099;

	public static void main(String[] args) {
		List<String> validCommands = Arrays.asList(UPLOAD, DOWNLOAD, DIR, LS, MKDIR, RMDIR, RM, SHUTDOWN);

		try {
			validateArguments(args);

			String command = args[0];
			validateCommand(command, validCommands);

			String environment = System.getenv("SERVER_PORT"); // Variable de entorno SERVER_PORT.
			if (environment != null) {
				String[] parts = environment.split(":");
				hostname = parts[0];
				portnumber = Integer.parseInt(parts[1]);
				System.out.println("Variable de entorno: " + environment);
			}
			System.out.println("Buscando conexión en " + hostname + ":" + portnumber);
			Registry myreg = LocateRegistry.getRegistry(hostname, portnumber);
			RmiInterface inter = (RmiInterface) myreg.lookup("remoteObject");

			executeCommand(command, args, inter);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// Valida los argumentos pasados al programa principal del Cliente:
	private static void validateArguments(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			throw new Exception("No se pasó ningún comando.");
		}
	}

	// Valida el comando ingresado:
	private static void validateCommand(String command, List<String> validCommands) throws Exception {
		if (!validCommands.contains(command)) {
			throw new Exception("Comando no válido.");
		}
	}

	// Ejecuta el comando especificado por el cliente en el servidor:
	private static void executeCommand(String command, String[] args, RmiInterface inter) throws Exception {
		switch (command) {
			case UPLOAD:
				uploadFile(args, inter);
				break;
			case DOWNLOAD:
				downloadFile(args, inter);
				break;
			case LS:
				listFiles(args, inter);
				break;
			case DIR:
				printServerPath(args);
				break;
			case MKDIR:
				createDirectory(args, inter);
				break;
			case RMDIR:
			case RM:
				removeDirectoryOrFile(args, inter);
				break;
			case SHUTDOWN:
				shutdownClient();
				break;
			default:
				throw new Exception("Comando no válido.");
		}
	}

	// Carga un archivo al servidor:
	private static void uploadFile(String[] args, RmiInterface inter) throws IOException, RemoteException {
		if (args.length < 2 || args.length > 3) {
			System.out.println("Error: No se puede cargar el archivo. Número de argumentos incorrecto.");
			return;
		}

		String clientPath = args[1];
		File clientPathFile = new File(clientPath);
		if (!clientPathFile.isFile()) {
			System.out.println("Error: No se puede cargar el archivo, ya que este no existe.");
			return;
		}

		String serverPath = (args.length == 3) ? getServerPath(args[2], clientPathFile) : getDefaultServerPath(clientPathFile);

		byte[] myData = new byte[(int) clientPathFile.length()];
		try (FileInputStream in = new FileInputStream(clientPathFile)) {
			System.out.println("Cargando al servidor...");
			in.read(myData, 0, myData.length);
		}

		inter.uploadFileToServer(myData, serverPath, myData.length);
		System.out.println("Archivo cargado al servidor en: " + serverPath);
	}

	// Modifica la ruta del servidor, si esta no termina en "/" se añade:
	private static String getServerPath(String specifiedPath, File clientPathFile) {
		if (!specifiedPath.endsWith("/")) {
			specifiedPath = specifiedPath + "/";
		}
		return specifiedPath + clientPathFile.getName();
	}

	// Obtiene la ruta del servidor por defecto:
	private static String getDefaultServerPath(File clientPathFile) {
		return SERVER_PATH + clientPathFile.getName();
	}

	// Descarga un archivo del servidor:
	private static void downloadFile(String[] args, RmiInterface inter) throws IOException, RemoteException {
		if (args.length != 3) {
			System.out.println("Error: No es posible descargar el archivo.");
			return;
		}

		String serverPath = args[1];
		String clientPath = args[2];

		//File serverPathFile = new File(serverPath);
		//if (!serverPathFile.isFile()) {
		//	System.out.println("Error: No es posible descargar el archivo, ya que este no existe en el servidor.");
		//	return;
		//}

		File clientPathFile = new File(clientPath);
		File parentDirectory = clientPathFile.getParentFile();

		if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
			System.out.println("Error: No se pudo crear el directorio: " + parentDirectory.getAbsolutePath());
			return;
		}

		byte[] myData = inter.downloadFileFromServer(serverPath);

		try (FileOutputStream out = new FileOutputStream(clientPathFile)) {
			System.out.println("Descargando...");
			out.write(myData);
			System.out.println("Archivo descargado en: " + clientPathFile);
		}
	}

	// Enlista los archivos y directorios de una ruta del almacenamiento del servidor:
	private static void listFiles(String[] args, RmiInterface inter) throws RemoteException {
		if (args.length > 2) {
			// Si hay más de un argumento, indicar error y salir
			System.out.println("Error: Demasiados argumentos para el comando " + args[0]);
			return;
		}

		String serverPath = SERVER_PATH;
		if (args.length == 2) {
			serverPath += args[1];
		}

		LISTFILE.clear();

		String[] fileList = inter.listFiles(serverPath);
		if (fileList == null) {
			// Si la ruta generada no es válida
			System.out.println(args[0] + ": Directorio " + args[1] + " no válido.");
			LISTFILE.add(args[0] + ": Directorio " + args[1] + " no válido.");
			return;
		}

		if (fileList.length == 0) {
			System.out.println("El directorio está vacío.");
			LISTFILE.add("El directorio está vacío.");
			return;
		}

		for (String fileName : fileList) {
			String fullPath = serverPath + fileName;
			if (new File(fullPath).isDirectory()) {
					System.out.println("/" + fileName);
					LISTFILE.add("/" + fileName);
			} else {
					System.out.println(fileName);
					LISTFILE.add(fileName);
			}
		}
	}

	// Imprime el directorio donde están alojados los archivos del servidor:
	private static void printServerPath(String[] args) {
		if (args != null && args.length > 1) {
			System.out.println("Error: Argumentos no válidos para el comando " + args[0] + ": " + Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
		} else {
			System.out.println("Ruta del servidor: " + SERVER_PATH);
		}
	}
	
	// Crea un directorio dentro del almacenamiento del servidor:
	private static void createDirectory(String[] args, RmiInterface inter) throws RemoteException {
		if (args.length != 2) {
			System.out.println("Error: No se proporcionó un nombre para el directorio a crear.");
			return;
		}

		String directoryName = args[1];
		boolean isCreated = inter.createDirectory(directoryName);

		if (isCreated) {
			System.out.println("El directorio ha sido creado exitosamente.");
		} else {
			System.out.println("No se pudo crear el directorio. Verifica si ya existe o los permisos de escritura.");
		}
	}

	// Elimina un directorio dentro del almacenamiento del servidor:
	private static void removeDirectoryOrFile(String[] args, RmiInterface inter) throws RemoteException {
		if (args.length != 2) {
			System.out.println("Error: No se proporcionó un directorio o archivo para eliminar.");
			return;
		}

		String path = args[1];
		boolean isRemoved = inter.removeDirectoryOrFile(path);

		if (isRemoved) {
			System.out.println("El directorio o archivo ha sido eliminado exitosamente.");
		} else {
			System.out.println("No se pudo eliminar el directorio o archivo. Verifica la existencia y los permisos.");
		}
	}

	// Apaga el cliente:
	private static void shutdownClient() {
		System.out.println("El cliente se ha apagado. Cierre la consola/terminal.");
		System.exit(0);
	}

	public static ArrayList<String> getListFiles(){
		return LISTFILE;
	}

	// Imprime el directorio donde están alojados los archivos del servidor:
	public static String getServerPath() {
		return SERVER_PATH;
	}

	// Indica la IP del servidor
	public static void setHostIP(String HostIP){
		hostname = HostIP;
	}
	// Indica el puerto del servidor
	public static void setPort(int port){
		portnumber = port;
	}
}


