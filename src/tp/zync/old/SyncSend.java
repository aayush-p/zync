package tp.zync.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTextArea;

import tp.zync.Packet;
import tp.zync.SendWorker;

public class SyncSend {

	static ObjectOutputStream oOut;
	static ObjectInputStream oIn;
	static JTextArea tf;
//static BufferedReader br;
	static Socket clientSocket;
	static SendWorker sendWorker;
	
	static HashMap<String, Long> fileMap = new HashMap<String, Long>();
	
	public static void mainStartFn(String currentFolder, String ip, String port, SendWorker sw) throws InterruptedException, ClassNotFoundException{

		sendWorker = sw;
	
//		sendWorker.publish();
		
//		tf = tf4;
		//select folder
		//String folderName = "C:\\Users\\Aayush\\Desktop\\Datafiction";
//		String folderName = "F:\\00 xtra OS data\\Downloads\\test_dt";
//		String folderName = "F:\\The.Other.Woman.2014.HDRip.XViD.juggs[ETRG]";
		String folderName = currentFolder;
		
		//		File f = new File(folderName);

//		FileInputStream fIn;
		
		//send
//		int portNumber = 27022;
//		String hostIP = "127.0.0.1";
		String hostIP = ip;
		int portNumber = Integer.parseInt(port);

		//s = null;

		boolean flag = true;
		
		while(flag){
			
			try {
				
				clientSocket = new Socket(hostIP, portNumber);
				
				oOut = new ObjectOutputStream(clientSocket.getOutputStream());
				oIn = new ObjectInputStream(clientSocket.getInputStream());
				
				startSending(folderName);
				
				flag = false;
				
			} catch (IOException e) {
				flag = false;
				//repeat until complete or die trying
				e.printStackTrace();
				System.out.println("repeatingggggg..");
				Thread.sleep(5000);
			}
		}
		
	}

	private static void startSending(String folderName) throws IOException, ClassNotFoundException {

		//start send
		Packet p = new Packet();
		p.pcktType = "start";
		p.fileName = folderName;

		SyncSend.oOut.writeObject(p);
		SyncSend.oOut.reset();

		Path root = Paths.get(folderName);
		Files.walkFileTree(root, new VisitNSave());
		
		//now we have all files list
		//check with reciever what files to send
		p = new Packet();
		p.pcktType = "check";
		p.fileMap = fileMap;
		
//		System.out.println("Sending Check");
		tf.append("Sending Check");

		SyncSend.oOut.writeObject(p);
		SyncSend.oOut.reset();
		
		//receive req files map
		Packet pRead = (Packet) SyncSend.oIn.readObject();
		fileMap = pRead.fileMap;
		
		
		//now send only these files...
		Iterator it = fileMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry) it.next();
			//send this file
			sendFile(entry.getKey().toString());
			it.remove();
		}
		
		//end
		p = new Packet();
		p.pcktType = "end";
		
		System.out.println("end");

		SyncSend.oOut.writeObject(p);
		SyncSend.oOut.reset();
		
		//last read
		pRead = (Packet) oIn.readObject();
		

		//send end start
		
		//start recv
		
		//end
		
	}



	private static void sendFile(String file) throws IOException {

		System.out.println("sending file: " + file.toString());

		Packet p = new Packet();
		p.pcktType = "fileStart";
		p.fileName = file.toString();
//		p.fileType = "file";

		SyncSend.oOut.writeObject(p);
		SyncSend.oOut.reset();

		//now send data
		p.pcktType = "data";
		p.fileName = file.toString();
//		p.fileType = "file";

		FileInputStream fIn = new FileInputStream(new File(file));

		//	System.out.println("data");
		int res = 0;
		byte[] buf = new byte[1024*1024];
		while((res = fIn.read(buf)) != -1 ){
			//		res = fIn.read(buf);

			//write output to packet and send
			//		System.out.println(Arrays.toString(buf));
			//		System.out.println(new String(buf));
			//		System.out.println(buf.toString() + " ... " + res);
			p.bArray = buf;

			p.size = res;
			//			System.out.println(pData.bArray);

			SyncSend.oOut.writeObject(p);
			SyncSend.oOut.reset();
		}

		fIn.close();

		p.pcktType = "fileEnd";
		p.fileName = file.toString();
//		p.fileType = "file";

		SyncSend.oOut.writeObject(p);
		SyncSend.oOut.reset();
		

	}


}


class VisitNSave extends SimpleFileVisitor<Path> {

	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attr) throws IOException {
		if (attr.isSymbolicLink()) {
			System.out.format("Symbolic link: %s ", file);
		} else if (attr.isRegularFile()) {
			System.out.format("Regular file: %s ", file);
		} else {
			System.out.format("Other: %s ", file);
		}
		System.out.println("(" + attr.size() + "bytes)");


		//add each file to the map 
		SyncSend.fileMap.put(file.toString(), attr.size());



		return FileVisitResult.CONTINUE;
	}

	// Print each directory visited.
	//@Override
	//public FileVisitResult preVisitDirectory(Path dir,
	//		BasicFileAttributes attr) throws IOException {
	//    System.out.format("Directory: %s%n", dir);
	//
	//	Packet p = new Packet();
	//	p.pcktType = "dirName";
	//	p.fileName = dir.toString();
	//	p.fileType = "dir";
	//	
	//	SyncSend.oOut.writeObject(p);
	//	SyncSend.oOut.reset();
	//    
	//    return FileVisitResult.CONTINUE;
	//}

	// If there is some error accessing
	// the file, let the user know.
	// If you don't override this method
	// and an error occurs, an IOException 
	// is thrown.
	@Override
	public FileVisitResult visitFileFailed(Path file,
			IOException exc) {
		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}
}