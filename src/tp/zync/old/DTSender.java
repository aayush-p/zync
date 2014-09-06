package tp.zync.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import tp.zync.Packet;

public class DTSender {

	static ObjectOutputStream oOut;
	static ObjectInputStream oIn;
//	static BufferedReader br;
	static Socket s;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int portNumber = 27000;
		String hostIP = "127.0.0.1";

		s = null;
		try {
			s = new Socket(hostIP, portNumber);
			
			DTSender.oOut = new ObjectOutputStream(s.getOutputStream());
			DTSender.oIn = new ObjectInputStream(s.getInputStream());
//			DTSender.br = new BufferedReader( new InputStreamReader(s.getInputStream()) );
			
			
			String folderName = "C:\\Users\\Aayush\\Desktop\\";
//			String fileName = "Infosys_Sal_00174041_2013.PDF";
			String fileName = "todoDB.txt";
			
			sendFile(folderName, fileName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception caught when trying to listen on port "
					+ portNumber + " or listening for a connection");
			//System.out.println(e.getMessage());
			e.printStackTrace();
		}


	}

	private static void sendFile(String folderName, String fileName) {
		
		File f = new File(folderName + fileName);
		FileInputStream fIn;
		try {
			fIn = new FileInputStream(f);

			Packet p = new Packet();

			//start pckt
			p.pcktType = "start";
//			p.bArray = fileName.getBytes();
			p.bArray = null;
			p.fileName = fileName;
			DTSender.oOut.writeUnshared(p);
			oOut.flush();

			Packet pRead = (Packet) oIn.readObject();
//			br.read();

			Packet pData = new Packet();
			
			pData.pcktType = "transfer";
			int res = 0;
			byte[] buf = new byte[100];
			while((res = fIn.read(buf)) != -1 ){
//				res = fIn.read(buf);
				
				//write output to packet and send
//				System.out.println(Arrays.toString(buf));
//				System.out.println(new String(buf));
//				System.out.println(buf.toString() + " ... " + res);
				pData.bArray = buf.clone();

				pData.size = res;
//					System.out.println(pData.bArray);
				DTSender.oOut.writeObject(pData);
				pRead = (Packet) oIn.readObject();
				
				oOut.reset();
			}

			//last pckt
			Packet pEnd = new Packet();
			pEnd.pcktType = "end";
			pEnd.bArray = null;
			DTSender.oOut.writeUnshared(pEnd);
			pRead = (Packet) oIn.readObject();
			//			br.read();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
