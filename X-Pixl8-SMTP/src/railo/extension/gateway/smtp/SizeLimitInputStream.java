package railo.extension.gateway.smtp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.TooMuchDataException;

/**
 * a FilterInputStream that keeps track of how many bytes were read, and throws TooMuchDataException if more bytes were read than allowed.
 */
public class SizeLimitInputStream extends FilterInputStream {

    private final long maxLength; 
    private long len = 0;
    
    
    public SizeLimitInputStream( InputStream in, long maxLength ) {
    	
    	super( in );
    	
        this.maxLength = maxLength;
    }
 
 
    /**
     * calls super read method and returns the result via the check() method
     */
    @Override 
    public int read() throws IOException, TooMuchDataException {

    	return check( super.read() );
    }


    /**
     * calls super read method and returns the result via the check() method
     */
    @Override 
    public int read( byte[] barr, int off, int len ) throws IOException, TooMuchDataException {
    	
    	return check( super.read( barr, off, len ) );
    }
    
    
    /**
     * called from the read() methods to ensure that the number of bytes read did not exceed the maxLength limit.
     * 
     * throws TooMuchDataException if maxLength was exceeded.
     * 
     * @param n
     * @return the passed value of n
     * @throws TooMuchDataException
     */
    private int check( int n ) throws TooMuchDataException {
    	
    	if ( n > 0 ) {
    		
    		len += n;
	    	
	        if ( len > maxLength ) 
	        	throw new TooMuchDataException( "Data exceeded the maximum allowed limit of " + maxLength + " bytes" );
    	}
    	
    	return n;
    }
    
    
    public long getLength() {
    	
    	return len;
    }
    
    
    public long getMaxLength() {
    
    	return maxLength;
    }
    
}