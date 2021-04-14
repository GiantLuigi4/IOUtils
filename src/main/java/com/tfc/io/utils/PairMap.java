package com.tfc.io.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

public class PairMap<A, B> {
	private final ArrayList<Pair<A, B>> pairs = new ArrayList<>();
	
	public Collection<A> keySet() {
		ArrayList<A> objects = new ArrayList<>();
		for (Pair<A, B> pair : pairs) objects.add(pair.getFirst());
		return objects;
	}
	
	public Collection<B> values() {
		ArrayList<B> objects = new ArrayList<>();
		for (Pair<A, B> pair : pairs) objects.add(pair.getSecond());
		return objects;
	}
	
	public void put(A key, B value) {
		pairs.add(new Pair<>(key, value));
	}
	
	public B getOrDefault(A key, B defaultVal) {
		B val = get(key);
		return val == null ? defaultVal : val;
	}
	
	public B get(A key) {
		int hash = key.hashCode();
		for (Pair<A, B> pair : pairs) {
			if (
					hash == pair.getFirst().hashCode() &&
							key.equals(pair.getFirst())
			) {
				return pair.getSecond();
			}
		}
		return null;
	}
	
	public boolean containsKey(A key) {
		int hash = key.hashCode();
		for (Pair<A, B> pair : pairs) {
			if (
					hash == pair.getFirst().hashCode() &&
							key.equals(pair.getFirst())
			) {
				return true;
			}
		}
		return false;
	}
	
	public void forEach(BiConsumer<A, B> consumer) {
		for (Pair<A, B> pair : pairs)
			consumer.accept(pair.getFirst(), pair.getSecond());
	}
	
	public boolean remove(A key) {
		if (!containsKey(key)) return false;
		int index = 0;
		int hash = key.hashCode();
		for (Pair<A, B> pair : pairs) {
			if (hash == pair.getFirst().hashCode() && key.equals(pair.getFirst())) break;
			index++;
		}
		pairs.remove(index);
		return true;
	}
	
	public boolean remove(int index) {
		pairs.remove(index);
		return true;
	}
	
	public void clear() {
		pairs.clear();
	}
}
