package uk.co.pixl8.common.jsonvalidation.function;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Creation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;


public class ValidateJson implements Function {

	private static final long serialVersionUID = -8114568696008052170L;
	
	public static final String KEY_ISVALID = "IsValid";
	public static final String KEY_ERRORS = "Errors";
	
	public static Struct call( PageContext pc, String json, String schema ) throws PageException {
		Creation creation = CFMLEngineFactory.getInstance().getCreationUtil();
				
		Struct result = creation.createStruct();
		Array errors = creation.createArray();
		ProcessingReport report;
		try {
			JsonNode jnDoc = JsonLoader.fromString( json );
			JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
			JsonSchema jsSchema = factory.getJsonSchema( JsonLoader.fromString( schema ) );
			report= jsSchema.validate( jnDoc );
		} 
		catch (Throwable t) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(t);
		}
		result.put( KEY_ERRORS, errors );
		
		if ( report != null && report.isSuccess() ) {
			result.put( KEY_ISVALID, Boolean.TRUE );
		} 
		else {
			result.put( KEY_ISVALID, Boolean.FALSE );
			if ( report != null ) {
				for (ProcessingMessage message : report) {				
					errors.append( message.toString() );
				}
			}
		}
		
		return result;
	}
}