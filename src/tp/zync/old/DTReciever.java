package tp.zync.old;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import tp.zync.Packet;

public class DTReciever {
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int portNumber = 27000;

		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();     
//			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
//			BufferedReader in = new BufferedReader(
//					new InputStreamReader(clientSocket.getInputStream()));

			ObjectInputStream oIn = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream oOut = new ObjectOutputStream(clientSocket.getOutputStream());
//			PrintWriter bw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()) );
			
			String folderName = "C:\\Users\\Aayush\\Desktop\\test\\";
			
			Packet p = (Packet) oIn.readObject();
			Packet pWrite = new Packet();
			
			
			if (p.pcktType.equals("start")){
				
//				String fileName = p.bArray.toString();
				String fileName = p.fileName;
				System.out.println(fileName);
				
				FileOutputStream fOut = new FileOutputStream(new File(folderName+fileName));
				
				while(true){
//					bw.write("a");
//					bw.flush();
					oOut.writeObject(pWrite);
					p = (Packet) oIn.readObject();
					if (p.pcktType.equals("end")){
						oOut.writeObject(pWrite);
						break;
					}
					System.out.println(p.pcktType);
					System.out.println(p.bArray.toString());
					fOut.write(p.bArray, 0, p.size);
				}
				
				//if end
				fOut.close();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception caught when trying to listen on port "
					+ portNumber + " or listening for a connection");
			//System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
