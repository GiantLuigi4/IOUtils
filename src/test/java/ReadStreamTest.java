import com.tfc.io.InputUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class ReadStreamTest {
	public static void main(String[] args) throws IOException {
		System.out.println(InputUtils.readFile("fileReadTest.txt").length());
		System.out.println(InputUtils.read(new FileInputStream("fileReadTest.txt")).length());
		System.out.println(InputUtils.readFully(new FileInputStream("fileReadTest.txt")).length());
	}
}
