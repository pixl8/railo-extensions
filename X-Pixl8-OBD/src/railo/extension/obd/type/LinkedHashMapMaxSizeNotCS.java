package railo.extension.obd.type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.exp.PageException;
import railo.runtime.util.Cast;

public class LinkedHashMapMaxSizeNotCS extends LinkedHashMapMaxSize<Object,Object> {
	
	private static final long serialVersionUID = -5213718245563794026L;
	private Cast caster;

	public LinkedHashMapMaxSizeNotCS(int maxSize){
		super(maxSize);
		caster=CFMLEngineFactory.getInstance().getCastUtil();
	}
	
	public LinkedHashMapMaxSizeNotCS(int initialCapacity,int maxSize){
		super(initialCapacity,maxSize);
	}
	
	public LinkedHashMapMaxSizeNotCS(int initialCapacity, float loadFactor,int maxSize) {
		super(initialCapacity, loadFactor,maxSize);
	}
	
	public LinkedHashMapMaxSizeNotCS(int initialCapacity,float loadFactor,boolean accessOrder,int maxSize) {
		super(initialCapacity,loadFactor,accessOrder,maxSize);
	}

	@Override
	public Object get(Object key) {
		return super.get(toKey(key));
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(toKey(key));
	}

	@Override
	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		Set<Entry<Object, Object>> src = super.entrySet();
		Set<Entry<Object, Object>> trg=new LinkedHashSet<Entry<Object, Object>>();
		Iterator<Entry<Object, Object>> it = src.iterator();
		Entry<Object, Object> e;
		while(it.hasNext()){
			e = it.next();
			trg.add(new EntryImpl(e.getKey(),e.getValue()));
		}
		return trg;
	}

	@Override
	public Set<Object> keySet() {
		Set<Object> src = super.keySet();
		Set<Object> trg=new LinkedHashSet<Object>();
		Iterator<Object> it = src.iterator();
		while(it.hasNext()){
			trg.add(it.next());
		}
		return trg;
	}

	@Override
	public Object put(Object key, Object value) {
		return super.put(toKey(key), value);
	}

	@Override
	public void putAll(Map<? extends Object, ? extends Object> map) {
		Iterator<?> it = map.entrySet().iterator();
		Map.Entry e;
	    while (it.hasNext()) {
	        e = (Map.Entry) it.next();
	        put(e.getKey(), e.getValue());
	    }
	}
	


	private Object toKey(Object key) {
		try {
			return caster.toKey(key);
		} catch (PageException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class EntryImpl implements Entry<Object, Object> {

		private Object key;
		private Object value;

		public EntryImpl(Object key, Object value) {
			this.key=key;
			this.value=value;
		}

		@Override
		public Object getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException("this operation is not supported");
		}
	}

}
