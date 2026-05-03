package es.um.redes.nanoFiles.tcp.message;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
 
import es.um.redes.nanoFiles.util.FileInfo;
 
public class PeerMessage {
 
 
 
 
	private byte opcode;
 
	/*
	 * Atributos adicionales para los distintos tipos de mensaje.
	 * No todos los campos son usados por todos los tipos de mensaje.
	 */
	private String   hashSubstring;   // hash o subcadena pedida
	private String   filename;        // nombre del fichero
	private long     filesize = -1;   // tamaño del fichero
	private FileInfo[] fileList;      // lista de ficheros (para FILE_LIST)
	private long     offset   = -1;   // desplazamiento para descarga de chunk
	private int      length   = -1;   // longitud del chunk pedido
	private byte[]   chunkData;       // datos del chunk
 
	/** Constructor para mensajes que solo llevan opcode */
	public PeerMessage(byte op) {
		opcode = op;
	}
 
	/** Constructor para mensajes HAS_FILE / GET_FILE_INFO — solo hash */
	public PeerMessage(byte op, String hashSubstring) {
		opcode = op;
		this.hashSubstring = hashSubstring;
	}
 
	/** Constructor para mensajes FILE_INFO (nombre + tamaño + hash) */
	public PeerMessage(byte op, String filename, long filesize, String hash) {
		opcode = op;
		this.filename      = filename;
		this.filesize      = filesize;
		this.hashSubstring = hash;
	}
 
	/** Constructor para mensajes FILE_LIST */
	public PeerMessage(byte op, FileInfo[] fileList) {
		opcode = op;
		this.fileList = fileList;
	}
 
	/** Constructor para mensajes DOWNLOAD_CHUNK */
	public PeerMessage(byte op, String hashSubstring, long offset, int length) {
		opcode = op;
		this.hashSubstring = hashSubstring;
		this.offset = offset;
		this.length = length;
	}
 
	/** Constructor para mensajes CHUNK_DATA */
	public PeerMessage(byte op, byte[] chunkData) {
		opcode = op;
		this.chunkData = chunkData;
	}
 
	/*
	 * Métodos getter y setter para los atributos del mensaje.
	 * Se comprueba que el campo pedido corresponde al tipo de mensaje.
	 */
	public byte getOpcode() {
		return opcode;
	}
 
	public String getHashSubstring() {
		return hashSubstring;
	}
 
	public String getFilename() {
		return filename;
	}
 
	public long getFilesize() {
		return filesize;
	}
 
	public FileInfo[] getFileList() {
		return fileList;
	}
 
	public long getOffset() {
		return offset;
	}
 
	public int getLength() {
		return length;
	}
 
	public byte[] getChunkData() {
		return chunkData;
	}
 
 
 
 
 
	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE);
		byte opcode = dis.readByte();
		switch (opcode) {
 
		case PeerMessageOps.OPCODE_GET_FILE_LIST: {
			// Solo opcode, sin campos adicionales
			message = new PeerMessage(opcode);
			break;
		}
		case PeerMessageOps.OPCODE_FILE_LIST: {
			// Número de ficheros (int) seguido de nombre(UTF)+tamaño(long)+hash(UTF) por cada uno
			int count = dis.readInt();
			FileInfo[] files = new FileInfo[count];
			for (int i = 0; i < count; i++) {
				String name  = dis.readUTF();
				long   size  = dis.readLong();
				String hash  = dis.readUTF();
				files[i] = new FileInfo(hash, name, size, null);
			}
			message = new PeerMessage(opcode, files);
			break;
		}
		case PeerMessageOps.OPCODE_HAS_FILE:
		case PeerMessageOps.OPCODE_GET_FILE_INFO: {
			// hash substring como UTF
			String hash = dis.readUTF();
			message = new PeerMessage(opcode, hash);
			break;
		}
		case PeerMessageOps.OPCODE_HAS_FILE_YES: {
			// sin campos adicionales
			message = new PeerMessage(opcode);
			break;
		}
		case PeerMessageOps.OPCODE_HAS_FILE_NO: {
			message = new PeerMessage(opcode);
			break;
		}
		case PeerMessageOps.OPCODE_FILE_INFO: {
			// nombre(UTF) + tamaño(long) + hash(UTF)
			String name  = dis.readUTF();
			long   size  = dis.readLong();
			String hash  = dis.readUTF();
			message = new PeerMessage(opcode, name, size, hash);
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_CHUNK: {
			// hash(UTF) + offset(long) + length(int)
			String hash   = dis.readUTF();
			long   offset = dis.readLong();
			int    length = dis.readInt();
			message = new PeerMessage(opcode, hash, offset, length);
			break;
		}
		case PeerMessageOps.OPCODE_CHUNK_DATA: {
			// longitud del chunk (int) + datos
			int len = dis.readInt();
			byte[] data = new byte[len];
			dis.readFully(data);
			message = new PeerMessage(opcode, data);
			break;
		}
		case PeerMessageOps.OPCODE_ERROR: {
			message = new PeerMessage(opcode);
			break;
		}
 
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}
 
	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */
 
		dos.writeByte(opcode);
		switch (opcode) {
 
		case PeerMessageOps.OPCODE_GET_FILE_LIST:
		case PeerMessageOps.OPCODE_HAS_FILE_YES:
		case PeerMessageOps.OPCODE_HAS_FILE_NO:
		case PeerMessageOps.OPCODE_ERROR:
			// Solo opcode, sin campos adicionales
			break;
 
		case PeerMessageOps.OPCODE_HAS_FILE:
		case PeerMessageOps.OPCODE_GET_FILE_INFO:
			// hash substring
			dos.writeUTF(hashSubstring);
			break;
 
		case PeerMessageOps.OPCODE_FILE_INFO:
			// nombre + tamaño + hash
			dos.writeUTF(filename);
			dos.writeLong(filesize);
			dos.writeUTF(hashSubstring);
			break;
 
		case PeerMessageOps.OPCODE_FILE_LIST: {
			// número de ficheros + (nombre+tamaño+hash) por fichero
			int count;
			if(fileList != null) {
				count = fileList.length; 
			} else {
					count = 0;
				}
			
			dos.writeInt(count);
			if (fileList != null) {
				for (FileInfo f : fileList) {
					dos.writeUTF(f.fileName);
					dos.writeLong(f.fileSize);
					dos.writeUTF(f.fileHash);
				}
			}
			break;
		}
 
		case PeerMessageOps.OPCODE_DOWNLOAD_CHUNK:
			// hash + offset + length
			dos.writeUTF(hashSubstring);
			dos.writeLong(offset);
			dos.writeInt(length);
			break;
 
		case PeerMessageOps.OPCODE_CHUNK_DATA: {
			// longitud de datos + datos
			int len = (chunkData != null) ? chunkData.length : 0;
			dos.writeInt(len);
			if (chunkData != null) {
				dos.write(chunkData);
			}
			break;
		}
 
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
 
 
 
 
}