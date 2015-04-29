package uk.co.pixl8.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;


public class GatewayMailMessageHandler implements MessageHandler {
	
	private Object identity;
	private String remoteAddress;
	private String from;
	
	private boolean isAccepted;
	
	private SMTPGateway gateway;
	private long maxMessageLength;
	
	private static AtomicInteger countTotal = new AtomicInteger();
	private static AtomicInteger countDelivered = new AtomicInteger();
	
	private int statusCode = 0;
	private String rejectMessage;
	
	private final String uuid;
	private final Struct allRecipients;
	
	/**
	 * initializes a Handler with max data length limit
	 * 
	 * @param context
	 * @param gateway
	 * @param maxDataLength
	 */
	public GatewayMailMessageHandler( MessageContext context, SMTPGateway gateway, long maxDataLength ) {
		
		this.gateway = gateway;
		this.maxMessageLength = maxDataLength;
		
		if ( context.getRemoteAddress() instanceof InetSocketAddress ) {
			
			remoteAddress = ( (InetSocketAddress) context.getRemoteAddress() ).getAddress().toString();
			
			if ( remoteAddress.startsWith( "/" ) ) {
				remoteAddress = remoteAddress.substring( 1 );
			}
		} else {
			
			remoteAddress = context.getRemoteAddress().toString();
		}
		
		if ( context.getAuthenticationHandler() != null ) {
			this.identity = context.getAuthenticationHandler().getIdentity();
		}
		
		uuid = UUID.randomUUID().toString();
		allRecipients = SMTPGateway.createStruct();
		
		countTotal.incrementAndGet();
	}
	
	public GatewayMailMessageHandler( MessageContext context, SMTPGateway gateway ) {
		this( context, gateway, 0 );
	}
	
	
	public static int getTotalCount() {
		return countTotal.get();
	}
	
	
	public static int getTotalDelivered() {
		return countDelivered.get();
	}
	

	@Override
	public void from( String from ) {
		this.from = from;
	}

	
	/**
	 * invokes GatewayListener's Accept() method and sets the isAccepted flag if any recipient is accepted
	 */
	@Override
	public void recipient( String to ) {
		Struct struct = gateway.invokeListenerAccept( this.from, to, remoteAddress, this.uuid, this.identity );			// listener should add data to struct if needed
		
		this.allRecipients.put( to, struct );
				
		if ( (Boolean) struct.get( SMTPGateway.LISTENER_ACCEPT_KEY_RETURN_REJECT, Boolean.FALSE ) ) {		// rejected
			try {
			
				statusCode = CFMLEngineFactory.getInstance().getCastUtil().toIntValue( struct.get( SMTPGateway.LISTENER_ACCEPT_KEY_RETURN_CODE, SMTPGateway.DEFAULT_REJECT_STATUS_CODE ) );			
			} 
			catch (Throwable t) {}
			
			rejectMessage = (String) struct.get( SMTPGateway.LISTENER_ACCEPT_KEY_RETURN_MESSAGE, "Rejected message from " + this.from + " at " + remoteAddress + " to " + to );
		}
		else {
			this.isAccepted = true;
		}
	}


	@Override
	public void data( InputStream in ) throws RejectException, TooMuchDataException, IOException {

		if ( !this.isAccepted ) {
			throw new RejectException( statusCode == 0 ? SMTPGateway.DEFAULT_REJECT_STATUS_CODE : statusCode, rejectMessage == null ? SMTPGateway.DEFAULT_REJECT_MESSAGE : rejectMessage );
		}
		
		if ( this.maxMessageLength > 0 )
			in = new SizeLimitInputStream( in, this.maxMessageLength );
		
		try {
			Session session = Session.getDefaultInstance( new Properties() );
			
			MimeMessage message = new MimeMessage( session, in );
			
			Struct struct = SMTPGateway.createStruct();
			
			Address[] javaRecipients = message.getAllRecipients();
			Address[] javaReplyTo = message.getReplyTo();
			Enumeration headerLines = message.getAllHeaderLines();
			
			String[] recipients = new String[ javaRecipients.length ];
			String[] replyTo    = new String[ javaReplyTo.length ];
			Struct   headers    = gateway.createStruct();

			for( int i=0; i<javaRecipients.length; i++ ){
				recipients[i] = javaRecipients[i].toString();
			}
			for( int i=0; i<javaReplyTo.length; i++ ){
				replyTo[i] = javaReplyTo[i].toString();
			}
			while( headerLines.hasMoreElements() ) {
				String header = (String) headerLines.nextElement();
				String[] headerParts = header.split( ":", 2 );
				
				if ( headerParts.length == 2 ) {
					headers.put( headerParts[0], headerParts[1] );
				}
				
			}			
			
			struct.put( "Raw"          , gateway.getRawMessage(message)                          );
			struct.put( "MessageId"    , message.getMessageID()                                  );
			struct.put( "Size"         , message.getSize()                                       );    		
			struct.put( "ContentType"  , message.getContentType()                                );
			struct.put( "Subject"      , message.getSubject()                                    );
			struct.put( "SentDate"     , message.getSentDate()                                   );
			struct.put( "RecipientList", this.allRecipients                                      );
			struct.put( "ReplyTo"      , SMTPGateway.toRailoArray( replyTo )                     );
			struct.put( "Recipients"   , SMTPGateway.toRailoArray( recipients )                  );
			struct.put( "headers"      , headers                                                 );
			
			try {
				struct.putAll( gateway.parseMimeMessage( message ) );
			} catch( PageException e ) {}
			
			gateway.invokeListenerDeliver( struct, this.uuid, this.identity );
		
		} catch ( MessagingException e ) {
			throw new IOException( e );
		}
		
		countDelivered.incrementAndGet();		
	}
	
	
	@Override
	public void done() {}
}