package org.unclazz.javax;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.unclazz.javax.LazyIterable.Supplier;
import org.unclazz.javax.LazyIterable.Yield;
import org.unclazz.javax.LazyIterable.YieldCallable;

public class LazyIterableTest {
	private static void sysoutf(String format, Object... args) {
		System.out.println(String.format("\"%s\" > ", method()) + String.format(format, args));
	}
	
	private static String method() {
		boolean sysoutfFound = false;
		for (StackTraceElement elm : Thread.currentThread().getStackTrace()) {
			final String name = elm.getMethodName();
			if (sysoutfFound) {
				return name;
			} else if (name.startsWith("sysoutf")) {
				sysoutfFound = true;
			}
		}
		return "unknown";
	}

	@Test
	public void forOnce_returnsIterableIncludesOnlyOneElement_case1() {
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		for (final String s : LazyIterable.forOnce(1, new YieldCallable<Integer,String>(){
			@Override
			public Yield<String> yield(Integer item, int index) {
				// Trace
				sysoutf("in yield method. item: %s, index: %s.", item, index);
				// Assert
				assertThat(item, equalTo(1));
				assertThat(index, equalTo(0));

				return Yield.yieldReturn("hello");
			}
			
		})) {
			// Trace
			sysoutf("in for statement. s: %s.", s);
			// Assert
			assertThat(s, equalTo("hello"));
		}
		// Trace
		sysoutf("after \"for\" statement.");
	}

	@Test
	public void forOnce_returnsIterableIncludesOnlyOneElement_case2() {
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		for (final String s : LazyIterable.forOnce(3, new YieldCallable<Integer,String>(){
			@Override
			public Yield<String> yield(Integer item, int index) {
				// Trace
				sysoutf("in yield method. item: %s, index: %s.", item, index);
				// Assert
				assertThat(item, equalTo(3));
				assertThat(index, equalTo(0));

				return Yield.yieldVoid();
			}
			
		})) {
			// Trace
			sysoutf("in for statement. s: %s.", s);
			// Assert
			fail("unreachable code.");
		}
		// Trace
		sysoutf("after \"for\" statement.");
	}

	@Test
	public void forOnce_returnsIterableIncludesOnlyOneElement_case3() {
		
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		for (final String s : LazyIterable.forOnce(3, new YieldCallable<Integer,String>(){
			@Override
			public Yield<String> yield(Integer item, int index) {
				// Trace
				sysoutf("in yield method. item: %s, index: %s.", item, index);
				// Assert
				assertThat(item, equalTo(3));
				assertThat(index, equalTo(0));

				return Yield.yieldVoid();
			}
			
		})) {
			// Trace
			sysoutf("in for statement. s: %s.", s);
			// Assert
			fail("unreachable code.");
		}
		// Trace
		sysoutf("after \"for\" statement.");
	}

	@Test
	public void forEach_returnsIterableIncludesElementsInBaseIterable_case1() {
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		final List<Integer> ints = Arrays.asList(100,101,102,103,104,105,106);
		for (final String s : LazyIterable.forEach(ints, new YieldCallable<Integer,String>(){
			@Override
			public Yield<String> yield(Integer item, int index) {
				// Trace
				sysoutf(String.format("in yield method. item: %s, index: %s.", item, index));
				if (item > 104) {
					return Yield.yieldBreak();
				} else if (item % 2 == 1) {
					return Yield.yieldVoid();
				} else {
					return Yield.yieldReturn(item.toString());
				}
			}
		})) {
			// Trace
			sysoutf(String.format("in for statement. s: %s.", s));
			// Assert
			assertFalse(Arrays.asList("101", "103", "105", "106").contains(s));
		}
		// Trace
		sysoutf("after \"for\" statement.");
	}

	@Test
	public void forEach_returnsIterableIncludesElementsInBaseIterable_case2() {
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		final List<Integer> ints = Arrays.asList(100,101,102,103,104,105,106);
		for (final String s : LazyIterable.forEach(ints, new YieldCallable<Integer,String>(){
			@Override
			public Yield<String> yield(Integer item, int index) {
				// Trace
				sysoutf(String.format("in yield method. item: %s, index: %s.", item, index));
				// Assert
				assertTrue(item < 101);
				assertTrue(index < 1);
				
				return Yield.yieldBreak();
			}
		})) {
			// Trace
			sysoutf(String.format("in for statement. s: %s.", s));
			// Assert
			fail("unreachable code.");
		}
		// Trace
		sysoutf("after \"for\" statement.");
	}

	@Test
	public void forEach_returnsIterableIncludesElementsInBaseIterable_case3() {
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		final List<Integer> ints = Arrays.asList(100,101,102,103,104,105,106);
		final List<Integer> ints2 = new LinkedList<Integer>();
		for (final String s : LazyIterable.forEach(ints, new YieldCallable<Integer,String>(){
			@Override
			public Yield<String> yield(Integer item, int index) {
				// Trace
				sysoutf(String.format("in yield method. item: %s, index: %s.", item, index));
				// Act
				ints2.add(item);
				
				return Yield.yieldVoid();
			}
		})) {
			// Trace
			sysoutf(String.format("in for statement. s: %s.", s));
			// Assert
			fail("unreachable code.");
		}
		// Trace
		sysoutf("after \"for\" statement.");
		// Assert
		assertTrue(ints2.containsAll(ints));
	}
	
	@Test
	public void forEarch_whenApplyToSupplier_returnsIterableEndless() {
		// Trace
		sysoutf("before \"for\" statement.");
		// Arrange
		final Supplier<Integer> fibSupplier = new Supplier<Integer>() {
			private int _0 = -1;
			private int _1 = -1;
			@Override
			public Integer get() {
				if (_0 == -1) {
					return _0 = 0;
				} else if (_1 == -1) {
					return _1 = 1;
				} else {
					final int next1 = _0 + _1;
					_0 = _1;
					_1 = next1;
					return _1;
				}
			}
		};
		for (final String s : LazyIterable.forEach(fibSupplier,
						new YieldCallable<Integer, String>() {
			@Override
			public Yield<String> yield(Integer item, int index) {
				sysoutf("in yield method. fib[%s] = %s.", index, item);
				if (index < 5) {
					return Yield.yieldReturn(item.toString());
				} else {
					return Yield.yieldBreak();
				}
			}
		})) {
			sysoutf("in \"for\" statement. fib[x] = %s.", s);
		}
		// Trace
		sysoutf("after \"for\" statement.");
	}
}
