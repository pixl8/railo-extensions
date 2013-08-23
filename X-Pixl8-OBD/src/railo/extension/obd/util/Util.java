package railo.extension.obd.util;

import java.lang.reflect.Method;

import railo.commons.io.res.Resource;
import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.Mapping;
import railo.runtime.PageContext;
import railo.runtime.config.ConfigWeb;
import railo.runtime.exp.PageException;
import railo.runtime.util.Cast;

public class Util {
	
	private static final Class<?>[] ARGS = new Class<?>[]{String.class, String.class};
	private static Method getApplicationMapping;
	
	
	public static Mapping getApplicationMapping(PageContext pc,Cast caster,String logicalPath, Resource physical, Resource archive) throws PageException {
		ConfigWeb cw = pc.getConfig();
		
		// TODO add support for archives, for this we need a method also accept archives
		if(getApplicationMapping==null || getApplicationMapping.getDeclaringClass()!=cw.getClass()) {
			try {
				getApplicationMapping=cw.getClass().getMethod("getApplicationMapping", ARGS);
			} catch (Throwable t) {
				throw caster.toPageException(t);
			}
		}
		
		try {
			return (Mapping) getApplicationMapping.invoke(cw, new Object[]{logicalPath,physical.getAbsolutePath()});
		} catch (Throwable t) {
			throw caster.toPageException(t);
		}
	}
}
