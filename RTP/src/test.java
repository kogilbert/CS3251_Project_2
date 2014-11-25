import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class test {

	private static final int TIMEOUT = 3000;
	private static final int MAXTRIES = 5;
	
	public static void main(String[] args) throws IOException {
		System.out.println("beginning of program\n");
		
		if ((args.length < 2) || (args.length > 3)) // Test for correct # of args
		throw new IllegalArgumentException("Parameter(s)' <Server> <Word> [<Port>]");
		
		InetAddress serverAddress = InetAddress.getByName(args[0]); // Server address
		
		// Convert the argument String to bytes using the default encoding
		byte[] bytesToSend = args[1].getBytes();
		
		int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;
		
		DatagramSocket socket = new DatagramSocket() ;
		
		socket.setSoTimeout(TIMEOUT); // Maximum receive blocking time (milliseconds)
		
		// Sending packet
		DatagramPacket sendPacket = new DatagramPacket(bytesToSend,	bytesToSend.length, serverAddress, servPort);
		
		DatagramPacket receivePacket = // Receiving packet
				
		new DatagramPacket(new byte[bytesToSend.length], bytesToSend.length);
		
		int tries = 0; // Packets may be lost, so we have to keep trying
		boolean receivedResponse = false;
		do {
			socket.send(sendPacket); // Send the echo string
		try {
			socket.receive(receivePacket); // Attempt echo reply reception
			
			if (!receivePacket.getAddress().equals(serverAddress)) // Check source
				throw new IOException("Received packet from an unknown source");
				receivedResponse = true;
				
		} catch (InterruptedIOException e) { // We did not get anything
			tries += 1;
			System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries...") ;
			}
			} while ((!receivedResponse) && (tries < MAXTRIES)) ;
		
			if (receivedResponse)
				System.out.println("Received: " + new String(receivePacket.getData()));
			else
				System. out.println("No response -- giving up.") ;
			socket.close();
		

	}

}
