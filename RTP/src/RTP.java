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
	}
	
	/**
	 * Constructor for server
	 * @throws SocketException 
	 */
	public RTP(int servPort) throws SocketException{
		this.clientPort = servPort;
		socket = new DatagramSocket(servPort);
	}
	
	/**
	 * Constructor for server
	 */
	public RTP(InetAddress serverAddress, int servPort, int clientPort) throws SocketException {
		super();
		this.serverAddress = serverAddress;
		this.servPort = servPort;
		this.clientPort = clientPort;
		socket = new DatagramSocket(clientPort);
		header = new RTPHeader((short)clientPort, (short)(clientPort+1), 0, 0);
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
	public void connect(){
		
	}
	
	public void send(byte[] data) throws IOException {
		
		byte[] dataWithHeader = packData(header.getHeader(), data);
		
		sendPacket = new DatagramPacket(dataWithHeader, dataWithHeader.length,
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
		
		if(recvPacket.getData() != null){
			byte[] actualRecvData = new byte[recvPacket.getLength()];
			System.arraycopy(recvPacket.getData(), 0, actualRecvData, 0, recvPacket.getLength());
			return actualRecvData;
		}
		
		
		// Once the datagram socket receive data the buffer will be reset to
		// the length of the data it received.
		// So we need to reset the buffer size back to the Maximum size
		recvPacket.setLength(BUFFERMAX);
		return null;
		
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
