package railo.extension.obd.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.jsp.tagext.BodyContent;

import railo.commons.io.res.Resource;
import railo.extension.obd.tag.TagMapping;
import railo.extension.obd.util.MD5;
import railo.loader.util.Util;
import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.PageContext;
import railo.runtime.exp.PageException;
import railo.runtime.ext.function.Function;
import railo.runtime.util.Cast;

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
			is=new ByteArrayInputStream(cfml.getBytes(pc.getConfig().getTemplateCharset()));
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

	private static synchronized String file() {
		counter++;
		if(counter<0) counter=0;
		return counter+".cfm";
	}
}
