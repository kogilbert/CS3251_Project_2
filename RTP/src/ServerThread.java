import java.io.IOException;


public class ServerThread extends Thread{
	RTP protocol;
	
	public ServerThread(RTP protocol){
		this.protocol = protocol;
	}


	@Override
	public void run() {
		try {
			protocol.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
