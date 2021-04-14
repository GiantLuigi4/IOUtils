package com.tfc.io;

import java.io.Closeable;
import java.io.Flushable;

@SuppressWarnings({"unused", "unchecked", "RedundantSuppression", "UnusedReturnValue"})
public class GeneralUtils {
	public static void closeAndFlush(Object closeable) {
		try {
			if (closeable instanceof Closeable) ((Closeable) closeable).close();
			if (closeable instanceof Flushable) ((Flushable) closeable).flush();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public static void flush(Flushable flushable) {
		try {
			flushable.flush();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
}
