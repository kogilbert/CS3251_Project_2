import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FTA {
	
	FileOutputStream fileOut;
	BufferedOutputStream outBuffer;
	FileInputStream fileIn;
	
	public FTA(){
		fileOut = null;
		outBuffer = null;
		fileIn = null;
	}
	
	public FTA(String receFileName) throws FileNotFoundException {
		super();
		this.fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + receFileName, true);
		outBuffer =  new BufferedOutputStream(fileOut);
	}


	public void receiveFile(byte[] data) throws IOException{
		if(outBuffer != null){
			outBuffer.write(data, 0, data.length);
			outBuffer.flush(); 
		} else {
			throw new IOException("outBuffer havent been initialized");
		}
		
	}
	
	
	public void sendFile(String filename, RTP rtpProtocol) throws IOException{
		
		fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
		
		byte[] buffer = new byte[RTP.BUFFERMAX - RTPHeader.headerLen];
		int payloadLen = fileIn.read(buffer);
		byte[] payload;
		while( payloadLen != -1){
			payload =  new byte[payloadLen];
			System.arraycopy(buffer, 0, payload, 0, payloadLen);
			rtpProtocol.send(payload);
			payloadLen = fileIn.read(buffer);
		}
		
		fileIn.close();
	}
	
	
}
