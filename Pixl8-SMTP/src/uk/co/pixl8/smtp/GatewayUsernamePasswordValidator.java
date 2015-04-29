package uk.co.pixl8.smtp;

import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public class GatewayUsernamePasswordValidator implements UsernamePasswordValidator {

	private SMTPGateway gateway;
	private CFMLEngine railo;
	
	public GatewayUsernamePasswordValidator( SMTPGateway gateway ) {
		this.gateway = gateway;
		this.railo   = CFMLEngineFactory.getInstance();
	}

	@Override
	public void login( String username, String password ) throws LoginFailedException {
		Struct authenticationResponse = gateway.invokeListenerAuthenticate( username, password );
		
		try {
			Boolean authenticated = railo.getCastUtil().toBoolean( authenticationResponse.get( SMTPGateway.LISTENER_AUTHENTICATE_KEY_RETURN_AUTHENTICATED ) ); 
					
			if ( !authenticated ) {
				String rejectMessage = railo.getCastUtil().toString( authenticationResponse.get( SMTPGateway.LISTENER_AUTHENTICATE_KEY_RETURN_MESSAGE ) );
				
				throw new LoginFailedException( rejectMessage ); 
			}
		} catch ( PageException e ) {
			throw new LoginFailedException( "An error occurred during authentication and your mail message could not be processed" );
		}
	}

}
