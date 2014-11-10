import java.io.*;
import java.net.*;

public class RTP {

	static DatagramSocket start(int port) throws SocketException{
		return new DatagramSocket(port);
	}
	
	void connect(){
		
	}
	
	void send(){
		
	}
	
	void receive(){
		
	}
	
	/**
	 * Pack the header and data together into a single byte[] array 
	 * so that we can send the data to the UDP socket
	 * @param header header byte[]
	 * @param data data byte[]
	 * @return result the single byte[] array
	 */
	static byte[] packData(byte[] header, byte[] data){
		int headerLen = header.length;
		int dataLen = data.length;
		int totalLen = headerLen + dataLen;
		byte[] result = new byte[totalLen];
		
		for(int i =0; i < headerLen; i++){
			result[i] = header[i];
		}
		int j = 0;
		for(int i = headerLen; i < totalLen; i++){
			result[i] = data[j];
			j++;
		}
		return result;
	}
	
	/**
	 * Extract the header information from received RTP packet
	 */
	static RTPHeader getHeader(byte[] receiveData){
		RTPHeader header = new RTPHeader();
		int headerLen = receiveData[4];
		byte[] headerArray = new byte[headerLen];
		System.arraycopy(receiveData, 0, headerArray, 0, headerLen);
		header.headerFromArray(headerArray);
		
		return header;
	}
	
	/**
	 * Extract the data information from received RTP packet
	 */
	static byte[] getContentByte(byte[] receiveData, int len){
		int headerLen = receiveData[4];
		byte[] content = new byte[len - headerLen];
		System.arraycopy(receiveData, headerLen, content, 0, len - headerLen);
		return content;
	}
	
	/**
	 * Convert the ASCII byte[] data into String 
	 */
	static String byteArrayToString(byte[] data){
		  StringBuilder buffer = new StringBuilder(data.length);
		    for (int i = 0; i < data.length; ++ i) {
		        if (data[i] < 0) throw new IllegalArgumentException();
		        buffer.append((char) data[i]);
		    }
		    return buffer.toString();
	}
	
	
}
