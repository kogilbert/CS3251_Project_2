import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTAClient {

	//private static final int TIMEOUT = 3000;
	//private static final int MAXTRIES = 5;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		System.out.println("Welcome to the client, type 'connect(Port Number, Emulator IP, Emulator Port Number)");
		//Scanner sc = new Scanner(System.in);
		//String input = sc.nextLine();
		//System.out.println("you inputed " + input);

		/*
		 * function Connect (Port num, Emulator IP, Emulator Port num) 
		 * Get File(File Name) 
		 * Post File (File Name)
		 *  Window (w) 
		 *  Debug On/Off 
		 *  Disconnect
		 */
		
		
		/**
		 * Set up user input parameters Port Number, Emulator IP, Emulator Port Number------------------
		 */
		
		// Test for correct # of args throw new
		// lllegalArgumentException("Parameter(s)' <Server> <Word> [<Port>]");
		if ((args.length < 3) || (args.length > 4)) {
			throw new IOException("Invalid Argument");
		}

		int clientPort = Integer.parseInt(args[3]);
		DatagramSocket socket = new DatagramSocket(clientPort);

		// Server IP address
		InetAddress serverAddress = InetAddress.getByName(args[0]);

		// Server port num
		int servPort = Integer.parseInt(args[1]);
		
		RTPHeader myHeader = new RTPHeader((short)clientPort, (short)(clientPort+1), 0, 0);
		myHeader.setSyn(true);
		System.out.println("Source port: "+ myHeader.getSourcePort());
		
		byte[] header = myHeader.getHeader();
		
		byte[] data = args[2].getBytes();

		/**
		 * Start sending and receiving data------------------------------------------------------------------------
		 */
		
		// data to send
		byte[] bytesToSend = RTP.packData(header, data);

		// length of data
		int dataLen = bytesToSend.length;

		// sending packet
		DatagramPacket sendPacket = new DatagramPacket(bytesToSend, dataLen,
				serverAddress, servPort);

		// receive packet
		DatagramPacket receivePacket = new DatagramPacket(new byte[dataLen],
				dataLen);

		socket.send(sendPacket);

		socket.receive(receivePacket);

		System.out.println("Received: " + new String(receivePacket.getData()));

	}

}
