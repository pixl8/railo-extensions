package uk.co.pixl8.obd.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import javax.servlet.jsp.tagext.BodyContent;

import uk.co.pixl8.obd.tag.TagMapping;
import uk.co.pixl8.obd.util.MD5;
import lucee.commons.io.res.Resource;
import lucee.loader.util.Util;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.util.Cast;

public class Render implements Function {
	
	private static final long serialVersionUID = -9073927788973242109L;
	
	private static int counter=0;

	public static String call(PageContext pc, String cfml) throws PageException{
		
		// prepare data
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		Cast caster = engine.getCastUtil();
		String virtual="/obd_render_function_mapping";
		Resource ramDir = caster.toResource("ram://"+virtual);
		ramDir.mkdirs();
		String name;
		try {
			name = MD5.getDigestAsString(cfml);
		} catch (IOException ioe) {
			throw caster.toPageException(ioe);
		}
		
		
		
		Resource ramFile = ramDir.getRealResource(name);
		
		// create a temporary ram resource
		TagMapping.addMapping(pc, caster, virtual, ramDir, null);
		
		
		// copy to ram resource
		InputStream is=null;
		OutputStream os = null;
		try{
			is=new ByteArrayInputStream(cfml.getBytes(getTemplateCharset(pc.getConfig())));
			os = ramFile.getOutputStream();
			Util.copy(is, os);
		}
		catch(IOException ioe){
			throw caster.toPageException(ioe);
		}
		finally{
			Util.closeEL(is, os);
		}
		
		// execute
		String str=null;
		BodyContent bc=null;
		try{
			bc = pc.pushBody();
			
			pc.doInclude(virtual+"/"+name);
		}
		catch(Throwable t){
			throw caster.toPageException(t);
		}
		finally{
			Util.closeEL(is, os);
			if(bc!=null)str=bc.getString();
			pc.popBody();
		}
		
		// clean
		//ramFile.delete();
		
		return str==null?"":str;
	}

	private static Charset getTemplateCharset(ConfigWeb config) {
		try{
			return config.getTemplateCharset();
		}
		catch(NoSuchMethodError nsme){ // fails with Lucee 4.5 because this method not exist
			try {
				Method m = config.getClass().getMethod("_getTemplateCharset", new Class[]{});
				return (Charset) m.invoke(config, new Object[]{});
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		return null;
	}

	private static synchronized String file() {
		counter++;
		if(counter<0) counter=0;
		return counter+".cfm";
	}
}
