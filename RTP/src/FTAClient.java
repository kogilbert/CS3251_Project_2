import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

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
		System.out.println("Welcome to the client, type 'connect(Port Number, Emulator IP, Emulator Port Number)");
		//Scanner sc = new Scanner(System.in);
		//String input = sc.nextLine();
		//System.out.println("you inputed " + input);
		
		/**
		 * Set up user input parameters Port Number, Emulator IP, Emulator Port Number------------------------------
		 */
		
		// Test for correct # of args throw new
		// lllegalArgumentException("Parameter(s)' <Server> <Word> [<Port>]");
		if ((args.length < 3) || (args.length > 4)) {
			throw new IOException("Invalid Argument");
		}

		int clientPort = Integer.parseInt(args[3]);

		// Server IP address
		InetAddress serverAddress = InetAddress.getByName(args[0]);

		// Server port num
		int servPort = Integer.parseInt(args[1]);
		
		RTP rtpProtocol = new RTP(serverAddress, servPort, clientPort);
		

		/**
		 * Start sending and receiving data------------------------------------------------------------------------
		 */
		
		String filename = args[2];
		
		FileInputStream fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
		
		rtpProtocol.sendFile(fileIn);
				

	}

}
