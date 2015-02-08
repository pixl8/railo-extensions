package railo.extension.gateway;

import java.util.Map;
import java.util.TreeMap;

import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;

public class GatewayConfig {

	private Map config;
	
	public GatewayConfig( Map map ) {		
		config = new TreeMap( String.CASE_INSENSITIVE_ORDER );
		
		config.putAll( map );
	}
	
	
	public Map getMap() {
		return config;
	}
	
	
	public String getString( String key, String def ) {
		Object value = config.get( key );
		
		if ( !( value instanceof String ) )
			return def;
		
		return (String) value;
	}
	
	
	public int getInt( String key, int def ) {
		Object value = config.get( key );
		
		if ( value != null ) {
			CFMLEngine railo = CFMLEngineFactory.getInstance(); 
			
			try {
				
				if ( railo.getDecisionUtil().isNumeric( value ) )
					return railo.getCastUtil().toIntValue( value );
			}
			catch (Throwable t) {}
		}
		
		return def;
	}
	
	
	public long getLong( String key, long def ) {
		Object value = config.get( key );		

		if ( value != null ) {
			CFMLEngine railo = CFMLEngineFactory.getInstance();
			
			try {
				
				if ( railo.getDecisionUtil().isNumeric( value ) )
					return railo.getCastUtil().toLongValue( value );
			}
			catch (Throwable t) {}
		}
		
		return ((Number) value).longValue();
	}
	
}
