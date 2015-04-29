package uk.co.pixl8.obd.type;

import java.util.LinkedHashMap;

public class LinkedHashMapMaxSize<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = -2530870450279283219L;
	private final int maxSize;


	public LinkedHashMapMaxSize(int maxSize){
		this.maxSize=maxSize;
	}
	
	public LinkedHashMapMaxSize(int initialCapacity,int maxSize){
		super(initialCapacity);
		this.maxSize=maxSize;
	}
	
	public LinkedHashMapMaxSize(int initialCapacity, float loadFactor,int maxSize) {
		super(initialCapacity, loadFactor);
		this.maxSize=maxSize;
	}
	
	public LinkedHashMapMaxSize(int initialCapacity,float loadFactor,boolean accessOrder,int maxSize) {
		super(initialCapacity,loadFactor,accessOrder);
		this.maxSize=maxSize;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return maxSize>0 && size() > maxSize;
	}
}
