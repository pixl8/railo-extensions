package railo.extension.gateway.smtp;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

public class GatewayMessageHandlerFactory implements MessageHandlerFactory {

	private SMTPGateway gateway;
	private long maxMessageLength;
	
	
	public GatewayMessageHandlerFactory( SMTPGateway gateway, long maxMessageLength ) {
		this.gateway = gateway;
		
		this.maxMessageLength = maxMessageLength;
	}
	
	
	public GatewayMessageHandlerFactory( SMTPGateway gateway ) {
		this( gateway, 0 );
	}
	
	
	@Override
	public MessageHandler create( MessageContext context ) {
        return new GatewayMailMessageHandler( context, gateway, maxMessageLength );
    }

}
