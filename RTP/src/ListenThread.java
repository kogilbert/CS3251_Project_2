import java.io.IOException;


public class ListenThread extends Thread{
	RTP protocol;
	
	public ListenThread(RTP protocol){
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
