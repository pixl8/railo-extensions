package railo.extension.obd.function;

import railo.extension.obd.type.LinkedHashMapMaxSize;
import railo.extension.obd.type.LinkedHashMapMaxSizeNotCS;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.PageContext;
import railo.runtime.exp.PageException;
import railo.runtime.ext.function.Function;
import railo.runtime.type.Struct;

public class StructListNew implements Function {
	
	private static final long serialVersionUID = 7864882899836593174L;
	public static final int TYPE_LINKED=1;

	public static Struct call(PageContext pc) throws PageException {
		// the caster wraps the map to with MapAsStruct instance
		return CFMLEngineFactory.getInstance().getCastUtil().toStruct(new LinkedHashMapMaxSizeNotCS(-1));
	}
	public static Struct call(PageContext pc, double maxSize) throws PageException {
		return CFMLEngineFactory.getInstance().getCastUtil().toStruct(new LinkedHashMapMaxSizeNotCS((int)maxSize));
	}
		
}
