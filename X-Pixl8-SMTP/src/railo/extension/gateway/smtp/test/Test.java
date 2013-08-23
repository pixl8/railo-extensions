package railo.extension.gateway.smtp.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;

import railo.extension.gateway.smtp.SMTPGateway;
import railo.runtime.exp.PageException;


public class Test {

	/*/
	public static List getBodyParts( MimeMessage message ) {
		
		List result = new ArrayList();
		
		try {
		
			getParts( message, result );
			
		}
		catch ( MessagingException e ) { }
		catch ( IOException e ) { }
			
		return result;
	}
	
	private static void getParts( Part part, List result ) throws MessagingException, IOException {
		
		String contentType = part.getContentType().toLowerCase();
		
		if ( contentType.startsWith( "multipart/" ) ) {
			
			Multipart multi = (Multipart)part.getContent();
			
			int count = multi.getCount();
			
			for (int i=0; i<count; i++) {
				
				getParts( multi.getBodyPart( i ), result );
			}		
		}
		else {
			
			Map map = new TreeMap( String.CASE_INSENSITIVE_ORDER );
			
			map.put( "ContentType", contentType );
			map.put( "Content", part.getContent() );
			map.put( "Disposition", part.getDisposition() != null ? part.getDisposition() : "" );
			map.put( "Filename", part.getFileName() != null ? part.getFileName() : "" );
			map.put( "Size", part.getSize() );
			
			result.add( map );
		}
	}	//*/
	
	
	public static void main(String[] args) {
		
		IBasicMessageListener listener = new IBasicMessageListener() {
			
			@Override
			public void deliver(String from, String recipient, InputStream in)
					throws TooMuchDataException, IOException {
				// TODO Auto-generated method stub
				
				System.out.println( String.format( "\tdeliver from %s to %s", from, recipient ) );
				
				try {
					
					MimeMessage message = new MimeMessage( Session.getDefaultInstance( new Properties() ), in );
					
					String contentType = message.getContentType();
					Object content = message.getContent();
					
					List parts = SMTPGateway.getBodyParts( message );
					
					System.out.println( contentType );
				}
				catch ( MessagingException e ) {
					
					
				}
			}
			
			@Override
			public boolean accept(String from, String recipient) {
				// TODO Auto-generated method stub
				
				if ( recipient.contains( "2@" ) ) {
				
					System.out.println( String.format( "rejected from %s to %s", from, recipient ) );
					return false;
				}
				
				System.out.println( String.format( "accepted from %s to %s", from, recipient ) );
				
				return true;
			}
			
			@Override
			public boolean accept(String from, String recipient, String ipAddress) {
				// TODO Auto-generated method stub
				
				if ( ipAddress.isEmpty() ) {
					
					return false;
				}

				return this.accept(from, recipient);
			}
		};
		
		SMTPServer smtpServer = new SMTPServer( new BasicMessageListenerAdapter( listener ) );
		
		smtpServer.start();
		
		try {
			
			while( true ) {
				
			
				Thread.sleep( 1000 );	
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
