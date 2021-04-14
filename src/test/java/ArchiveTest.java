import com.tfc.io.archive.ArchiveFile;

import java.io.File;
import java.io.IOException;

public class ArchiveTest {
	public static void main(String[] args) throws IOException {
		ArchiveFile file = new ArchiveFile(new File("test.zip"));
		file.remove("test3.txt");
		if (!file.hasFile("test1.txt")) file.putFile("test1.txt", "hello, this file did not exist last run");
		else file.replace("test1.txt", "hello, this file did exist last run");
		file.putIfAbsent("hello.txt", "hello, how are you doing today?");
		byte[] bytes = file.search("test2.txt");
		file.replace("test2.txt", bytes == null ? "1" : (Integer.parseInt(new String(bytes)) + 1 + ""));
		file.write();
		System.out.println(new String(file.search("hello.txt")));
		System.out.println(new String(file.search("test1.txt")));
		
		ArchiveFile file1 = new ArchiveFile(new File("test.zip"));
		file1.putIfAbsent("test3.txt", "this was written through a second archive file instance");
		file1.write();
		
		file.refresh();
		file.replace("test4.txt", "test3.txt was" + (file.hasFile("test3.txt") ? "" : " not") + " found");
		file.write();
		
		ArchiveFile fileJar = new ArchiveFile(new File("test.jar")).merge(file);
		fileJar.replace("test/test.txt", "folders behave like this");
		fileJar.replace("test/test/test.txt", "folders in folders behave like this");
		fileJar.write();
		
		ArchiveFile file2 = new ArchiveFile(fileJar.toByteArray());
		file2.write(new File("test2.zip"));
		
		ArchiveFile file3 =  new ArchiveFile();
		file3.putFile("readme.md", "hello, yes, this zip was created in memory");
		ArchiveFile file4 =  new ArchiveFile();
		file4.putFile("yes.txt", "yes, inner zips work");
		file3.putFile("inner zip.zip", file4.toByteArray());
		file3.write(new File("test3.zip"));
		
		ArchiveFile file5 = new ArchiveFile(new File("test3.zip"));
		ArchiveFile innerZip = new ArchiveFile(file5.search("inner zip.zip"));
		System.out.println(new String(innerZip.search("yes.txt")));
	}
}
