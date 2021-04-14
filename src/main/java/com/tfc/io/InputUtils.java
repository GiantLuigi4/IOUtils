package com.tfc.io;

import java.io.*;
import java.util.Objects;

import static com.tfc.io.GeneralUtils.closeAndFlush;

@SuppressWarnings({"unused", "unchecked", "RedundantSuppression", "UnusedReturnValue"})
public class InputUtils {
	public static String readFromCL(String path) {
		try {
			return readFully(Objects.requireNonNull(InputUtils.class.getClassLoader().getResourceAsStream(path)));
		} catch (Throwable err) {
			err.printStackTrace();
			return null;
		}
	}
	
	public static String read(InputStream stream) throws IOException {
		StringBuilder builder = new StringBuilder();
		byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		closeAndFlush(stream);
		builder.append(new String(bytes));
		return builder.toString();
	}
	
	public static byte[] readBytes(InputStream stream) throws IOException {
		byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		closeAndFlush(stream);
		return bytes;
	}
	
	public static byte[] readWithoutClosing(InputStream stream) throws IOException {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		int b;
		while ((b = stream.read()) != -1) {
			stream1.write(b);
		}
		return stream1.toByteArray();
	}
	
	public static String readFully(InputStream stream) throws IOException {
		StringBuilder builder = new StringBuilder();
		while (stream.available() != 0) {
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			builder.append(new String(bytes));
		}
		closeAndFlush(stream);
		return builder.toString();
	}
	
	public static String readFile(String file) {
		try {
			FileInputStream stream = new FileInputStream(new File(file));
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			closeAndFlush(stream);
			return new String(bytes);
		} catch (Throwable ignored) {
			return null;
		}
	}
}
