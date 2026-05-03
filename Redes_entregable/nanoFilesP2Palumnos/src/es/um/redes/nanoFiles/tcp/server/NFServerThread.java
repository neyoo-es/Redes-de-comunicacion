
package es.um.redes.nanoFiles.tcp.server;
 
import java.net.Socket;
 
public class NFServerThread extends Thread {
	/*
	 * Hilo creado desde NFServer para atender a un cliente concreto.
	 * Recibe el socket de conexión con ese cliente (devuelto por accept) y delega
	 * en NFServer.serveFilesToClient para gestionar toda la comunicación.
	 */
	private final Socket clientSocket;
 
	public NFServerThread(Socket socket) {
		this.clientSocket = socket;
	}
 
	@Override
	public void run() {
		NFServer.serveFilesToClient(clientSocket);
	}
}
 