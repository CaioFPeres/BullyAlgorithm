import java.net.*; 
import java.io.*;

public class Client extends Node {
	
	Socket clientSocket;
	DataOutputStream clientDataOut;
	
	int portNum;
	
	public Client(int port)
	{
		this.portNum = port;
	}


	public void connect(int port)
	{
		try
        { 
        	clientSocket = new Socket("localhost", port);
			clientDataOut = new DataOutputStream(clientSocket.getOutputStream());
        } 
       
        catch(Exception e) {
			System.out.println("Erro: " + e);
		}
	}

	public void SendMessage(String msg)
	{ 
	
		try
		{
			clientDataOut.writeUTF(msg);
		}
		catch(Exception e)
		{

		}
		
	}

}
