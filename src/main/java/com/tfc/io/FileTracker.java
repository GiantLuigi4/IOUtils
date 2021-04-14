package com.tfc.io;

import java.io.File;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "unchecked", "RedundantSuppression", "UnusedReturnValue"})
public class FileTracker {
	long lastModified = 0;
	String file;
	Consumer<String> onUpdate;
	
	public FileTracker(String file, Consumer<String> onUpdate) {
		this.lastModified = lastModified;
		this.file = file;
		this.onUpdate = onUpdate;
		lastModified = new File(file).lastModified();
	}
	
	public void tick() {
		if (lastModified < new File(file).lastModified()) {
			onUpdate.accept(InputUtils.readFile(file));
			lastModified = new File(file).lastModified();
		}
	}
}
