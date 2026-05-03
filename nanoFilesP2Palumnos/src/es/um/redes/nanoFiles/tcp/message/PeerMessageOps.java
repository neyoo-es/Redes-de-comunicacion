package es.um.redes.nanoFiles.tcp.message;
 
import java.util.Map;
import java.util.TreeMap;
 
public class PeerMessageOps {
 
	public static final byte OPCODE_INVALID_CODE = 0;
 
	/*
	 * Opcodes del protocolo de comunicación con un par servidor de ficheros.
	 * El cliente envía peticiones y el servidor responde con los correspondientes
	 * mensajes de respuesta.
	 */
	public static final byte OPCODE_GET_FILE_LIST    = 1;  // cliente pide lista de ficheros
	public static final byte OPCODE_FILE_LIST        = 2;  // servidor responde con lista
	public static final byte OPCODE_HAS_FILE         = 3;  // cliente pregunta si tiene un hash
	public static final byte OPCODE_HAS_FILE_YES     = 4;  // servidor: sí lo tiene
	public static final byte OPCODE_HAS_FILE_NO      = 5;  // servidor: no lo tiene
	public static final byte OPCODE_GET_FILE_INFO    = 6;  // cliente pide nombre+tamaño por hash
	public static final byte OPCODE_FILE_INFO        = 7;  // servidor responde con nombre+tamaño
	public static final byte OPCODE_DOWNLOAD_CHUNK   = 8;  // cliente pide un fragmento
	public static final byte OPCODE_CHUNK_DATA       = 9;  // servidor responde con los bytes
	public static final byte OPCODE_ERROR            = 10; // error genérico
 
	/*
	 * Tabla de opcodes y sus representaciones textuales (mismo orden).
	 */
	private static final Byte[] _valid_opcodes = { OPCODE_INVALID_CODE,
		OPCODE_GET_FILE_LIST,
		OPCODE_FILE_LIST,
		OPCODE_HAS_FILE,
		OPCODE_HAS_FILE_YES,
		OPCODE_HAS_FILE_NO,
		OPCODE_GET_FILE_INFO,
		OPCODE_FILE_INFO,
		OPCODE_DOWNLOAD_CHUNK,
		OPCODE_CHUNK_DATA,
		OPCODE_ERROR,
	};
	private static final String[] _valid_operations_str = { "INVALID_OPCODE",
		"GET_FILE_LIST",
		"FILE_LIST",
		"HAS_FILE",
		"HAS_FILE_YES",
		"HAS_FILE_NO",
		"GET_FILE_INFO",
		"FILE_INFO",
		"DOWNLOAD_CHUNK",
		"CHUNK_DATA",
		"ERROR",
	};
 
	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;
 
	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
 
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}
 
	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
