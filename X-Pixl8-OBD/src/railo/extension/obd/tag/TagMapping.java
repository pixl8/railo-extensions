package railo.extension.obd.tag;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import railo.commons.io.res.Resource;
import railo.loader.engine.CFMLEngine;
import railo.loader.engine.CFMLEngineFactory;
import railo.loader.util.Util;
import railo.runtime.CFMLFactory;
import railo.runtime.Mapping;
import railo.runtime.PageContext;
import railo.runtime.config.ConfigServer;
import railo.runtime.config.ConfigWeb;
import railo.runtime.exp.PageException;
import railo.runtime.ext.tag.TagSupport;
import railo.runtime.listener.ApplicationContext;
import railo.runtime.type.Struct;
import railo.runtime.util.Cast;
import railo.runtime.util.Creation;
import railo.runtime.util.Excepton;


public final class TagMapping extends TagSupport {
	
	private final Excepton exp;
	private final Cast caster;
	private final Creation creator;
	
	private String logicalPath;
	private String relativePath;
	private String directoryPath;
	private String archive;

		
	public TagMapping(){
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		exp=engine.getExceptionUtil();
		caster=engine.getCastUtil();
		creator=engine.getCreationUtil();
	}
	
	@Override
	public void release()	{
		super.release();
		logicalPath=null;
		relativePath=null;
		directoryPath=null;
		archive=null;
	}
	
	public void setLogicalpath(String logicalPath) {
		if(Util.isEmpty(logicalPath,true)) return;
		this.logicalPath = logicalPath.trim();
	}

	public void setRelativepath(String relativePath) {
		if(Util.isEmpty(relativePath,true)) return;
		this.relativePath = relativePath.trim();
	}

	public void setDirectorypath(String directoryPath) {
		if(Util.isEmpty(directoryPath,true)) return;
		this.directoryPath = directoryPath.trim();
	}

	public void setArchive(String archive) {
		if(Util.isEmpty(archive,true)) return;
		this.archive = archive.trim();
	}

	@Override
	public int doStartTag() throws PageException	{
		boolean hasDirectoryPath=!Util.isEmpty(directoryPath);
		boolean hasRelativePath=!Util.isEmpty(relativePath);
		boolean hasArchive=!Util.isEmpty(archive);
		
		// check attributes
		int notEmptyCount=(hasDirectoryPath?1:0)+(hasRelativePath?1:0)+(hasArchive?1:0);
		if(notEmptyCount==0)
			throw exp.createApplicationException("You have to define at least one of the following attributes: directoryPath, relativePath or archive");
		if(notEmptyCount>1)
			throw exp.createApplicationException("You can only define one of the following attributes: directoryPath, relativePath or archive");
		if(hasArchive)
			throw exp.createApplicationException("attribute archive is not supported yet!");
		
		// get physical or archive Resource
		Resource physical=null;
		Resource _archive=null;
		if(hasRelativePath) {
			Resource rel = pageContext.getCurrentPageSource().getResourceTranslated(pageContext).getParentResource();
			rel = rel.getRealResource(relativePath);
			Resource abs = relativePath.equals("/")?null:creator.createResource(relativePath, false);
			physical=(abs!=null && abs.isDirectory())?abs:rel;
			
		}
		else if(hasDirectoryPath) {
			physical = creator.createResource(directoryPath, false);
			
		}
		else /* archive */ {
			_archive = creator.createResource(archive, true);
		}
		
		
		
		addMapping(pageContext, caster, logicalPath, physical, _archive);
		
		return SKIP_BODY;
	}
	
	public static void addMapping(PageContext pc,Cast caster, String logicalPath, Resource physical, Resource archive) throws PageException	{
		physical=getCanonicalResourceEL(physical);
		archive=getCanonicalResourceEL(archive);
		
		// get virtual
		logicalPath=translateVirtual(logicalPath);
		
		// get mappings
		Mapping mapping = railo.extension.obd.util.Util.getApplicationMapping(pc,caster,logicalPath,physical,archive);
		ApplicationContext ac = pc.getApplicationContext();
		
		// merge mappings
		Mapping[] mappings = ac.getMappings();
		if(mappings!=null && mappings.length>0) {
			Mapping[] newMappings = new Mapping[mappings.length+1];
			for(int i=0;i<mappings.length;i++){
				newMappings[i]=mappings[i];
			}
			newMappings[mappings.length]=mapping;
			ac.setMappings(newMappings);
		}
		else 
			ac.setMappings(new Mapping[]{mapping});
	}

	private static Resource getCanonicalResourceEL(Resource res) {
		if(res==null) return res;
		try {
			return res.getCanonicalResource();
		} catch (IOException e) {
			return res.getAbsoluteResource();
		}
	}

	private static String translateVirtual(String virtual) {
		virtual=virtual.replace('\\', '/').trim();
		if(!virtual.startsWith("/"))virtual="/".concat(virtual);
		return virtual;
	}

}
