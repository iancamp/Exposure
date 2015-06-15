/**
 * @author Ian Campbell iancamp@udel.edu Linkedin.com/in/iancamp
 */

package ExposureServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
 
public class Server 
{
 
    private static Socket socket;
 
    public static void main(String[] args) 
    {
        try
        {
 
            int port = 25000;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server Started and listening to the port 25000");
 
            //Server is running always. This is done using this while(true) loop
            while(true) 
            {
                //Reading the message from the client
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String company = br.readLine(); 
                System.out.println("Message received from client is "+ company);

                String returnMessage;
                try
                {
                    //int numberInIntFormat = Integer.parseInt(number);
                    System.out.println("Waiting8");
                    //int returnValue = numberInIntFormat*2;
                    //returnMessage = String.valueOf(returnValue) + "\n";
                    
                    returnMessage = DataProcess.run(company).toString();
                }
                catch(Exception e)
                {
                    //Input was not a number. Sending proper message back to client.
                    returnMessage = "Unable to find a stock ticker for that company\n";
                }
                System.out.println("Waiting9");
 
                System.out.println("RETURN MESSAGE: " + returnMessage);
                //Sending the response back to the client.
                OutputStream os = socket.getOutputStream();
                System.out.println("Waiting10");
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                System.out.println("Waiting12");
                bw.write(returnMessage);
                System.out.println("Waiting13");
                System.out.println("Message sent to the client is \n"+returnMessage);
                bw.flush();
                System.out.println("Waiting14");
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
}