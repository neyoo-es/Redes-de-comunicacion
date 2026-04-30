package es.um.redes.nanoFiles.udp.message;
 
public class DirMessageOps {
 
	/*
	 * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	// TODO: definir las operaciones del protocolo de directorio
	public static final String OPERATION_PING_OK = "ping_ok";
	public static final String OPERATION_PING_DENIED = "ping_denied";
	public static final String OPERATION_GET_FILE_LIST = "get_file_list";
	public static final String OPERATION_FILE_LIST = "file_list";
	public static final String OPERATION_REGISTER_SERVER = "register_server";
	public static final String OPERATION_REGISTER_OK = "register_ok";
	public static final String OPERATION_REGISTER_FAIL = "register_fail";
	public static final String OPERATION_GET_PEER_LIST = "get_peer_list";
	public static final String OPERATION_PEER_LIST = "peer_list";
	public static final String OPERATION_UNREGISTER_SERVER = "unregister_server";
	public static final String OPERATION_UNREGISTER_OK = "unregister_ok";
	public static final String OPERATION_UNREGISTER_FAIL = "unregister_fail";
 
 
 
 
}
 