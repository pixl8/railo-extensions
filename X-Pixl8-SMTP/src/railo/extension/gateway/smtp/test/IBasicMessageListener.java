package railo.extension.gateway.smtp.test;

import org.subethamail.smtp.helper.SimpleMessageListener;

/**
 * 
 * deprecated - probably not needed, delete before deploy
 */
//@Deprecated
public interface IBasicMessageListener extends SimpleMessageListener {

	public boolean accept( String from, String recipient, String ipAddress );
	
//	public boolean acceptConnection( String ipAddress );
}
