package org.unclazz.javax;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class LazyIterable<U,T> implements Iterable<U> {
	public static final class Yield<T> {
		private static final Yield<?> nullInstance = new Yield<Object>(null, 0);
		private static final Yield<?> voidInstance = new Yield<Object>(null, 1);
		private static final Yield<?> breakInstance = new Yield<Object>(null, 2);
		@SuppressWarnings("unchecked")
		public static<T> Yield<T> yieldReturn(T value) {
			return (Yield<T>) (value == null ? nullInstance : new Yield<T>(value, 0));
		}
		@SuppressWarnings("unchecked")
		public static<T> Yield<T> yieldVoid() {
			return (Yield<T>) voidInstance;
		}
		@SuppressWarnings("unchecked")
		public static<T> Yield<T> yieldBreak() {
			return (Yield<T>) breakInstance;
		}
		
		private final T value;
		private final int mode; // 0: return, 1: void, 2: break
		private Yield(T value, int mode){
			this.value = value;
			this.mode = mode;
		}
		public T get() {
			if (mode == 0) {
				return value;
			} else {
				throw new NoSuchElementException();
			}
		}
		public T get(T defaultValue) {
			return mode == 0 ? value : defaultValue;
		}
		public boolean isReturn() {
			return mode == 0;
		}
		public boolean isVoid() {
			return mode == 1;
		}
		public boolean isBreak() {
			return mode == 2;
		}
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (mode == 0 && other instanceof Yield) {
				final Yield<?> that = (Yield<?>)other;
				return this.get().equals(that.get(null));
			}
			return false;
		}
	}
	public static interface YieldCallable<T,U> {
		Yield<U> yield(T item, int index);
	}
	private static class IteratorBasedLazyIterator<T,U> implements Iterator<U>{
		private final Iterator<T> source;
		private final YieldCallable<T, U> callable;
		
		private boolean hasNextChecked = false;
		private boolean lastNextReturn = true;
		private U nextCached = null;
		private int index = -1;
		
		private IteratorBasedLazyIterator(final Iterator<T> source, final YieldCallable<T, U> callable) {
			this.source = source;
			this.callable = callable;
		}

		@Override
		public boolean hasNext() {
			if (!lastNextReturn) {
				return false;
			}
			while (lastNextReturn = source.hasNext()) {
				final Yield<U> y = callable.yield(source.next(), ++index);
				if (y.isBreak()) {
					break;
				}
				if (y.isVoid()) {
					continue;
				}
				if (y.isReturn()) {
					lastNextReturn = true;
					hasNextChecked = true;
					nextCached = y.get();
					return true;
				}
			}
			lastNextReturn = false;
			hasNextChecked = true;
			nextCached = null;
			return false;
		}

		@Override
		public U next() {
			if (!hasNextChecked) {
				hasNext();
			}
			if (!lastNextReturn) {
				throw new NoSuchElementException();
			}
			hasNextChecked = false;
			return nextCached;
		}
		
		@Override
		public void remove() {
			notSupportedRemoveMethod();
		}
	}
	
	public static<T,U> Iterable<U> forOnce(final T source, final YieldCallable<T, U> callable) {
		return new LazyIterable<U, T>(Collections
				.<T>singletonList(source), callable);
	}
	public static<T,U> Iterable<U> forEach(final Iterable<T> source, final YieldCallable<T, U> callable) {
		return new LazyIterable<U, T>(source, callable);
	}
	private static void notSupportedRemoveMethod() {
		throw new UnsupportedOperationException(String.format(
				"%s and relative iterators does not support remove() method.",
				IteratorBasedLazyIterator.class.getSimpleName()));
	}
	
	private final Iterable<T> source;
	private final YieldCallable<T, U> callable;
	
	private LazyIterable(final Iterable<T> source, final YieldCallable<T, U> callable) {
		this.source = source;
		this.callable = callable;
	}
	
	@Override
	public Iterator<U> iterator() {
		return new IteratorBasedLazyIterator<T, U>(source.iterator(), callable);
	}
}
