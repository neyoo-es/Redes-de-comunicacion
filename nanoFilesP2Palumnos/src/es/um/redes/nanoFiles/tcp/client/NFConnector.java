package es.um.redes.nanoFiles.tcp.client;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
 
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;
 
//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream  dis;
	private DataOutputStream dos;
 
	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		// Crear el socket TCP; la conexión se establece en la construcción
		socket = new Socket(fserverAddr.getAddress(), fserverAddr.getPort());
		// Crear los streams de entrada/salida a partir del socket
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	}
 
	public void test() {
		// Enviar un entero arbitrario y comprobar que el servidor lo devuelve igual
		try {
			int testValue = 42;
			dos.writeInt(testValue);
			dos.flush();
			int response = dis.readInt();
			if (response == testValue) {
				System.out.println("[testModeTCP] NFConnector.test: received expected value " + response + " - OK");
			} else {
				System.err.println("[testModeTCP] NFConnector.test: expected " + testValue + " but got " + response);
			}
		} catch (IOException e) {
			System.err.println("[testModeTCP] NFConnector.test error: " + e.getMessage());
		}
	}
 
	/** Obtiene la lista de ficheros disponibles en el servidor. */
	public FileInfo[] getFileList() throws IOException {
		PeerMessage req = new PeerMessage(PeerMessageOps.OPCODE_GET_FILE_LIST);
		req.writeMessageToOutputStream(dos);
		dos.flush();
		PeerMessage resp = PeerMessage.readMessageFromInputStream(dis);
		if (resp.getOpcode() == PeerMessageOps.OPCODE_FILE_LIST) {
			return resp.getFileList();
		}
		return null;
	}
 
	/** Comprueba si el servidor tiene el fichero identificado por la subcadena de hash. */
	public boolean hasFile(String hashSubstring) throws IOException {
		PeerMessage req = new PeerMessage(PeerMessageOps.OPCODE_HAS_FILE, hashSubstring);
		req.writeMessageToOutputStream(dos);
		dos.flush();
		PeerMessage resp = PeerMessage.readMessageFromInputStream(dis);
		return resp.getOpcode() == PeerMessageOps.OPCODE_HAS_FILE_YES;
	}
 
	/** Obtiene el nombre remoto del fichero identificado por la subcadena de hash. */
	public String getRemoteFileName(String hashSubstring) throws IOException {
		PeerMessage req = new PeerMessage(PeerMessageOps.OPCODE_GET_FILE_INFO, hashSubstring);
		req.writeMessageToOutputStream(dos);
		dos.flush();
		PeerMessage resp = PeerMessage.readMessageFromInputStream(dis);
		if (resp.getOpcode() == PeerMessageOps.OPCODE_FILE_INFO) {
			return resp.getFilename();
		}
		return null;
	}
 
	/** Obtiene el tamaño remoto del fichero identificado por la subcadena de hash. */
	public long getRemoteFileSize(String hashSubstring) throws IOException {
		PeerMessage req = new PeerMessage(PeerMessageOps.OPCODE_GET_FILE_INFO, hashSubstring);
		req.writeMessageToOutputStream(dos);
		dos.flush();
		PeerMessage resp = PeerMessage.readMessageFromInputStream(dis);
		if (resp.getOpcode() == PeerMessageOps.OPCODE_FILE_INFO) {
			return resp.getFilesize();
		}
		return -1;
	}
 
	/**
	 * Descarga un fragmento (chunk) del fichero identificado por la subcadena de
	 * hash, a partir del desplazamiento indicado y con la longitud solicitada.
	 */
	public byte[] downloadChunk(String hashSubstring, long offset, int length) throws IOException {
		PeerMessage req = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_CHUNK,
				hashSubstring, offset, length);
		req.writeMessageToOutputStream(dos);
		dos.flush();
		PeerMessage resp = PeerMessage.readMessageFromInputStream(dis);
		if (resp.getOpcode() == PeerMessageOps.OPCODE_CHUNK_DATA) {
			return resp.getChunkData();
		}
		return null;
	}
 
	/** Cierra la conexión TCP con el servidor. */
	public void disconnect() throws IOException {
		socket.close();
	}
 
 
 
 
 
	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}
 
}

