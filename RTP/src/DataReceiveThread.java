import java.io.IOException;


public class DataReceiveThread extends Thread{
	RTP protocol;
	
	public DataReceiveThread(RTP protocol){
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
