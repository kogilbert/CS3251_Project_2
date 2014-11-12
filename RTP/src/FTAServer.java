import java.io.IOException;

/* functions
Start (Port num)
Window (w)
Debug On/Off
*/

public class FTAServer {


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
	
		/**
		 * Set up user input parameters Server Port Number-------------------------------------------------------
		 */
		
		servPort = Integer.parseInt(args[0]);
		RTP rtpProtocol = new RTP(servPort);
		System.out.println("Server has been set up with port num: " + servPort);
		
		
		/**
		 * Start sending and receiving data------------------------------------------------------------------------
		 */
		

		FTA fileTrasimitor = new FTA("recvFile.txt");
		
		//keeping listening potential incoming packet
		while(true){
			byte[] receiveData = rtpProtocol.receive();
			
			if(receiveData != null){
				byte[] payload = rtpProtocol.getContentByte(receiveData);
				fileTrasimitor.receiveFile(payload);
			}
		
		}
		

	}

}
