package uk.co.pixl8.obd.util;

import java.lang.reflect.Method;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.util.Cast;

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
