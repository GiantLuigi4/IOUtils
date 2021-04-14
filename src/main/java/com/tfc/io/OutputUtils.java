package com.tfc.io;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Supplier;
import static com.tfc.io.GeneralUtils.*;

public class OutputUtils {
	public static String readOrCreate(String file, Supplier<String> defaultVal) {
		try {
			File f = new File(file);
			if (!f.exists()) {
				create(file);
				String def = defaultVal.get();
				write(f, def);
				return def;
			} else {
				String returnVal = InputUtils.readFile(file);
				return returnVal == null ? defaultVal.get() : returnVal;
			}
		} catch (Throwable ignored) {
			return defaultVal.get();
		}
	}
	
	public static String readOrCreate(File file, Supplier<String> defaultVal) {
		return readOrCreate(file.getPath(), defaultVal);
	}
	
	public static String readOrCreate(File file) {
		return readOrCreate(file.getPath(), () -> "");
	}
	
	public static String readOrCreate(String file) {
		return readOrCreate(file, () -> "");
	}
	
	public static boolean create(File file) {
		if (file.exists()) return false;
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			return true;
		} catch (Throwable ignored) {
			return false;
		}
	}
	
	public static boolean create(String file) {
		return create(new File(file));
	}
	
	public static boolean write(File file, String text) {
		return write(file, text.getBytes());
	}
	
	public static boolean write(String file, String text) {
		return write(new File(file), text.getBytes());
	}
	
	public static boolean write(String file, byte[] bytes) {
		return write(new File(file), bytes);
	}
	
	public static boolean write(File file, byte[] bytes) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(bytes);
			closeAndFlush(stream);
			return true;
		} catch (Throwable ignored) {
			return false;
		}
	}
}
