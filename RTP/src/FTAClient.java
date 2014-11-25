import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/*
 * function Connect (Port num, Emulator IP, Emulator Port num) 
 * Get File(File Name) 
 * Post File (File Name)
 *  Window (w) 
 *  Debug On/Off 
 *  Disconnect
 */

public class FTAClient {

	//576 - 20 -8 -16 = 531
	
	public static void main(String[] args) throws IOException {
		
		int emuPort=0;
		int clientPort=0;
		int desPort=0;
		InetAddress serverAddress=null;
		RTP rtpProtocol=null;
		
		System.out.println("Welcome to the client, type 'connect(Emulator IP, Emulator Port Number, Client Port Number)");
		Scanner sc = new Scanner(System.in);
		boolean notConnected= true;
		while(notConnected){
			String input = sc.nextLine();
			System.out.println("you inputed " + input);
			if(input.contains("connect")){
				
				String emIP = input.substring(input.indexOf('(')+1, input.indexOf(','));
				System.out.println(emIP);
				serverAddress = InetAddress.getByName(emIP);
				input = input.substring(input.indexOf(',')+2);
				emuPort = Integer.parseInt(input.substring(0, input.indexOf(',')));
				System.out.println(emuPort);
				input = input.substring(input.indexOf(',')+2);
				clientPort = Integer.parseInt(input.substring(0, input.indexOf(')')));
				System.out.println(clientPort);
				desPort = clientPort+1;
				rtpProtocol = new RTP(serverAddress, emuPort, clientPort, desPort);
				rtpProtocol.connect();

				notConnected = false;
			}
		}


		System.out.println("type 'postfile(filename) or getfile(filename)");
		String input = sc.nextLine();
		System.out.println("you inputed " + input);
		
		if(input.contains("postfile")){
			String filename = input.substring(input.indexOf('(')+1, input.indexOf(')'));
			FileInputStream fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
			rtpProtocol.sendFile(fileIn);	
		} else if(input.contains("getfile")){
			//get file stuff
		}else if(input.contains("disconnect")){
			//rtpProtocol.close();
		}
		
		
		
		/**
		 * Set up user input parameters Port Number, Emulator IP, Emulator Port Number------------------------------
		 */
		/*
		// Test for correct # of args throw new
		// lllegalArgumentException("Parameter(s)' <Server> <Word> [<Port>]");
		if ((args.length < 3) || (args.length > 4)) {
			throw new IOException("Invalid Argument");
		}
*/
		/*
		int clientPort = Integer.parseInt(args[3]);

		// Server IP address
		InetAddress serverAddress = InetAddress.getByName(args[0]);

		// Server port num
		int emuPort = Integer.parseInt(args[1]);
		
		int desPort =clientPort+1;
		
		RTP rtpProtocol = new RTP(serverAddress, emuPort, clientPort, desPort);
		

		/**
		 * Start sending and receiving data------------------------------------------------------------------------
		 */
		/*
		rtpProtocol.connect();
		
		String filename = args[2];
		
		FileInputStream fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
		
		rtpProtocol.sendFile(fileIn);
		
		//rtpProtocol.close();
*/
	}

}
