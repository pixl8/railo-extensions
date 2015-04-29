package uk.co.pixl8.obd.function;

import uk.co.pixl8.obd.type.LinkedHashMapMaxSize;
import uk.co.pixl8.obd.type.LinkedHashMapMaxSizeNotCS;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;

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
