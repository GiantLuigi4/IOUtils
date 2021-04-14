package com.tfc.io.archive;

import com.tfc.io.OutputUtils;
import com.tfc.io.utils.PairMap;

import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.tfc.io.GeneralUtils.closeAndFlush;
import static com.tfc.io.GeneralUtils.flush;
import static com.tfc.io.InputUtils.readBytes;
import static com.tfc.io.InputUtils.readWithoutClosing;
import static com.tfc.io.OutputUtils.create;

@SuppressWarnings({"unused", "unchecked", "RedundantSuppression", "UnusedReturnValue"})
public class ArchiveFile {
	private final File thisFile;
	private final ArrayList<ZipEntry> folders = new ArrayList<>();
	private final PairMap<ZipEntry, byte[]> entries = new PairMap<>();
	
	public ArchiveFile(File file) {
		thisFile = file;
		try {
			if (!file.exists()) {
				create(file);
				FileOutputStream stream1 = new FileOutputStream(file);
				ZipOutputStream stream = thisFile.getName().endsWith(".jar") ? new JarOutputStream(stream1) : new ZipOutputStream(stream1);
				stream.finish();
				closeAndFlush(stream);
				flush(stream1);
			}
			refresh();
		} catch (IOException err) {
			throw new RuntimeException(err);
		}
	}
	
	public ArchiveFile(byte[] bytes) {
		thisFile = null;
		try {
			ByteArrayInputStream stream1 = new ByteArrayInputStream(bytes);
			ZipInputStream stream = new ZipInputStream(stream1);
			ZipEntry entry;
			while ((entry = stream.getNextEntry()) != null) {
				byte[] bytes1 = readWithoutClosing(stream);
				stream.closeEntry();
				entries.put(entry, bytes1);
			}
			closeAndFlush(stream);
			closeAndFlush(stream1);
		} catch (IOException err) {
			throw new RuntimeException(err);
		}
	}
	
	public ArchiveFile() {
		thisFile = null;
	}
	
	public ArchiveFile merge(ArchiveFile other) {
		for (ZipEntry entry : other.getEntries()) {
			if (!hasFile(entry.getName())) {
				if (entry.isDirectory()) folders.add(entryCopy(entry.getName(), entry));
				else putFile(entry.getName(), other.search(entry.getName()));
			}
		}
		return this;
	}
	
	public Collection<ZipEntry> getEntries() {
		Collection<ZipEntry> entries = this.entries.keySet();
		entries.addAll(folders);
		return entries;
	}
	
	/**
	 * Clears the archive wrapper and fills it out with whatever is on the disk
	 */
	public void refresh() throws IOException {
		entries.clear();
		folders.clear();
		ZipFile file1 = thisFile.getName().endsWith(".jar") ? new JarFile(thisFile) : new ZipFile(thisFile);
		Stream<? extends ZipEntry> stream = file1.stream();
		stream.forEach((entry) -> {
			if (entry.isDirectory()) {
				folders.add(new ZipEntry(entry));
			} else {
				try {
					InputStream stream1 = file1.getInputStream(entry);
					entries.put(entryCopy(entry.getName(), entry), readBytes(stream1));
					closeAndFlush(stream1);
				} catch (IOException err) {
					throw new RuntimeException(err);
				}
			}
		});
		closeAndFlush(stream);
		closeAndFlush(file1);
	}
	
	/**
	 * finds an entry in the archive's cached contents and returns the bytes of it
	 *
	 * @param entry the zip entry
	 * @return the bytes of the entry
	 */
	public byte[] search(ZipEntry entry) {
		return entries.getOrDefault(entry, search(entry.getName()));
	}
	
	/**
	 * finds an entry in the archive's cached contents and returns the bytes of it
	 *
	 * @param name the name of the zip entry
	 * @return the bytes of the entry
	 */
	public byte[] search(String name) {
		ZipEntry[] keys = this.entries.keySet().toArray(new ZipEntry[0]);
		byte[][] values = this.entries.values().toArray(new byte[0][0]);
		for (int index = 0; index < keys.length; index++)
			if (keys[index].getName().equals(name))
				return values[index];
		return null;
	}
	
	public void putFile(String entryName, String text) {
		putFile(entryName, text.getBytes());
	}
	
	public void putFile(String entryName, byte[] bytes) {
		entryName = entryName.replace("\\", "/");
		String path = "";
		String[] split = entryName.split("/");
		for (String s : split) {
			path += s + "/";
			if (!s.equals(split[split.length - 1]))
				if (!hasFile(path))
					folders.add(entryNow(path));
		}
		entries.put(entryNow(entryName), bytes);
	}
	
	protected void putFile(String entryName, byte[] bytes, long creation) {
		entryName = entryName.replace("\\", "/");
		String path = "";
		String[] split = entryName.split("/");
		for (String s : split) {
			path += s + "/";
			if (!s.equals(split[split.length - 1]))
				if (!hasFile(path))
					folders.add(entryNow(path));
		}
		entries.put(entryNowAndThen(entryName, creation), bytes);
	}
	
	public static ZipEntry entryNow(String file) {
		return new ZipEntry(file).setLastModifiedTime(FileTime.fromMillis(new Date().getTime())).setCreationTime(FileTime.fromMillis(new Date().getTime()));
	}
	
	public static ZipEntry entryNowAndThen(String file, long creation) {
		return entryNow(file).setCreationTime(FileTime.fromMillis(creation));
	}
	
	public static ZipEntry entryCopy(String file, ZipEntry source) {
		ZipEntry entry = new ZipEntry(file);
		if (source.getCreationTime() != null) entry.setCreationTime(source.getCreationTime());
		if (source.getLastModifiedTime() != null) entry.setLastModifiedTime(source.getLastModifiedTime());
		if (source.getLastAccessTime() != null) entry.setLastAccessTime(source.getLastAccessTime());
		entry.setCrc(source.getCrc());
		entry.setComment(source.getComment());
		entry.setExtra(source.getExtra());
		entry.setTime(source.getTime());
		entry.setMethod(source.getMethod());
		entry.setCompressedSize(source.getCompressedSize());
		entry.setSize(source.getSize());
		return entry;
	}
	
	public void putIfAbsent(String entryName, String text) {
		if (hasFile(entryName)) return;
		putFile(entryName, text.getBytes());
	}
	
	public void putIfAbsent(String entryName, byte[] bytes) {
		if (hasFile(entryName)) return;
		putFile(entryName, bytes);
	}
	
	public boolean remove(String entryName) {
		if (hasFile(entryName)) return removeFile(entryName) || removeFolder(entryName);
		return false;
	}
	
	public boolean removeFile(String entryName) {
		if (entries.containsKey(new ZipEntry(entryName))) {
			entries.remove(new ZipEntry(entryName));
		} else {
			int index = 0;
			Collection<ZipEntry> entries = this.entries.keySet();
			for (ZipEntry folder : entries) {
				if (folder.getName().equals(entryName))
					break;
				index++;
			}
			if (index > entries.size()) return false;
			this.entries.remove(index);
		}
		return true;
	}
	
	public boolean removeFolder(String entryName) {
		if (folders.contains(new ZipEntry(entryName))) {
			folders.remove(new ZipEntry(entryName));
		} else {
			int index = 0;
			for (ZipEntry folder : folders) {
				if (folder.getName().equals(entryName))
					break;
				index++;
			}
			if (index > folders.size()) return false;
			folders.remove(index);
		}
		return true;
	}
	
	public void write(File file) throws IOException {
		byte[] bytes = toByteArray();
		create(file);
		OutputUtils.write(file, bytes);
	}
	
	public void write() throws IOException {
		if (thisFile == null)
			throw new IOException("Cannot write zip file to source file if the source file does not exist (the archive was created using a constructor which does not take file as an argument)");
		write(thisFile);
	}
	
	/**
	 * Checks if a file/folder exists in the archive
	 * @param entry the entry of the file/folder to look for
	 * @return whether or not the file/folder exists in the archive
	 */
	public boolean hasFile(ZipEntry entry) {
		return entries.containsKey(entry) || folders.contains(entry) || searchFolders(entry.getName()) != null || search(entry.getName()) != null;
	}
	
	public ZipEntry searchFolders(String name) {
		for (ZipEntry folder : folders)
			if (folder.getName().equals(name))
				return folder;
		return null;
	}
	
	/**
	 * Checks if a file/folder exists in the archive
	 * @param name the name of the file/folder to look for
	 * @return whether or not the file/folder exists in the archive
	 */
	public boolean hasFile(String name) {
		return search(name) != null || searchFolders(name) != null;
	}
	
	public void replace(String entryName, String text) {
		replace(entryName, text.getBytes());
	}
	
	public void replace(String entryName, byte[] bytes) {
		long creation = new Date().getTime();
		if (hasFile(entryName)) {
			FileTime time = getEntry(entryName).getCreationTime();
			if (time != null) creation = time.toMillis();
			remove(entryName);
		}
		System.out.println(creation);
		putFile(entryName, bytes, creation);
	}
	
	public ZipEntry getEntry(String entryName) {
		for (ZipEntry entry : entries.keySet())
			if (entry.getName().equals(entryName))
				return entry;
		for (ZipEntry entry : folders)
			if (entry.getName().equals(entryName))
				return entry;
		return null;
	}
	
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		ZipOutputStream stream = new ZipOutputStream(stream1);
		for (ZipEntry folder : folders) {
			stream.putNextEntry(folder);
			stream.closeEntry();
		}
		entries.forEach((entry, contents) -> {
			try {
				stream.putNextEntry(entry);
				stream.write(contents);
				stream.closeEntry();
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		});
		closeAndFlush(stream);
		byte[] bytes = stream1.toByteArray();
		closeAndFlush(stream1);
		return bytes;
	}
	
	public long getLastModified() {
		return thisFile.lastModified();
	}
}
