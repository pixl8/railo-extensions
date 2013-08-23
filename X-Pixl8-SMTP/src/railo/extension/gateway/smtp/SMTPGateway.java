package railo.extension.gateway.smtp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

import railo.extension.gateway.GatewayConfig;
import railo.extension.gateway.GatewaySupport;
import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.exp.PageException;
import railo.runtime.gateway.GatewayEngine;
import railo.runtime.type.Array;
import railo.runtime.type.Struct;


public class SMTPGateway extends GatewaySupport {

	public static String CONFIG_SMTP_HOSTNAME = "hostname";
	public static String CONFIG_SMTP_PORT = "port";
	public static String CONFIG_MAX_MESSAGE_LENGTH = "maxMessageLength";
	
	public static String LISTENER_METHOD_ACCEPT = "accept";
	public static String LISTENER_METHOD_DELIVER = "deliver";
	
	public static String LISTENER_ACCEPT_KEY_FROM = "from";
	public static String LISTENER_ACCEPT_KEY_TO = "to";
	public static String LISTENER_ACCEPT_KEY_IP = "ipAddress";

	public static String LISTENER_ACCEPT_KEY_RETURN_REJECT = "reject";
	public static String LISTENER_ACCEPT_KEY_RETURN_CODE = "code";
	public static String LISTENER_ACCEPT_KEY_RETURN_MESSAGE = "message";
	
	public static final int DEFAULT_REJECT_STATUS_CODE = 530;
	public static final String DEFAULT_REJECT_MESSAGE = "System is not configured to accept this message";
	
	private CFMLEngine railo;
	private GatewayEngine engine;
	
	private SMTPServer smtpServer;
	private GatewayConfig config;
		
	
	@Override
	public void init(GatewayEngine engine, String id, String cfcPath, Map config) throws IOException {
		
		info( this.getClass().getName() + ".init(): " + id );
		
		super.init(engine, id, cfcPath, config);
		this.engine = engine;
		
		this.config = new GatewayConfig( config );
		
		this.railo = CFMLEngineFactory.getInstance();
		
		long maxMessageLength = this.config.getLong( CONFIG_MAX_MESSAGE_LENGTH, 0 );
		
		info( this.getClass().getName() + ".maxMessageLength: " + maxMessageLength );
		
		MessageHandlerFactory handlerFactory = new GatewayMessageHandlerFactory( this, maxMessageLength );
		
		smtpServer = new SMTPServer( handlerFactory );
				
		smtpServer.setPort( this.config.getInt( CONFIG_SMTP_PORT, 25 ) );
		
		String host = this.config.getString( CONFIG_SMTP_HOSTNAME, null );
		
		if ( host != null )
			smtpServer.setHostName( host );
	}
	
	
	@Override
	public String sendMessage(Map data) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void _doStart() throws PageException {

		info( this.getClass().getName() + ": starting smtp server" );
		
		smtpServer.start();
		
		info( this.getClass().getName() + ": started smtp server" );
	}

	@Override
	protected void _doStop() throws PageException {
		
		info( this.getClass().getName() + ": stopping smtp server" );
		
		smtpServer.stop();
		
		info( this.getClass().getName() + ": stopped smtp server" );
	}
	
	
	public Struct getStats() {
		
		Struct result = createStruct();
		
		result.put( "IsRunning", smtpServer.isRunning() );
		
		result.put( "Connections", GatewayMailMessageHandler.getTotalCount() );
		result.put( "Delivered", GatewayMailMessageHandler.getTotalDelivered() );
		
		return result;
	}
	
	
	/**
	 * if rejected, the listener should populate data with the optional (reasonable defaults exist) keys: 
	 * LISTENER_ACCEPT_KEY_RETURN_REJECT (boolean, default false)
	 * LISTENER_ACCEPT_KEY_RETURN_CODE (string with 3 digit numeric value, default 250)
	 * LISTENER_ACCEPT_KEY_RETURN_MESSAGE (string)
	 * 
	 * @param from
	 * @param to
	 * @param ipAddress
	 * @return
	 */
	public Struct invokeListenerAccept( String from, String to, String ipAddress ) {
				
		String failedInvokeMsg = "Gateway Invocation Failed: " + LISTENER_METHOD_ACCEPT;
		
		Struct args = railo.getCreationUtil().createStruct();
		
		args.put( LISTENER_ACCEPT_KEY_FROM, from );
		args.put( LISTENER_ACCEPT_KEY_TO, to );
		args.put( LISTENER_ACCEPT_KEY_IP, ipAddress );		

		Struct result = createStruct();
		
		result.put( LISTENER_ACCEPT_KEY_RETURN_REJECT, Boolean.FALSE );
		result.put( LISTENER_ACCEPT_KEY_RETURN_CODE, 0 );
		result.put( LISTENER_ACCEPT_KEY_RETURN_MESSAGE, "" );
		
		args.put( "RESULT", result );
		
		boolean isInvokeOk = engine.invokeListener( this, LISTENER_METHOD_ACCEPT, args );
				
		if ( isInvokeOk ) {
							
			try {
				
				Boolean isRejected = railo.getCastUtil().toBoolean( result.get( LISTENER_ACCEPT_KEY_RETURN_REJECT ) );

				if ( isRejected ) {
					
					if ( result.get( LISTENER_ACCEPT_KEY_RETURN_MESSAGE ).toString().isEmpty() )
						result.put( LISTENER_ACCEPT_KEY_RETURN_MESSAGE, DEFAULT_REJECT_MESSAGE );
					
					if ( railo.getDecisionUtil().isNumeric( result.get( LISTENER_ACCEPT_KEY_RETURN_CODE ) ) ) {
						
						result.put( LISTENER_ACCEPT_KEY_RETURN_CODE, railo.getCastUtil().toInteger( result.get( LISTENER_ACCEPT_KEY_RETURN_CODE ) ) );
					}
					else {
						
						result.put( LISTENER_ACCEPT_KEY_RETURN_CODE, DEFAULT_REJECT_STATUS_CODE );
					}
				}
			} 
			catch ( PageException e ) {
			
				isInvokeOk = false;
				failedInvokeMsg = e.getMessage();
			}			
		} 
		
		if ( !isInvokeOk ) {	// invocation failed -- log and reject 
			
			result.put( LISTENER_ACCEPT_KEY_RETURN_REJECT, Boolean.TRUE );
			result.put( LISTENER_ACCEPT_KEY_RETURN_CODE, 451 );
			result.put( LISTENER_ACCEPT_KEY_RETURN_MESSAGE, failedInvokeMsg );
			
			error( "failed to invoke listener method " + LISTENER_METHOD_ACCEPT + "( '" + from + "', '" + to + "', '" + ipAddress + "' )" );
		}
				
		return result;
	}
	
	
	public void invokeListenerDeliver( Struct data ) {
		
		Struct args = createStruct();
		
		args.put( "DATA", data );
		
		if ( engine.invokeListener( this, LISTENER_METHOD_DELIVER, args ) ) {
			
			
		} else {
			
			error( "failed to invoke listener method " + LISTENER_METHOD_DELIVER );
		}
	}
	
	
	/** returns a Railo Array object */
	public static Array createArray() {
		
		return CFMLEngineFactory.getInstance().getCreationUtil().createArray();
	}
	
	/** returns a Railo Struct object */
	public static Struct createStruct() {
		
		return CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
	}
	
	
	public static Array toRailoArray( Enumeration e ) {
		
		Array result = createArray();
		
		if ( e != null ) {			
			
			try {			
				
				while ( e.hasMoreElements() )
					result.append( e.nextElement() );
				
			} 
			catch ( PageException ex ) {}
		}
		
		return result;
	}

	
	public static Array toRailoArray( Object[] arr ) {
		
		Array result = createArray();
		
		if ( arr != null ) {
		
			try {			
				
				for ( int i=0; i<arr.length; i++ )
					result.append( arr[ i ] );
				
			} 
			catch ( PageException ex ) {}
				
		}
		
		return result;
	}
	

	/**
	 * returns the raw message from MimeMessage
	*/
    public static String getRawMessage( MimeMessage message ) {
    	
    	if ( message == null )
    		return "";
    	
    	StringBuilder sb = new StringBuilder( 1024 );
    	
        try {
        	
        	BufferedReader reader = new BufferedReader(new InputStreamReader( message.getRawInputStream() ));
			
			String line;        	
            while ((line = reader.readLine()) != null) {
            	
                sb.append( line ).append( '\n' );
            }            
		} catch (Throwable t) {

			sb.append( t.toString() );
        }
        
        return sb.toString();
    }
    
    
    /**
     * returns a Struct with the keys: BodyText (String), BodyHtml (String), Attachments (Array)
     * 
     * @param message
     * @return
     * @throws PageException
     */
    public static Struct parseMimeMessage( MimeMessage message ) throws PageException {
    	
    	Struct result = createStruct();
    	
    	String text = "", html = "";
    	Array arr = createArray();
    	
    	List<Map<String, Object>> al = getBodyParts( message );
    	
    	for ( Map<String, Object> m : al ) {
    		
    		String ctype = (String)m.get( "ContentType" );
    		
    		if ( ctype.startsWith( "text/" ) && m.get( "Filename" ).toString().isEmpty() && m.get( "Disposition" ).toString().isEmpty() ) {
    			
    			if ( ctype.startsWith( "text/plain" ) )
    				text = (String)m.get( "Content" );
    			else if ( ctype.startsWith( "text/html" ) )
    				html = (String)m.get( "Content" );
    		}
    		else {
    			
    			Struct s = createStruct();    			
    			s.putAll( m );    			
    			arr.append( s );
    		}
    	}
    	
    	result.put( "BodyText", text );
    	result.put( "BodyHtml", html );
    	result.put( "Attachments", arr );
    	
    	return result;
    }
    
    
    /**
     * returns a java.util.List of Map objects with the different Body Parts
     *  
     * @param message
     * @return
     */
    public static List<Map<String, Object>> getBodyParts( MimeMessage message ) {
		
		List<Map<String, Object>> result = new ArrayList();
		
		try {
		
			addParts( message, result );			
		}
		catch ( MessagingException e ) { }
		catch ( IOException e ) { }
			
		return result;
	}
    
	
    /**
     * called from getBodyParts() and then recursively to populate the result
     * 
     * @param part
     * @param result
     * @throws MessagingException
     * @throws IOException
     */
	private static void addParts( Part part, List<Map<String, Object>> result ) throws MessagingException, IOException {
		
		Object content = part.getContent();
		
		if ( content instanceof Part ) {
			
			addParts( (Part)content, result );					// recurse single
		} 
		else if ( content instanceof Multipart ) {
			
			Multipart multi = (Multipart)content;
			
			int count = multi.getCount();			
			for (int i=0; i < count; i++) {
				
				addParts( multi.getBodyPart( i ), result );		// recurse multi
			}
		} 
		else {
			
			String contentType = part.getContentType().toLowerCase();
			
			Map<String, Object> map = new TreeMap( String.CASE_INSENSITIVE_ORDER );
			
			int size = part.getSize();
			
			if ( content instanceof InputStream ) {
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				part.getDataHandler().writeTo( baos );
				
				content = baos.toByteArray();
				size = ((byte[])content).length;
			}
			
			map.put( "ContentType", contentType );
			map.put( "Content", content );
			map.put( "Disposition", part.getDisposition() != null ? part.getDisposition() : "" );
			map.put( "Filename", part.getFileName() != null ? part.getFileName() : "" );

			List<String> headers = new ArrayList();
			Enumeration e = part.getAllHeaders();
			while ( e.hasMoreElements() ) {
				
				Header h = (Header)e.nextElement();
				headers.add( h.getName() + ": " + h.getValue() );
			}
			
			map.put( "Headers", headers );
			map.put( "Size", size );
			
			result.add( map );
		}		
	}
	
}