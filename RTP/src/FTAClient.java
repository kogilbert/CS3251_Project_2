import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/*
 * Functions:
 * 
 * Connect
 * Get File(File Name) 
 * Post File (File Name)
 * Window (w) 
 * Disconnect
 */

public class FTAClient {

	public static void main(String[] args) throws IOException {
		RTP rtpProtocol=null;
		Scanner sc = new Scanner(System.in);
		
		
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
		Thread sendThread = null;
		while(true){
				System.out.println("Type connect, post filename, get filename, Window W, or disconnect");
				String input = sc.nextLine();
//				System.out.println("you inputed " + input);
				
				if(input.equals("connect")){
					rtpProtocol = new RTP(serverAddress, emuPort, clientPort, desPort);
					clientProtocol = new DataReceiveThread(rtpProtocol);
					clientProtocol.start();
					rtpProtocol.connect();
				} else if(input.contains("post")){
					String[] inputstring = input.split("\\s");
					//FileInputStream fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
					sendThread = new SendFileThread(rtpProtocol,inputstring[1]);
					sendThread.start();
				} else if(input.contains("get")){
					String[] inputstring = input.split("\\s");
					//String filename = input.substring(input.indexOf('t')+2);
					rtpProtocol.getFile(inputstring[1]);	
	
				} else if(input.contains("window")){
					//change window
					String[] inputstring = input.split("\\s");
					int wsize = Integer.parseInt(inputstring[1]);
					rtpProtocol.changeWinSize(wsize);
					
				} else if(input.equals("disconnect")){
					rtpProtocol.close();
					clientProtocol.stop();
					if(sendThread != null){
						sendThread.stop();
					}
					rtpProtocol.getSocket().close();
				}
			}
		}
}
