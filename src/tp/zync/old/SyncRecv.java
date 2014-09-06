package tp.zync.old;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import tp.zync.Packet;

public class SyncRecv {

	static ObjectOutputStream oOut;
	static ObjectInputStream oIn;
//	static BufferedReader br;
	static ServerSocket serverSocket;
	
	static HashMap<String, Long> fileMap;
	static String recvSrcFolder;
	static String senderSrcFolder;
	
	public static void main(String[] args) throws InterruptedException {

		//select folder
		recvSrcFolder = "F:\\00 xtra OS data\\Downloads\\test2";
//		String folderName = "C:\\Users\\Aayush\\Desktop\\t2";
//		String folderName = "F:\\Fotus\\NYC Naagu";

		
//		File f = new File(folderName);

//		FileInputStream fIn;

		//recv
		int portNumber = 27022;

		serverSocket = null;
		try {
			serverSocket = new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();
			System.out.println(((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getPort() );
			System.out.println(((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getAddress() );
		
			oIn = new ObjectInputStream(clientSocket.getInputStream());
			oOut = new ObjectOutputStream(clientSocket.getOutputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
		//			BufferedReader in = new BufferedReader(
		//					new InputStreamReader(clientSocket.getInputStream()));

		//start recv
		try {
			
			Packet p = (Packet) oIn.readObject();
			
			senderSrcFolder = p.fileName;
			String destFileName = null;
			FileOutputStream fOut = null;
			
			System.out.println(p.pcktType + "\t" + p.fileName);
			
			if (p.pcktType.equals("start")){
				
				p = (Packet) oIn.readObject();
				
				while(!p.pcktType.equals("end")){
					
					//System.out.println(p.pcktType);
					
					if(p.pcktType.equals("check")){
						fileMap = p.fileMap;
						//check in folder what files are needed
						Path root = Paths.get(recvSrcFolder);
						Files.walkFileTree(root, new VisitNEdit());

						Packet pCheckWrite = new Packet();
						pCheckWrite.pcktType = "check";
						pCheckWrite.fileMap = fileMap;
						oOut.writeObject(p);
						oOut.reset();
						
					}else if(p.pcktType.equals("fileStart")){
						String srcName = p.fileName;
						String newFile = srcName.substring( senderSrcFolder.length() );
						System.out.println("file name" + newFile);
						destFileName = recvSrcFolder + newFile;
						System.out.println(destFileName);
//						System.out.println("name---" + p.pcktType + "\t" + p.fileType + "\t" + p.fileName);
						
						File newFileFile = new File(destFileName);
						//create folder if not exists
						String destParent = newFileFile.getParent();
						new File(destParent).mkdirs();
						
						fOut = new FileOutputStream(newFileFile, false);
						
					}
					else if(p.pcktType.equals("data")){
						System.out.println("data aya");
						
						fOut.write(p.bArray, 0, p.size);
						

						
					}else if(p.pcktType.equals("dirName")){
						String src = p.fileName;
						String newFile = src.substring( senderSrcFolder.length() );
						destFileName = recvSrcFolder + newFile;
						System.out.println(destFileName);
						System.out.println("dir name" + newFile);
						(new File(destFileName)).mkdirs();

					}else if(p.pcktType.equals("endFile")){
						System.out.println("file end");
						fOut.close(); 
						oOut.writeObject(p);
						oOut.reset();
					}
					
					p = (Packet) oIn.readObject();
				}
			}
			
			oOut.writeObject(p);
			oOut.reset();
			Thread.sleep(888);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//end
		
		
		//start send

		//send end start
		
		
		
	}

}


class VisitNEdit extends SimpleFileVisitor<Path> {

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

		String onlyFileName = file.toString().substring(SyncRecv.recvSrcFolder.length());
		
		String nameOnSender = SyncRecv.senderSrcFolder + onlyFileName;
		
		//check if this is present in map... if yes then remove it
		if (SyncRecv.fileMap.containsKey(nameOnSender) && SyncRecv.fileMap.get(nameOnSender) == attr.size()){
			SyncRecv.fileMap.remove(nameOnSender);
		}
		

		return FileVisitResult.CONTINUE;
	}


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
