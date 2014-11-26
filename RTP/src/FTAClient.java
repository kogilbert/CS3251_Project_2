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
		RTP rtpProtocol=null;
		Scanner sc = new Scanner(System.in);
		boolean connected= false;
		
		
		// lllegalArgumentException("Parameter(s)' <Server> <Word> [<Port>]");
		if ((args.length < 3) || (args.length > 4)) {
			throw new IOException("Invalid Argument");
		}
		
		//client port
		int clientPort = Integer.parseInt(args[0]);
		// Server IP address
		InetAddress serverAddress = InetAddress.getByName(args[1]);
		// Emu port num
		int emuPort = Integer.parseInt(args[2]);
		//Dest port
		int desPort =clientPort+1;
		
		Thread clientProtocol = null;
		
		while(true){
			while(!connected){
				System.out.println("Welcome to the client, type connect");
				String input = sc.nextLine();
				System.out.println("you inputed " + input);
				if(input.contains("connect")){
					rtpProtocol = new RTP(serverAddress, emuPort, clientPort, desPort);
					clientProtocol = new DataReceiveThread(rtpProtocol);
					clientProtocol.start();
					rtpProtocol.connect();
					connected = true;
				}
			}
	
			while(connected){
				System.out.println("type post filename, get filename, Window W, or disconnect");
				String input = sc.nextLine();
				System.out.println("you inputed " + input);
				
				if(input.contains("post")){
					String filename = input.substring(input.indexOf('t')+2);
					//FileInputStream fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
					rtpProtocol.sendFile(filename);	
				} else if(input.contains("get")){
					String filename = input.substring(input.indexOf('t')+2);
					//get file stuff
	
				}else if(input.contains("window")){
					//change window
					
				}else if(input.contains("disconnect")){
					rtpProtocol.close();
					clientProtocol.stop();
					rtpProtocol.getSocket().close();
					connected = false;
				}
			}
		}
	}
}
