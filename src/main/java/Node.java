import java.util.*;


public class Node {

	static public int PID;
	static public int maxNodes = 5;
	static public int coordinator;
	static public int numPendingRes;
	static public int pontoDeFalha;

	int[] ports = { 3425, 3435, 3445, 3455, 3465 };

	static public List<Client> clientObjList = new ArrayList<Client>();
	static public List<Server> serverObjList = new ArrayList<Server>();
	static public List<Thread> serverThreadList = new ArrayList<Thread>();


	public Node(int id, int coordinator) {
		Node.PID = id;
		Node.coordinator = coordinator;
		Node.pontoDeFalha = 1; // para simular a falha do processo
		Node.numPendingRes = maxNodes - PID - 1;
	}

	public Node()
	{

	}
	
	

	public void MainFunction()
	{

		try
		{

			System.out.println("Processo " + Node.PID);

			// for para criar servidores
			for(int i = 0; i < Node.maxNodes; i++)
			{

				if(i == Node.PID)
				{
					Node.serverObjList.add( i, new Server(0, i));
					Thread t1 = new Thread(Node.serverObjList.get(i));
					Node.serverThreadList.add(i, t1);
				}
				else
				{
					Node.serverObjList.add( i, new Server(ports[i] + Node.PID, i));
					Thread t1 = new Thread(Node.serverObjList.get(i));
					Node.serverThreadList.add( i, t1);
					t1.start();
				}

			}
		
			Thread.sleep(2000);
		
			// for para criar clientes
			for(int i = 0; i < Node.maxNodes; i++)
			{
				if(i == Node.PID)
				{
					for(int j = 0; j < Node.maxNodes; j++)
					{
						if(j == Node.PID)
						{
							clientObjList.add( j, new Client(ports[i] + j));
						}
						else
						{
							clientObjList.add( j, new Client(ports[i] + j));
							clientObjList.get(j).connect(ports[i] + j);
						}

					}

				}

			}

			Thread.sleep(2000);

			int rec = 0;
			Scanner scanner;

			while(rec != -1)
			{

				scanner = new Scanner(System.in);
				rec = Integer.parseInt(scanner.nextLine());

				if(rec == 0){
					System.out.println("Coordenador atual: " + Node.coordinator);
				}

			}


			for(int i = 0; i < Node.maxNodes; i++)
			{
				if(i != Node.PID)
				{
					serverObjList.get(i).server.close();
				}
			}

			System.out.println("Teste Finalizado");
			System.in.read();
			System.exit(0);
		    
		}

		catch(Exception e)
		{
			System.out.println("Erro: " + e);
		}
		
	}

}
