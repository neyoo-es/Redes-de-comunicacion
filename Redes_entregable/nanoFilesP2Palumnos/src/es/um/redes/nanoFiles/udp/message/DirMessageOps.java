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

    // ping_ok / ping_denied
    public static final String OPERATION_WELCOME        = "welcome";      
    public static final String OPERATION_DENIED         = "denied";      

    // dirfiles 
    public static final String OPERATION_DIRFILES       = "dirfiles";     
    public static final String OPERATION_AVAILABLEFILES = "availablefiles"; 

    // dirdl 
    public static final String OPERATION_DIRDL          = "dirdl";
    public static final String OPERATION_DIRDL_RESPONSE = "dirdl_response";

    // serve 
    public static final String OPERATION_SERVE          = "serve";       
    public static final String OPERATION_SERVE_RESPONSE = "serve_response"; 
    public static final String OPERATION_SERVE_FAIL     = "serve_fail";   

    // peers 
    public static final String OPERATION_PEERS          = "peers";        
    public static final String OPERATION_PEERLIST       = "peerlist";    

    // quit 
    public static final String OPERATION_QUIT           = "quit";         
    public static final String OPERATION_QUIT_RESPONSE  = "quit_response"; 
    public static final String OPERATION_QUIT_FAIL      = "quit_fail";    
}

 
 