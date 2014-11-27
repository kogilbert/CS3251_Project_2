import java.io.IOException;


public class SendFileThread extends Thread {
	RTP protocol;
	String filename;
	public SendFileThread(RTP protocol, String filename){
		this.protocol = protocol;
		this.filename = filename;
	}


	@Override
	public void run() {
		try {
			protocol.sendFile(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
