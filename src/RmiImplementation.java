// Esta clase contiene la implementación de todas las funcionalidades provistas al cliente.
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiImplementation extends UnicastRemoteObject implements RmiInterface, Serializable {

	public RmiImplementation(String str) throws RemoteException {
		File storageDir = new File(str);
		storageDir.mkdirs(); // Utiliza mkdirs() para crear directorios recursivamente si es necesario.
	}

	public void uploadFileToServer(byte[] myData, String serverPath, int length) throws RemoteException {
		try (FileOutputStream out = new FileOutputStream(serverPath)) {
			out.write(myData);
			System.out.println("Se ha completado la escritura de la información...");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
		}
	}

	public byte[] downloadFileFromServer(String serverPath) throws RemoteException {
		byte[] myData = null;
		File serverPathFile = new File(serverPath);

		try (FileInputStream in = new FileInputStream(serverPathFile)) {
			myData = new byte[(int) serverPathFile.length()];
			in.read(myData, 0, myData.length);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
		}
		return myData;
	}

	public String[] listFiles(String serverPath) throws RemoteException {
		File serverPathDir = new File(serverPath);
		return serverPathDir.list();
	}

	public boolean createDirectory(String serverPath) throws RemoteException {
		File serverPathDir = new File(serverPath);
		return serverPathDir.mkdir();
	}

	public boolean removeDirectoryOrFile(String serverPath) throws RemoteException {
		File serverPathDir = new File(serverPath);
		return serverPathDir.delete();
	}
}
