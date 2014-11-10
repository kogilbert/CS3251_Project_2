import java.io.*;
import java.net.*;
import java.util.*;

public class FTAServer {

	private static final int BUFFERMAX = 255;

	public static void main(String[] args) throws IOException{
		int servPort;
//		System.out.println("Welcome to the server, type 'connect(Port Number)");
//		Scanner sc = new Scanner(System.in);
//		String input = sc.nextLine();
//		System.out.println("you inputed " + input);
//		
//		int startIndex = input.indexOf('(');
//		int endIndex = input.indexOf(')');
//		String port = input.substring((startIndex + 1), endIndex);
//		System.out.println(port);
	
		servPort = Integer.parseInt(args[0]);
		System.out.println("Server has been set up with port num: " + servPort);
		/* functions
		Start (Port num)
		Window (w)
		Debug On/Off
		*/
		
		
		DatagramSocket socket = RTP.start(servPort);

		
		
		
		

		//int servPort = Integer.parseInt(args[0]);

		//DatagramSocket socket = new DatagramSocket(servPort);
		DatagramPacket packet = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);

		// keeping running
		while (true) {
			socket.receive(packet);
			System.out.println("Handling client at "
					+ packet.getAddress().getHostAddress() + " on port "
					+ packet.getPort());
			
			RTPHeader header = new RTPHeader();
		
			header.headerFromArray(packet.getData());
			
			System.out.println("Soure port: " + header.getSourcePort() + "\nDest Port: " + header.getDestPort() + "\nSYN : " + header.isSyn());
			socket.send(packet);

			// Once the datagram socket receive data the buffer will be reset to
			// the length of the data it received.
			// So we need to reset the buffer size back to the Maximum size
			packet.setLength(BUFFERMAX);
		}

	}

}
