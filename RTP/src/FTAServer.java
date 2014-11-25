import java.io.IOException;
import java.net.InetAddress;

/* functions
Start (Port num)
Window (w)
Debug On/Off
*/

public class FTAServer {


	public static void main(String[] args) throws IOException{
//		System.out.println("Welcome to the server, type 'connect(Port Number)");
//		Scanner sc = new Scanner(System.in);
//		String input = sc.nextLine();
//		System.out.println("you inputed " + input);
//		
//		int startIndex = input.indexOf('(');
//		int endIndex = input.indexOf(')');
//		String port = input.substring((startIndex + 1), endIndex);
//		System.out.println(port);
	
		/**
		 * Set up user input parameters Server Port Number, Emulator IP, Emulator Port Number--------------------------
		 */
		
		// Test for correct # of args throw new
		// lllegalArgumentException("Parameter(s)' <Server> <Word> [<Port>]");
		if (args.length != 3) {
			throw new IOException("Invalid Argument");
		}

		int hostPort = Integer.parseInt(args[2]);

		// Server IP address
		InetAddress serverAddress = InetAddress.getByName(args[0]);

		// Server port num
		int emuPort = Integer.parseInt(args[1]);
		
		int desPort =hostPort-1;
		
		RTP rtpProtocol = new RTP(serverAddress, emuPort, hostPort, desPort);
		
		
		/**
		 * Start sending and receiving data------------------------------------------------------------------------
		 */

		rtpProtocol.listen();
		
		while(true){
			if(rtpProtocol.conFlag == 2){
				String receFileName = "recvFile.txt";
				rtpProtocol.recvFile(receFileName);
			}

		}

		
//		@SuppressWarnings("resource")
//		FileOutputStream fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + receFileName, true);
//		BufferedOutputStream outBuffer=  new BufferedOutputStream(fileOut);
//		
//		//keeping listening potential incoming packet
//		while(true){
//			byte[] receiveData = rtpProtocol.receive();
//			
//			if(receiveData != null){
//				rtpProtocol.sendAck();
//				byte[] payload = rtpProtocol.getContentByte(receiveData);
//				if(outBuffer != null){
//					outBuffer.write(payload, 0, payload.length);
//					outBuffer.flush(); 
//				} else {
//					throw new IOException("outBuffer havent been initialized");
//					
//				}
//			}
//		}
		
		
	}

}
