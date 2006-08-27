/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/

package edu.uci.ics.jung.algorithms.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ConstantMap<K,V> implements Map<K,V> {

	private Map<K,V> delegate;
	
	public ConstantMap(V value) {
		delegate = Collections.singletonMap(null, value);
	}
	public V get(Object key) {
		return delegate.get(null);
	}
	public void clear() {
		delegate.clear();
	}
	public boolean containsKey(Object key) {
		return true;
	}
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}
	public Set<Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}
	public boolean equals(Object o) {
		return delegate.equals(o);
	}
	public int hashCode() {
		return delegate.hashCode();
	}
	public boolean isEmpty() {
		return delegate.isEmpty();
	}
	public Set<K> keySet() {
		return delegate.keySet();
	}
	public V put(K key, V value) {
		return delegate.put(key, value);
	}
	public void putAll(Map<? extends K, ? extends V> t) {
		delegate.putAll(t);
	}
	public V remove(Object key) {
		return delegate.remove(key);
	}
	public int size() {
		return delegate.size();
	}
	public Collection<V> values() {
		return delegate.values();
	}
}