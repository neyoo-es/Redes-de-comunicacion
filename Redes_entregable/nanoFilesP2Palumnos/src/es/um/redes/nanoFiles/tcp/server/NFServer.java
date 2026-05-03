package es.um.redes.nanoFiles.tcp.server;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
 
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;
 
public class NFServer implements Runnable {
 
	public static final int PORT = 10000;
 
 
 
	private ServerSocket serverSocket = null;
 
	public NFServer() throws IOException {
		// Crear socket servidor ligado al puerto especificado (PORT), como usamos un puerto fijo no lo modificamos
		serverSocket = new ServerSocket(0);
	}
 
	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}
 
		while (true) {
			try {
				// Esperar la conexión de un peer cliente
				Socket clientSocket = serverSocket.accept();
				System.out.println("[fileServerTestMode] Client connected from "
						+ clientSocket.getRemoteSocketAddress());
				/*
				 * Punto 5: eco de entero para prueba básica TCP.
				 * Punto 8: una vez desactivado testModeTCP, sustituir el bloque de eco
				 * por la llamada a serveFilesToClient(clientSocket) para usar mensajes
				 * binarios del protocolo PeerMessage diseñado.
				 */
				if (NanoFiles.testModeTCP) {
					DataInputStream  tdis = new DataInputStream(clientSocket.getInputStream());
					DataOutputStream tdos = new DataOutputStream(clientSocket.getOutputStream());
					int value = tdis.readInt();
					System.out.println("[fileServerTestMode] Received integer: " + value + " — echoing back");
					tdos.writeInt(value);
					tdos.flush();
					clientSocket.close();
				} else {
					serveFilesToClient(clientSocket);
				}
			} catch (IOException e) {
				System.err.println("[fileServerTestMode] Error: " + e.getMessage());
			}
		}
	}
 
	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (serverSocket == null || serverSocket.isClosed()) {
			System.err.println("NFServer.run: serverSocket not available");
			return;
		}
		System.out.println("* NFServer running on " + serverSocket.getLocalSocketAddress());
		while (!serverSocket.isClosed()) {
			try {
				// Esperar conexión de un cliente
				Socket clientSocket = serverSocket.accept();
				System.out.println("* Client connected from " + clientSocket.getRemoteSocketAddress());
				// Crear un hilo nuevo para el cliente
				NFServerThread t = new NFServerThread(clientSocket);
				t.setDaemon(true);
				t.start();
			} catch (IOException e) {
				if (serverSocket.isClosed()) {
					System.out.println("* NFServer stopped.");
				} else {
					System.err.println("* NFServer accept error: " + e.getMessage());
				}
			}
		}
	}
 
	/** Devuelve el puerto de escucha del servidor (0 si no está activo). */
	public int getPort() {
		if (serverSocket != null && serverSocket.isBound()) {
			return serverSocket.getLocalPort();
		}
		return 0;
	}
 
	/** Detiene el servidor cerrando el ServerSocket. */
	public void stop() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
			System.err.println("* NFServer.stop error: " + e.getMessage());
		}
	}
 
 
 
 
	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		try {
			DataInputStream  dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
 
			boolean clientConnected = true;
			while (clientConnected) {
				// Leer el siguiente mensaje del cliente
				PeerMessage request = PeerMessage.readMessageFromInputStream(dis);
				byte opcode = request.getOpcode();
 
				switch (opcode) {
 
				case PeerMessageOps.OPCODE_GET_FILE_LIST: {
					// Responder con la lista de ficheros compartidos
					FileInfo[] files = NanoFiles.db.getFiles();
					PeerMessage response = new PeerMessage(PeerMessageOps.OPCODE_FILE_LIST, files);
					response.writeMessageToOutputStream(dos);
					dos.flush();
					break;
				}
 
				case PeerMessageOps.OPCODE_HAS_FILE: {
					String hash = request.getHashSubstring();
					FileInfo match = findByHashSubstring(NanoFiles.db.getFiles(), hash);
					byte replyCode = (match != null)
							? PeerMessageOps.OPCODE_HAS_FILE_YES
							: PeerMessageOps.OPCODE_HAS_FILE_NO;
					PeerMessage response = new PeerMessage(replyCode);
					response.writeMessageToOutputStream(dos);
					dos.flush();
					break;
				}
 
				case PeerMessageOps.OPCODE_GET_FILE_INFO: {
					String hash = request.getHashSubstring();
					FileInfo match = findByHashSubstring(NanoFiles.db.getFiles(), hash);
					PeerMessage response;
					if (match != null) {
						response = new PeerMessage(PeerMessageOps.OPCODE_FILE_INFO,
								match.fileName, match.fileSize, match.fileHash);
					} else {
						response = new PeerMessage(PeerMessageOps.OPCODE_ERROR);
					}
					response.writeMessageToOutputStream(dos);
					dos.flush();
					break;
				}
				
				case PeerMessageOps.OPCODE_DOWNLOAD_CHUNK: {
					String hash   = request.getHashSubstring();
					long   offset = request.getOffset();
					int    length = request.getLength();
					FileInfo match = findByHashSubstring(NanoFiles.db.getFiles(), hash);
					PeerMessage response;
					if (match != null) {
						String filePath = NanoFiles.db.lookupFilePath(match.fileHash);
						if (filePath != null) {
							try (java.io.RandomAccessFile raf =
									new java.io.RandomAccessFile(filePath, "r")) {
								raf.seek(offset);
								int toRead = (int) Math.min(length, match.fileSize - offset);
								byte[] chunk = new byte[toRead];
								int read = raf.read(chunk, 0, toRead);
								if (read < toRead) {
									chunk = java.util.Arrays.copyOf(chunk, read);
								}
								response = new PeerMessage(PeerMessageOps.OPCODE_CHUNK_DATA, chunk);
							}
						} else {
							response = new PeerMessage(PeerMessageOps.OPCODE_ERROR);
						}
					} else {
						response = new PeerMessage(PeerMessageOps.OPCODE_ERROR);
					}
					response.writeMessageToOutputStream(dos);
					dos.flush();
					break;
				}
 
				default:
					System.err.println("* serveFilesToClient: unexpected opcode " + opcode);
					clientConnected = false;
					break;
				}
			}
		} catch (java.io.EOFException e) {
			// El cliente cerró la conexión; es el final normal
			System.out.println("* Client disconnected from " + socket.getRemoteSocketAddress());
		} catch (IOException e) {
			System.err.println("* serveFilesToClient I/O error: " + e.getMessage());
		} finally {
			try { socket.close(); } catch (IOException ignored) {}
		}
	}
	
	private static FileInfo findByHashSubstring(FileInfo[] files, String hashSubstring) {
		if (files == null || hashSubstring == null) return null;
		for (FileInfo f : files) {
			if (f.fileHash != null && f.fileHash.contains(hashSubstring)) {
				return f;
			}
		}
		return null;
	}
 
 
 
 
}
