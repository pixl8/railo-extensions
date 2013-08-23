package railo.extension.gateway.smtp.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.io.DeferredFileOutputStream;


public class BasicMessageListenerAdapter implements MessageHandlerFactory {

	
	private static int DEFAULT_DATA_DEFERRED_SIZE = 1024*1024*5;

	private Collection<IBasicMessageListener> listeners;
	private int dataDeferredSize;


	public BasicMessageListenerAdapter(IBasicMessageListener listener) {
		
		this(Collections.singleton(listener), DEFAULT_DATA_DEFERRED_SIZE);
	}

	public BasicMessageListenerAdapter(Collection<IBasicMessageListener> listeners) {
		
		this(listeners, DEFAULT_DATA_DEFERRED_SIZE);
	}

	/**
	 * Initializes this factory with the listeners.
	 * @param dataDeferredSize The server will buffer
	 *        incoming messages to disk when they hit this limit in the
	 *        DATA received.
	 */
	public BasicMessageListenerAdapter(Collection<IBasicMessageListener> listeners, int dataDeferredSize) {
		
		this.listeners = listeners;
		this.dataDeferredSize = dataDeferredSize;
	}

	/* (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandlerFactory#create(org.subethamail.smtp.MessageContext)
	 */
	public MessageHandler create(MessageContext ctx) {
		
		return new Handler(ctx);
	}

	/**
	 * Needed by this class to track which listeners need delivery.
	 */
	static class Delivery {
		
		IBasicMessageListener listener;
		public IBasicMessageListener getListener() { return this.listener; }

		String recipient;
		public String getRecipient() { return this.recipient; }

		public Delivery(IBasicMessageListener listener, String recipient) {
			
			this.listener = listener;
			this.recipient = recipient;
		}
	}

	/**
	 * Class which implements the actual handler interface.
	 */
	class Handler implements MessageHandler {
		
		MessageContext context;
		String from;
		List<Delivery> deliveries = new ArrayList<Delivery>();

		/** */
		public Handler(MessageContext ctx) {
			this.context = ctx;
		}

		/** */
		public void from(String from) throws RejectException {
			
			this.from = from;
		}

		/** */
		public void recipient(String recipient) throws RejectException {
			
			boolean addedListener = false;

			String remoteAddress;
			
			if ( context.getRemoteAddress() instanceof InetSocketAddress ) {
				
				remoteAddress = ( (InetSocketAddress) context.getRemoteAddress() ).getAddress().toString();
				
				if ( remoteAddress.startsWith( "/" ) )
					remoteAddress = remoteAddress.substring( 1 );
			} else {
				
				remoteAddress = context.getRemoteAddress().toString();
			}
			
			for ( IBasicMessageListener listener: BasicMessageListenerAdapter.this.listeners ) {
				
				if ( listener.accept( this.from, recipient, remoteAddress ) ) {
					
					this.deliveries.add( new Delivery( listener, recipient ) );
					addedListener = true;
				}
			}
			
//			if (!addedListener) throw new RejectException(553, "<" + recipient + "> address unknown.");
			
			System.out.println( "added " + this.deliveries.size() + " recipients" );
		}

		/** */
		public void data(InputStream data) throws TooMuchDataException, IOException {
			
			if ( this.deliveries.isEmpty() ) {
				
				throw new RejectException(553, "This system is not configured to accept this message");
			}
			
			System.out.println( "Will deliver " + this.deliveries.size() + " messages..." );
			
			if ( true || this.deliveries.size() == 1 ) {
				
				Delivery delivery = this.deliveries.get( 0 );
				delivery.getListener().deliver( this.from, delivery.getRecipient(), data ) ;
			}	/*/ we only need one copy for cfsmtp
			else {
				
				DeferredFileOutputStream dfos = new DeferredFileOutputStream( BasicMessageListenerAdapter.this.dataDeferredSize );

				try {
					
					int value;
					
					while ((value = data.read()) >= 0) {
						dfos.write(value);
					}

					for (Delivery delivery: this.deliveries) {
						delivery.getListener().deliver(this.from, delivery.getRecipient(), dfos.getInputStream());
					}
				}
				finally {
					dfos.close();
				}
			}	//*/
		}

		/** */
		public void done() { }
	
	}
}
