package es.um.redes.nanoFiles.logic;
 
import java.net.InetSocketAddress;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.util.FileInfo;
 
import es.um.redes.nanoFiles.tcp.server.NFServer;
 
public class NFControllerLogicP2P {
	// Servidor TCP local para compartir ficheros con otros peers
	private NFServer fileServer = null;
 
 
 
	protected NFControllerLogicP2P() {
	}
 
	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		boolean serverRunning = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
		} else {
			/*
			 * TODO: (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
			 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
			 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
			 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
			 * y tratadas en este método. Si se produce una excepción de entrada/salida
			 * (error del que no es posible recuperarse), se debe informar sin abortar el
			 * programa
			 * 
			 */
			try {
			    fileServer = new NFServer();
			    Thread serverThread = new Thread(fileServer);
			    serverThread.setDaemon(true);   // muere cuando termina el hilo principal
			    serverThread.start();
			    int port = fileServer.getPort();
			    if (port > 0) {
			        System.out.println("* File server listening on port " + port);
			        serverRunning = true;
			    } else {
			        System.err.println("* File server failed to bind to a valid port");
			        fileServer = null;
			    }
			} catch (IOException e) {
			    System.err.println("* Cannot start the file server: " + e.getMessage());
			    fileServer = null;
			}
 
		}
		return serverRunning;
 
	}
 
	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {
 
			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}
 
	public void testTCPClient() {
 
		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */
 
		    try {

		        int port;
		        if(fileServer != null && fileServer.getPort() > 0) { port = fileServer.getPort(); }else {port = NFServer.PORT;}
		        NFConnector nfConnector = new NFConnector(new InetSocketAddress(port));
		        nfConnector.test();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	}
	/**
	 * Método para listar los ficheros de un peer concreto vía TCP e imprimirlos por
	 * pantalla.
	 * 
	 * @param La dirección del peer cuyos ficheros se quiere listar
	 * @return Verdadero si se ha obtenido exitosamente el listado de fichero del
	 *         peer
	 */
	protected boolean listPeerFiles(InetSocketAddress peerAddr) {
		boolean success = false;
 
		try {
		    NFConnector connector = new NFConnector(peerAddr);
		    FileInfo[] files = connector.getFileList();
		    if (files != null) {
		        System.out.println("* Files available at peer " + peerAddr + ":");
		        FileInfo.printToSysout(files);
		        success = true;
		    } else {
		        System.err.println("* Could not retrieve file list from peer " + peerAddr);
		    }
		    connector.disconnect();
		} catch (IOException e) {
		    System.err.println("* Error connecting to peer " + peerAddr + ": " + e.getMessage());
		}
		return success;
	}
 
	/**
	 * Descarga un fichero identificado por subcadena de hash desde uno o varios
	 * peers. Si se pasa "*" como nickname, usa el directorio para localizar los
	 * peers que tienen el hash.
	 */
	protected boolean downloadFromPeers(NFControllerLogicDir dirLogic, String targetPeerNickname,
			String targetHashSubstring) {
		// TODO: localizar peers con el hash solicitado (o uno concreto) y delegar en
		// downloadFileFromServers
		boolean success = false;
		java.util.Map<String, InetSocketAddress> peers = dirLogic.fetchPeerList();
		if (peers == null || peers.isEmpty()) {
		    System.err.println("* No peers registered in the directory");
		    return false;
		}
 
		InetSocketAddress[] serverAddresses;
 
		if (targetPeerNickname.equals("*")) {
		    // Usar todos los peers registrados
		    serverAddresses = peers.values().toArray(new InetSocketAddress[0]);
		} else {
		    // Usar peer que tenga el nickname
		    InetSocketAddress addr = peers.get(targetPeerNickname);
		    if (addr == null) {
		        System.err.println("* Peer '" + targetPeerNickname + "' not found in directory");
		        return false;
		    }
		    serverAddresses = new InetSocketAddress[] { addr };
		}
 
		success = downloadFileFromServers(serverAddresses, targetHashSubstring);
		return success;
	}
 
	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList   La lista de direcciones de los servidores a los
	 *                            que se conectará
	 * @param targetHashSubstring Subcadena del hash del fichero a descargar
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetHashSubstring) {
		boolean downloaded = false;
 
		if (serverAddressList.length == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		// TODO: crear conectores TCP solo a los servidores que confirmen el hash
		// pedido, obtener nombre remoto, reservar nombre local sin colisiones, alternar
		// descarga de chunks y verificar hash final. Cerrar los sockets al terminar.
		if (serverAddressList.length == 0) {
		    System.err.println("* Cannot start download - No list of server addresses provided");
		    return false;
		}
 
		// 1. Conectar solo a los servidores que confirman tener el hash pedido
		java.util.List<NFConnector> activeConnectors = new java.util.ArrayList<>();
		String remoteFileName = null;
		long remoteFileSize = -1;
 
		for (InetSocketAddress serverAddr : serverAddressList) {
		    try {
		        NFConnector connector = new NFConnector(serverAddr);
		        // Comprobar si el servidor tiene el fichero con ese hash
		        if (connector.hasFile(targetHashSubstring)) {
		            if (remoteFileName == null) {
		                remoteFileName = connector.getRemoteFileName(targetHashSubstring);
		                remoteFileSize  = connector.getRemoteFileSize(targetHashSubstring);
		            }
		            activeConnectors.add(connector);
		            System.out.println("* Server " + serverAddr + " has the file — added to download list");
		        } else {
		            connector.disconnect();
		        }
		    } catch (IOException e) {
		        System.err.println("* Could not connect to " + serverAddr + ": " + e.getMessage());
		    }
		}
 
		if (activeConnectors.isEmpty()) {
		    System.err.println("* No server has the requested file (hash: " + targetHashSubstring + ")");
		    return false;
		}
 
		// 2. Reservar nombre local sin colisiones
		java.nio.file.Path sharedDir = java.nio.file.Paths.get(NanoFiles.sharedDirname);
		try {
			if (!java.nio.file.Files.exists(sharedDir)) {
				java.nio.file.Files.createDirectories(sharedDir);
			}
		} catch (IOException ignored) {}
		java.nio.file.Path localPath = es.um.redes.nanoFiles.util.FileNameUtil.chooseAvailableName(sharedDir.resolve(remoteFileName).toString());
 
		// Descarga alternando chunks entre los servidores disponibles
		try {
		    int chunkSize  = 65536;          // 64 KB por chunk
		    long offset    = 0;
		    int  serverIdx = 0;
		    int  numServers = activeConnectors.size();
 
		    java.io.RandomAccessFile raf =
		            new java.io.RandomAccessFile(localPath.toFile(), "rw");
		    raf.setLength(remoteFileSize);
 
		    System.out.println("* Downloading '" + remoteFileName + "' (" + remoteFileSize
		            + " bytes) from " + numServers + " server(s)...");
 
		    while (offset < remoteFileSize) {
		        NFConnector current = activeConnectors.get(serverIdx % numServers);
		        int toRead = (int) Math.min(chunkSize, remoteFileSize - offset);
 
		        byte[] chunk = current.downloadChunk(targetHashSubstring, offset, toRead);
		        if (chunk == null || chunk.length == 0) {
		            System.err.println("* Error downloading chunk at offset " + offset);
		            raf.close();
		            return false;
		        }
		        raf.seek(offset);
		        raf.write(chunk, 0, chunk.length);
		        offset += chunk.length;
		        serverIdx++;
		    }
		    raf.close();
 
		    // Verificar hash 
		    String computedHash =
		            es.um.redes.nanoFiles.util.FileDigest.computeFileChecksumString(
		                    localPath.toString());
		    System.out.println("* File saved to: " + toDisplayPath(localPath)
		            + " (" + remoteFileSize + " bytes)");
		    if (computedHash.contains(targetHashSubstring)) {
		        System.out.println("* Hash verified: " + computedHash);
		        downloaded = true;
		        NanoFiles.db = new es.um.redes.nanoFiles.util.FileDatabase(NanoFiles.sharedDirname);
		    } else {
		        System.err.println("* WARNING: computed hash (" + computedHash
		                + ") does not match requested substring (" + targetHashSubstring + ")");
		    }
		} catch (IOException e) {
		    System.err.println("* I/O error during download: " + e.getMessage());
		} finally {
		    //  Cerrar los sockets al terminar
		    for (NFConnector c : activeConnectors) {
		        try { c.disconnect(); } catch (IOException ignored) {}
		    }
		}
		return downloaded;
	}
 
	private String toDisplayPath(java.nio.file.Path path) {
		java.nio.file.Path abs = path.toAbsolutePath().normalize();
		java.nio.file.Path cwd = java.nio.file.Paths.get("").toAbsolutePath().normalize();
		if (abs.startsWith(cwd)) {
			return cwd.relativize(abs).toString();
		}
		return path.toString();
	}
 
	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		int port = 0;
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros
		 */
		if (fileServer != null) {
		    port = fileServer.getPort();
		}
 
		return port;
	}
 
	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		if (fileServer != null) {
		    fileServer.stop();
		    fileServer = null;
		    System.out.println("* File server stopped");
		}
 
 
	}
 
	protected boolean serving() {
		boolean result = false;
 
		result = (fileServer != null);
		return result;
 
	}
 
}