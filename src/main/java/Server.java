import java.net.*;
import java.io.*;


public class Server extends Node implements Runnable
{
	
	Socket socket = null;
	ServerSocket server = null;
	DataInputStream dataIn = null;

	static volatile boolean electionHappening = false;
	static volatile boolean commStarted = false;

	volatile Timeout timeOutThread;
	volatile ComTimeout comThread;

	int portNum;
	int clientIndex;


	public Server(int portNum, int clientIndex)
	{
		this.portNum = portNum;
		this.clientIndex = clientIndex;
		timeOutThread = new Timeout(this, 6000);
		comThread = new ComTimeout(this, 4000);
	}


	public void LerMensagem()
	{
		try {
			if (dataIn.available() > 0) {

				String line = dataIn.readUTF();
				System.out.println("Mensagem:\t" + line);

				// Comunicacao acontecendo:	COM:PID
				// Request de Election:		ELECTION:PID
				// Response de Election:	RESPONSE:PID
				// RESULTADO da Election:	RESULT:PID
				String resArray[] = line.split(":", 4);


				if (resArray[0].equals("COM")) {
					ComResponse(Integer.parseInt(resArray[1]));
				} else if(resArray[0].equals("ELECTION")) {
					SendElectionResponse(Integer.parseInt(resArray[1]));
				} else if (resArray[0].equals("RESPONSE")){
					ProcessaResponse(Integer.parseInt(resArray[1]));
				}
				else{
					ProcessaResultado(Integer.parseInt(resArray[1]));
				}

			}
		}
		catch(Exception e)
		{

		}

	}


	public void ComResponse(int PID){

		Node.clientObjList.get(PID).SendMessage("COM:" + Node.PID);
		comThread.Reset();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public void IniciaEleicao() {

		if(electionHappening)
			return;

		System.out.println("Eleicao INICIADA pelo processo: " + Node.PID);


		for(int i = Node.PID; i < Node.maxNodes; i++){
			if(i != Node.PID)
				Node.clientObjList.get(i).SendMessage("ELECTION:" + Node.PID);
		}

		if(timeOutThread.isAlive())
			timeOutThread.Resume();
		else
			timeOutThread.start();

		electionHappening = true;

	}

	public void SendElectionResponse(int PID)
	{
		if(PID != Node.PID) {
			System.out.println("Mandando response pro Processo " + PID);
			Node.clientObjList.get(PID).SendMessage("RESPONSE:" + Node.PID);
		}
	}

	// processa as mensagens de Response da ELECTION do processo atual
	public void ProcessaResponse(int PID)
	{
		if(PID > Node.PID){
			timeOutThread.Pause();
			return;
		}
	}

	public void ProcessaResultado(int PID){
		Node.coordinator = PID;
		System.out.println("Novo Coordenador: " + Node.coordinator);
		electionHappening = false;

		timeOutThread.Pause();

		if(Node.PID != Node.coordinator){
			comThread.Resume();
			ComResponse(Node.coordinator);
		}

	}

	synchronized public void NoResponse() {
		for(int i = 0; i < Node.maxNodes; i++){
			if(i != Node.PID)
				Node.clientObjList.get(i).SendMessage("RESULT:" + Node.PID);
		}

		ProcessaResultado(Node.PID);
	}
	
	public void run()
	{
		try
		{
			System.out.println("Server rodando na porta: " + portNum);
			server = new ServerSocket(portNum);
			Thread.sleep(1000);
			socket = server.accept();
			dataIn = new DataInputStream(socket.getInputStream());
			System.out.println("Conexao Aceita na porta: " + portNum);
			Thread.sleep(1000);
		}
		catch(Exception e) {
			System.out.println("Erro: " + e);
		}


		if(Node.PID != Node.coordinator && !commStarted){
			commStarted = true;
			Node.clientObjList.get(Node.coordinator).SendMessage("COM:" + Node.PID);

			comThread.start();
		}

		
		while(true)
		{
			try
			{
				LerMensagem();
				Thread.sleep(50);
			}
			catch(Exception e)
			{
				System.out.println("Erro: " + e);
				
			}
		}
	}
}
