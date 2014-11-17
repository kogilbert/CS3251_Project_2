import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RTP {

	public static final int BUFFERMAX = 255;
	
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int servPort;
	private int clientPort;
	private RTPHeader header;
	DatagramPacket sendPacket;
	DatagramPacket recvPacket;
	RTPWindow window;
	
	/**
	 * Default Constructor
	 */
	public RTP(){
		this.serverAddress = null;
		this.servPort = -1;
		this.clientPort = -1;
		socket = null;
		header = null;
		sendPacket = null;
		recvPacket = null;
		window = new RTPWindow();
	}
	
	/**
	 * Constructor for server
	 * @throws SocketException 
	 */
	public RTP(int servPort) throws SocketException{
		this.clientPort = servPort;
		socket = new DatagramSocket(servPort);
		header = new RTPHeader((short)clientPort, (short)(clientPort+1), 0, 0);
		window = new RTPWindow();
	}
	
	/**
	 * Constructor for client
	 */
	public RTP(InetAddress serverAddress, int servPort, int clientPort) throws SocketException {
		super();
		this.serverAddress = serverAddress;
		this.servPort = servPort;
		this.clientPort = clientPort;
		socket = new DatagramSocket(clientPort);
		header = new RTPHeader((short)clientPort, (short)(clientPort+1), 0, 0);
		window = new RTPWindow();
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public InetAddress getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServPort() {
		return servPort;
	}

	public void setServPort(int servPort) {
		this.servPort = servPort;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public RTPHeader getHeader() {
		return header;
	}

	public void setHeader(RTPHeader header) {
		this.header = header;
	}

	
	
//--------------------------------------------------Functions---------------------------------------------------------------//
	
	
///**
// * Open the socket on the server side for listening.
// * @param port, server port number
// * @return
// * @throws SocketException
// */
//
//	static DatagramSocket start(int port) throws SocketException{
//		return new DatagramSocket(port);
//	}
//	
	public void connect() throws IOException{
		header.setSyn(true);
		this.send(null);
		byte[] recvData = this.receive();
		header = this.getHeader(recvData);
		if(header.getAckNum() == 0){
			header.setSyn(false);
		}
		
	}
	
	public void listen() throws IOException{
		while(true){
			byte[] recvData = this.receive();
			if(recvData != null){
				header = this.getHeader(recvData);
				if(header.isSyn()){
					this.sendAck();
				} 
			}
		}
	}
	
	public void sendFile(FileInputStream fileIn) throws IOException{
		byte[] buffer = new byte[RTP.BUFFERMAX - RTPHeader.headerLen];
		int payloadLen = fileIn.read(buffer);
		byte[] payload;
		
		while( payloadLen != -1){
			if(window.getNextToSend() <= window.getEndWindow()){
				payload =  new byte[payloadLen];
				System.arraycopy(buffer, 0, payload, 0, payloadLen);
				int seq = window.getNextToSend();
				header.setSeqNum(seq);
				this.send(payload);
				seq++;
				window.setNextToSend(seq);
				payloadLen = fileIn.read(buffer);
			}
			
			byte[] recvData = this.receive();
			RTPHeader tmp = this.getHeader(recvData);
			if(tmp.getAckNum() == window.getStartWindow()){
				window.setStartWindow(window.getStartWindow()+1);
				window.setEndWindow(window.getEndWindow()+1);
			}
			
		}
		
		fileIn.close();
	}
	
	
	public void send(byte[] data) throws IOException {
		header.setAck(false);
		byte[] dataWithHeader = packData(header.getHeader(), data);
		
		sendPacket = new DatagramPacket(dataWithHeader, dataWithHeader.length,
				serverAddress, servPort);
		socket.send(sendPacket);

	}
	
	public void sendAck() throws IOException {
		header.setAck(true);
		sendPacket = new DatagramPacket(header.getHeader(), RTPHeader.headerLen,
				serverAddress, servPort);
		socket.send(sendPacket);
	}
	
	
	public byte[] receive() throws IOException{
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);
		socket.receive(recvPacket);
		System.out.println("Received packet from client at "
				+ recvPacket.getAddress().getHostAddress() + " on port "
				+ recvPacket.getPort());
		
		byte[] actualRecvData = null;
		if(recvPacket.getData() != null){
			actualRecvData = new byte[recvPacket.getLength()];
			System.arraycopy(recvPacket.getData(), 0, actualRecvData, 0, recvPacket.getLength());
			RTPHeader tmp = this.getHeader(actualRecvData);
			int seq = tmp.getSeqNum();
			header.setAckNum(seq);
			System.out.println("Recv ack :" + tmp.getAckNum());
		}
		
		
		// Once the datagram socket receive data the buffer will be reset to
		// the length of the data it received.
		// So we need to reset the buffer size back to the Maximum size
		recvPacket.setLength(BUFFERMAX);
		return actualRecvData;
	}
	
	public void recvFile(String receFileName) throws IOException{
		
		FileOutputStream fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + receFileName, true);
		BufferedOutputStream outBuffer=  new BufferedOutputStream(fileOut);
		
		//keeping listening potential incoming packet
		while(true){
			byte[] receiveData = this.receive();
			
			if(receiveData != null){
				this.sendAck();
				byte[] payload = this.getContentByte(receiveData);
				if(outBuffer != null){
					outBuffer.write(payload, 0, payload.length);
					outBuffer.flush(); 
				} else {
					throw new IOException("outBuffer havent been initialized");
					
				}
			}
		}
	}
	
	/**
	 * Pack the header and data together into a single byte[] array 
	 * so that we can send the data to the UDP socket
	 * @param header header byte[]
	 * @param data data byte[]
	 * @return result the single byte[] array
	 */
	static byte[] packData(byte[] header, byte[] data){
		
		if(data != null){
			int headerLen = header.length;
			int totalLen = headerLen + data.length;
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
		} else {
			return header;
		}

	}
	
	/**
	 * Extract the header information from received RTP packet
	 */
	public RTPHeader getHeader(byte[] receiveData){
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
	public byte[] getContentByte(byte[] receiveData){
		int headerLen = receiveData[4];
		byte[] content = new byte[receiveData.length - headerLen];
		System.arraycopy(receiveData, headerLen, content, 0, receiveData.length - headerLen);
		return content;
	}
	
	/**
	 * Convert the ASCII byte[] data into String 
	 */
	public String byteArrayToString(byte[] data){
		  StringBuilder buffer = new StringBuilder(data.length);
		    for (int i = 0; i < data.length; ++ i) {
		        if (data[i] < 0) throw new IllegalArgumentException();
		        buffer.append((char) data[i]);
		    }
		    return buffer.toString();
	}
	
	
}
