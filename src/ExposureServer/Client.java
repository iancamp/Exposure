package ExposureServer;

import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	public static void main(String args[]){  
		try{  
			Socket s = new Socket("localhost",5555);  
			OutputStream os = s.getOutputStream();  
			ObjectOutput oos = new ObjectOutputStream(os);  
			String to = new String("object from client");  
			oos.writeObject(to);  
			oos.writeObject(new String("another object from the client"));  
			oos.close();  
			os.close();  
			s.close();  
		}catch(Exception e){System.out.println(e);}  
	}  
}
