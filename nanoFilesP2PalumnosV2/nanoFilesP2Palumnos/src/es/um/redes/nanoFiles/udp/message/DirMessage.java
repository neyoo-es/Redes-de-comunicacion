package es.um.redes.nanoFiles.udp.message;
 
 
 
 
/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)
 
	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea
 
	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	private static final String FIELDNAME_PROTOCOL_ID = "protocol";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SERVER_PORT = "port";
	private static final String FIELDNAME_FILENAME = "filename";
	private static final String FIELDNAME_FILESIZE = "filesize";
	private static final String FIELDNAME_FILEHASH = "filehash";
	private static final String FIELDNAME_PEER_NICKNAME = "peer_nickname";
	private static final String FIELDNAME_PEER_ADDRESS = "peer_address";
 
 
 
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private int serverPort = -1;
	// Listas de ficheros: nombres, tamaños y hashes en paralelo
	private java.util.List<String> filenames = new java.util.ArrayList<>();
	private java.util.List<Long> filesizes = new java.util.ArrayList<>();
	private java.util.List<String> filehashes = new java.util.ArrayList<>();
	// Lista de peers: nicknames y direcciones en paralelo
	private java.util.List<String> peerNicknames = new java.util.ArrayList<>();
	private java.util.List<String> peerAddresses = new java.util.ArrayList<>();
 
 
 
 
	public DirMessage(String op) {
		operation = op;
	}
 
	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */
	/** Constructor para mensajes de ping (cliente → directorio) */
	public DirMessage(String op, String protocolIdentOrNickname) {
		operation = op;
		if (op.equals(DirMessageOps.OPERATION_PING)) {
			this.protocolId = protocolIdentOrNickname;
		} else {
			this.nickname = protocolIdentOrNickname;
		}
	}
 
	/** Constructor para mensajes de registro de servidor */
	public DirMessage(String op, String nickname, int serverPort) {
		operation = op;
		this.nickname = nickname;
		this.serverPort = serverPort;
	}
 
 
 
 
	public String getOperation() {
		return operation;
	}
 
	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}
 
	public String getProtocolId() {
		return protocolId;
	}
 
	public void setNickname(String nick) {
		this.nickname = nick;
	}
 
	public String getNickname() {
		return nickname;
	}
 
	public void setServerPort(int port) {
		this.serverPort = port;
	}
 
	public int getServerPort() {
		return serverPort;
	}
 
	/** Añade un fichero (nombre, tamaño, hash) a la lista del mensaje */
	public void addFile(String filename, long filesize, String filehash) {
		this.filenames.add(filename);
		this.filesizes.add(filesize);
		this.filehashes.add(filehash);
	}
 
	public java.util.List<String> getFilenames() { return filenames; }
	public java.util.List<Long> getFilesizes() { return filesizes; }
	public java.util.List<String> getFilehashes() { return filehashes; }
 
	/** Añade un peer (nickname, dirección "ip:puerto") a la lista del mensaje */
	public void addPeer(String peerNick, String peerAddr) {
		this.peerNicknames.add(peerNick);
		this.peerAddresses.add(peerAddr);
	}
 
	public java.util.List<String> getPeerNicknames() { return peerNicknames; }
	public java.util.List<String> getPeerAddresses() { return peerAddresses; }
 
 
 
 
	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */
 
		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;
 
		// Variables temporales para acumular campos de ficheros en orden
		String lastFilename = null;
		long lastFilesize = -1;
 
		for (String line : lines) {
			if (line.trim().isEmpty()) continue; // línea vacía = fin de mensaje
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			if (idx < 0) continue;
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();
 
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			/*
			 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
			 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
			 * delimitador DELIMITER, y guardarlo en variables locales.
			 */
			case FIELDNAME_PROTOCOL_ID: {
				if (m != null) m.protocolId = value;
				break;
			}
			case FIELDNAME_NICKNAME: {
				if (m != null) m.nickname = value;
				break;
			}
			case FIELDNAME_SERVER_PORT: {
				if (m != null) {
					try { m.serverPort = Integer.parseInt(value); } catch (NumberFormatException e) {}
				}
				break;
			}
			case FIELDNAME_FILENAME: {
				// Guardamos el nombre del fichero; lo añadimos cuando llegue su filesize
				if (m != null) lastFilename = value;
				break;
			}
			case FIELDNAME_FILESIZE: {
				if (m != null) {
					try {
						lastFilesize = Long.parseLong(value);
					} catch (NumberFormatException e) { lastFilesize = -1; }
				}
				break;
			}
			case FIELDNAME_FILEHASH: {
				// Con hash ya tenemos los tres campos del fichero; añadimos la entrada
				if (m != null && lastFilename != null) {
					m.filenames.add(lastFilename);
					m.filesizes.add(lastFilesize);
					m.filehashes.add(value);
					lastFilename = null;
					lastFilesize = -1;
				}
				break;
			}
			case FIELDNAME_PEER_NICKNAME: {
				if (m != null) m.peerNicknames.add(value);
				break;
			}
			case FIELDNAME_PEER_ADDRESS: {
				if (m != null) m.peerAddresses.add(value);
				break;
			}
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}
 
 
 
 
		return m;
	}
 
	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {
 
		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			// Mensaje de ping del cliente: incluye el protocol ID
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL_ID + DELIMITER + protocolId + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_PING_OK:
		case DirMessageOps.OPERATION_PING_DENIED:
		case DirMessageOps.OPERATION_UNREGISTER_OK:
		case DirMessageOps.OPERATION_UNREGISTER_FAIL:
		case DirMessageOps.OPERATION_GET_FILE_LIST:
		case DirMessageOps.OPERATION_GET_PEER_LIST:
		case DirMessageOps.OPERATION_UNREGISTER_SERVER: {
			// Mensajes sin campos adicionales
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_SERVER: {
			// Registro: nickname + puerto TCP
			if (nickname != null) sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			if (serverPort > 0)   sb.append(FIELDNAME_SERVER_PORT + DELIMITER + serverPort + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_OK: {
			// Respuesta de registro exitoso: puede incluir nickname asignado
			if (nickname != null) sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_FAIL: {
			break;
		}
		case DirMessageOps.OPERATION_FILE_LIST: {
			// Lista de ficheros: cada fichero ocupa tres líneas (filename, filesize, filehash)
			for (int i = 0; i < filenames.size(); i++) {
				sb.append(FIELDNAME_FILENAME + DELIMITER + filenames.get(i) + END_LINE);
				sb.append(FIELDNAME_FILESIZE + DELIMITER + filesizes.get(i) + END_LINE);
				sb.append(FIELDNAME_FILEHASH + DELIMITER + filehashes.get(i) + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_PEER_LIST: {
			// Lista de peers: cada peer ocupa dos líneas (peer_nickname, peer_address)
			for (int i = 0; i < peerNicknames.size(); i++) {
				sb.append(FIELDNAME_PEER_NICKNAME + DELIMITER + peerNicknames.get(i) + END_LINE);
				sb.append(FIELDNAME_PEER_ADDRESS + DELIMITER + peerAddresses.get(i) + END_LINE);
			}
			break;
		}
		default:
			System.err.println("DirMessage.toString: unknown operation " + operation);
			break;
		}
 
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
 

 