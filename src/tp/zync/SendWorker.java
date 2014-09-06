package tp.zync;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 * Searches the text files under the given directory and counts the number of instances a given word is found in these
 * file.
 * 
 * @author Albert Attard
 */
public class SendWorker extends SwingWorker<Integer, String> {

	static ObjectOutputStream oOut;
	static ObjectInputStream oIn;
//static BufferedReader br;
	static Socket clientSocket;
	static SendWorker sendWorker;
	static String senderSrcFolder;
	static String recvSrcFolder;
	
	static HashMap<String, Long> fileMap = new HashMap<String, Long>();

	
	

  private static void failIfInterrupted() throws InterruptedException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException("Interrupted while searching files");
    }
  }

  /** The text area where messages are written. */
  private final JTextArea messagesTextArea;
private String currentFolder;
private String ip;
private String port;



public static void mainStartFn(String currentFolder, String ip, String port, SendWorker sw) throws InterruptedException, ClassNotFoundException, IOException{

	sendWorker = sw;

	sendWorker.publish("in main start send");

	//	tf = tf4;
	//select folder
	//String folderName = "C:\\Users\\Aayush\\Desktop\\Datafiction";
	//	String folderName = "F:\\00 xtra OS data\\Downloads\\test_dt";
	//	String folderName = "F:\\The.Other.Woman.2014.HDRip.XViD.juggs[ETRG]";
	String folderName = currentFolder;
	senderSrcFolder = folderName;

	//		File f = new File(folderName);

	//	FileInputStream fIn;

	//send
	//	int portNumber = 27022;
	//	String hostIP = "127.0.0.1";
	String hostIP = ip;
	int portNumber = Integer.parseInt(port);

	//s = null;

	boolean flag = true;

	while(flag){


			clientSocket = new Socket(hostIP, portNumber);

			oOut = new ObjectOutputStream(clientSocket.getOutputStream());
			oIn = new ObjectInputStream(clientSocket.getInputStream());

			startSending(folderName, sw);

			flag = false;

	}
	
	
	

}


private static void startSending(String folderName, SendWorker sw) throws IOException, ClassNotFoundException {

	sendWorker.publish("in main -> startSending send");
	
	//start send
	Packet p = new Packet();
	p.pcktType = "start";
	p.fileName = folderName;

	SendWorker.oOut.writeObject(p);
	SendWorker.oOut.reset();

	Path root = Paths.get(folderName);
	Files.walkFileTree(root, new VisitNSaveWorkerSendClass ());
	
	System.out.println("initial " + fileMap.size());
	
	//now we have all files list
	//check with reciever what files to send
	p = new Packet();
	p.pcktType = "check";
	p.fileMap = fileMap;
	
//	System.out.println("Sending Check");
//	tf.append("Sending Check");

	SendWorker.oOut.writeObject(p);
	SendWorker.oOut.reset();
	
	//receive req files map
	Packet pRead = (Packet) SendWorker.oIn.readObject();
	fileMap = (HashMap<String, Long>) pRead.fileMap.clone();
	
	System.out.println("returned " + fileMap.size());
	
	//now send only these files...
	@SuppressWarnings("rawtypes")
	Iterator it = fileMap.entrySet().iterator();
	while(it.hasNext()){
		@SuppressWarnings("rawtypes")
		Map.Entry entry = (Map.Entry) it.next();
		//send this file
		sendFile(entry.getKey().toString());
		it.remove();
	}
	
	//end
	p = new Packet();
	p.pcktType = "end";
	
	sendWorker.publish("end");

	SendWorker.oOut.writeObject(p);
	SendWorker.oOut.reset();
	
	//last read
	pRead = (Packet) oIn.readObject();
	
}


private static void sendFile(String file) throws IOException {

	sendWorker.publish("sending file: " + file.toString());
	System.out.println("sending file: " + file.toString());

	Packet p = new Packet();
	p.pcktType = "fileStart";
	p.fileName = file.toString();
//	p.fileType = "file";

	SendWorker.oOut.writeObject(p);
	SendWorker.oOut.reset();

	//now send data
	p.pcktType = "data";
	p.fileName = file.toString();
//	p.fileType = "file";

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

		SendWorker.oOut.writeObject(p);
		SendWorker.oOut.reset();
	}

	fIn.close();

	p.pcktType = "fileEnd";
	p.fileName = file.toString();
//	p.fileType = "file";

	SendWorker.oOut.writeObject(p);
	SendWorker.oOut.reset();
	

}




  /**
   * Creates an instance of the worker
   * 
   * @param word
   *          The word to search
   * @param directory
   *          the directory under which the search will occur. All text files found under the given directory are
   *          searched
   * @param messagesTextArea
   *          The text area where messages are written
   */
  public SendWorker(final String currentFolder, final String ip, String port, final JTextArea messagesTextArea) {

	  publish("in constrtr send");
	  this.currentFolder = currentFolder;
	  this.ip = ip;
	  this .port = port;
	  this.messagesTextArea = messagesTextArea;
  }

  @Override
  protected Integer doInBackground() {

	  publish("in backgnd send");
	  
	  boolean flag = true;

	  while(flag){
		  try {
			  mainStartFn(currentFolder, ip, port, this);

			  mainRecvFn();

			  flag = false;
		  } catch (Exception e) {
			  flag = true;
			  publish(e.getMessage());
			  publish("repeating after exception in 5s..");
			  
			  if(oOut != null){
				  try {
					oOut.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			  }
			  if(oIn != null){
				  try {
					oIn.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			  }
			  if(clientSocket != null){
				  try {
					clientSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			  }

			  fileMap = new HashMap<String, Long>();
			  
			  e.printStackTrace();

			  try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  }  
	  }
	  
//	  SendWorker.failIfInterrupted();
	  
//	  publish("Listing all text files under the directory: " + directory);
	  

    // Return the number of matches found
    return 0;
  }
  
  

  private void mainRecvFn() throws InterruptedException, IOException, ClassNotFoundException {

	  //now recv
	  sendWorker.publish("starting recv");

	  //select folder
	  //		sendSrcFolder = "F:\\00 xtra OS data\\Downloads\\test2";
	  //		String folderName = "C:\\Users\\Aayush\\Desktop\\t2";
	  //		String folderName = "F:\\Fotus\\NYC Naagu";


	  //		File f = new File(folderName);

	  //		FileInputStream fIn;

	  //recv
	  //		int portNumber = 27022;
	  //		int portNumber = Integer.parseInt(port);

	  //		serverSocket = null;
	  //		try {
	  //			serverSocket = new ServerSocket(portNumber);
	  //			Socket clientSocket = serverSocket.accept();
	  //			sendWorker.publish(((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getPort() + "" );
	  //			sendWorker.publish(((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getAddress() + "");
	  //	
	  //			oIn = new ObjectInputStream(clientSocket.getInputStream());
	  //			oOut = new ObjectOutputStream(clientSocket.getOutputStream());
	  //
	  //		} catch (IOException e) {
	  //			// TODO Auto-generated catch block
	  //			e.printStackTrace();
	  //		}
	  //			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
	  //			BufferedReader in = new BufferedReader(
	  //					new InputStreamReader(clientSocket.getInputStream()));

	  //start recv

		  Packet p = (Packet) oIn.readObject();

		  recvSrcFolder = p.fileName;
		  String destFileName = null;
		  FileOutputStream fOut = null;

		  sendWorker.publish(p.pcktType + "\t" + p.fileName);

		  if (p.pcktType.equals("start")){

			  p = (Packet) oIn.readObject();

			  while(!p.pcktType.equals("end")){

				  //System.out.println(p.pcktType);

				  if(p.pcktType.equals("check")){
					  fileMap = new HashMap<String, Long>(p.fileMap);
					  //check in folder what files are needed
					  Path root = Paths.get(senderSrcFolder);
					  Files.walkFileTree(root, new VisitNEditWorkerSendClass());

					  Packet pCheckWrite = new Packet();
					  pCheckWrite.pcktType = "check";
					  pCheckWrite.fileMap = fileMap;
					  oOut.writeObject(pCheckWrite);
					  oOut.reset();

				  }else if(p.pcktType.equals("fileStart")){
					  String srcName = p.fileName;
					  String newFile = srcName.substring( recvSrcFolder.length() );
					  //						recvWorker.publish("file name" + newFile);
					  destFileName = senderSrcFolder + newFile;
					  sendWorker.publish("receiving file: " + destFileName);
					  System.out.println("receiving file: " + destFileName);
					  //						recvWorker.publish("name---" + p.pcktType + "\t" + p.fileType + "\t" + p.fileName);

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
					  String newFile = src.substring( recvSrcFolder.length() );
					  destFileName = senderSrcFolder + newFile;
					  sendWorker.publish(destFileName);
					  sendWorker.publish("dir name" + newFile);
					  (new File(destFileName)).mkdirs();

				  }else if(p.pcktType.equals("fileEnd")){
//					  sendWorker.publish("file end");
					  fOut.close();
//					  oOut.writeObject(p);
//					  oOut.reset();
				  }

				  p = (Packet) oIn.readObject();
			  }
		  }

		  sendWorker.publish("End sab kuch");

		  oOut.writeObject(p);
		  oOut.reset();
		  Thread.sleep(888);



  }


  @Override
  protected void process(final List<String> chunks) {
	  // Updates the messages text area
	  for (final String string : chunks) {
		  messagesTextArea.append(string);
		  messagesTextArea.append("\n");
	  }
  }
}




class VisitNSaveWorkerSendClass extends SimpleFileVisitor<Path> {

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
		SendWorker.fileMap.put(file.toString(), attr.size());


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


class VisitNEditWorkerSendClass extends SimpleFileVisitor<Path> {

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

		String onlyFileName = file.toString().substring(SendWorker.senderSrcFolder.length());
		
		String nameOnSender = SendWorker.recvSrcFolder + onlyFileName;
		
		//check if this is present in map... if yes then remove it
		if (SendWorker.fileMap.containsKey(nameOnSender) && SendWorker.fileMap.get(nameOnSender) == attr.size()){
			System.out.println("same found: " + nameOnSender);
			SendWorker.fileMap.remove(nameOnSender);
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

