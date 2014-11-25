import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RTP {

	public static final int BUFFERMAX = 255;
	
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int emuPort;
	private int hostPort;
	private int destPort;
	private RTPHeader header;
	DatagramPacket sendPacket;
	DatagramPacket recvPacket;
	RTPWindow window;
	private int conFlag;
	
	/**
	 * Default Constructor
	 */
	public RTP(){
		this.serverAddress = null;
		this.emuPort = -1;
		this.hostPort = -1;
		this.setDestPort(-1);
		socket = null;
		header = null;
		sendPacket = null;
		recvPacket = null;
		window = new RTPWindow();
		conFlag = 0;
	}

	
	/**
	 * Constructor for host
	 */
	public RTP(InetAddress serverAddress, int emuPort, int hostPort, int destPort) throws SocketException {
		super();
		this.serverAddress = serverAddress;
		this.emuPort = emuPort;
		this.hostPort = hostPort;
		this.destPort = destPort;
		socket = new DatagramSocket(hostPort);
		header = new RTPHeader((short)hostPort, (short)destPort, 0, 0);
		window = new RTPWindow();
		conFlag = 0;
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

	public int getemuPort() {
		return emuPort;
	}

	public void setemuPort(int emuPort) {
		this.emuPort = emuPort;
	}

	public int getDestPort() {
		return destPort;
	}


	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}


	public int gethostPort() {
		return hostPort;
	}

	public void sethostPort(int hostPort) {
		this.hostPort = hostPort;
	}

	public RTPHeader getHeader() {
		return header;
	}

	public void setHeader(RTPHeader header) {
		this.header = header;
	}

	synchronized public int getConFlag() {
		return conFlag;
	}


	synchronized public void setConFlag(int conFlag) {
		this.conFlag = conFlag;
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
	synchronized public void connect() throws IOException{
		socket.setSoTimeout(1000);
		header.setSyn(true);
		header.setSeqNum(0);
		this.send(null);
		System.out.println("Send first msg[SYN=1].");
		RTPTimer timer = new RTPTimer();
		timer.start();
		
		while(conFlag != 2){
			try{
				byte[] recvData = this.receive();
				if(recvData != null){
					RTPHeader tmp = this.getHeader(recvData);
					if(conFlag == 0) {
						if(tmp.isAck()){
							header.setSyn(false);
							header.setSeqNum(1);
							this.send(null);
							timer.start();
							System.out.println("Received first SYN ack, sending second msg[SYN=0].");
							while(conFlag != 2){
								try {
									recvData = this.receive();
									if(recvData != null){
										tmp = this.getHeader(recvData);
										if(tmp.isAck() & tmp.getAckNum() == 1){
											this.setConFlag(2);
											System.out.println("-------------------Connection established--------------------");
										}
									}
								} catch(SocketTimeoutException e) {
									header.setSeqNum(1);
									System.out.println("Re-Send s msg[SYN=0].");
									this.send(null);
								}
							}
						}
					}
				}
			} catch(SocketTimeoutException e) {
				header.setSyn(true);
				this.send(null);
				System.out.println("Re-Send first msg[SYN=1].");
				timer.start();
			} 
		}
		
		socket.setSoTimeout(0);
	}
	
	synchronized public void close() throws IOException{
		header.setFin(true);
		header.setSeqNum(0);
		this.send(null);
		System.out.println("Send first msg[FIN=1].");
		while(true){
			try{
				byte[] recvData = this.receive();
				if(recvData != null){
					RTPHeader tmp = this.getHeader(recvData);
					if(conFlag == 2){
						if(tmp.isAck() & tmp.getAckNum() == 0){
							System.out.println("Received first ACK.");
							this.setConFlag(3);
						}
					} else if (conFlag == 3){
						if (tmp.isFin()){
							this.sendAck();
							System.out.println("Received msg[FIN=1], sending back ACK.");
							this.setConFlag(0);
							System.out.println("-------------------Connection closed--------------------");
						}
					}
				}
			}catch(SocketTimeoutException e){}
		}
	}

	/**
	 * conFlag : connection flag. 
	 * 0 : listening for connection 
	 * 1 : received first SYN = 1 packet.  
	 * 2 : connection established. 
	 * 3 : closing wait 
	 * @throws IOException
	 */
	synchronized public void listen() throws IOException{
		while(true){
			byte[] recvData = this.receive();
			if(recvData != null){
				RTPHeader tmp = this.getHeader(recvData);
				if(conFlag == 0) {
					if(tmp.isSyn()){
						this.sendAck();
						this.setConFlag(1);
					} 
				} else if (conFlag == 1){
					if(tmp.isSyn() == false){
						this.setConFlag(2);
						this.sendAck();
						System.out.println("-------------------Connection established--------------------");
					}
				} else if (conFlag == 2) {
					if(tmp.isFin()){
						this.sendAck();
						header.setFin(true);
						this.send(null);
						this.setConFlag(3);
					} 
				} else if (conFlag == 3){
					if(tmp.isAck()){
						this.setConFlag(0);
						System.out.println("-------------------Connection closed--------------------");
					}
				}
			}
		}
	}
	
	synchronized public void sendFile(FileInputStream fileIn) throws IOException{
		if(conFlag == 2) {
			byte[] buffer = new byte[RTP.BUFFERMAX - RTPHeader.headerLen];
			int payloadLen = fileIn.read(buffer);
			byte[] payload;
			RTPTimer timer = new RTPTimer();
			timer.start();
			
			while( payloadLen != -1){
				if(timer.checkTimeout()){
					window.setNextToSend(window.getStartWindow());
				}
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
				
				try{
					byte[] recvData = this.receive();
					RTPHeader tmp = this.getHeader(recvData);
					if(tmp.getAckNum() == window.getStartWindow()){
						timer.start();
						window.setStartWindow(window.getStartWindow()+1);
						window.setEndWindow(window.getEndWindow()+1);
					}
				}catch(SocketTimeoutException e){}
							
			}
			fileIn.close();
		} else {
			System.out.println("Please initialize connection first.");
		}
	}
	
	
	synchronized public void send(byte[] data) throws IOException {
		header.setAck(false);
		byte[] dataWithHeader = packData(header.getHeader(), data);
		System.out.println("Sending packet--" + "Seq Num:" + header.getSeqNum());
		sendPacket = new DatagramPacket(dataWithHeader, dataWithHeader.length,
				serverAddress, emuPort);
		socket.send(sendPacket);
	}
	
	synchronized public void sendAck() throws IOException {
		header.setAck(true);
		System.out.println("SendingAck--" + "ACK Num:" + header.getAckNum());
		sendPacket = new DatagramPacket(header.getHeader(), RTPHeader.headerLen,
				serverAddress, emuPort);
		socket.send(sendPacket);
	}
	
	
	synchronized public byte[] receive() throws IOException{
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);
		socket.receive(recvPacket);
		byte[] actualRecvData = null;
		if(recvPacket.getData() != null){
			System.out.println("Received packet at "
					+ recvPacket.getAddress().getHostAddress() + " on port "
					+ recvPacket.getPort());
			actualRecvData = new byte[recvPacket.getLength()];
			System.arraycopy(recvPacket.getData(), 0, actualRecvData, 0, recvPacket.getLength());
			RTPHeader tmp = this.getHeader(actualRecvData);
			int seq = tmp.getSeqNum();
			header.setAckNum(seq);
			//System.out.println("Recv ack :" + tmp.getAckNum());
		}
		
		
		// Once the datagram socket receive data the buffer will be reset to
		// the length of the data it received.
		// So we need to reset the buffer size back to the Maximum size
		recvPacket.setLength(BUFFERMAX);
		return actualRecvData;
	}
	
	synchronized public void recvFile(String receFileName) throws IOException{
		if(conFlag == 2) {
			FileOutputStream fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + receFileName, true);
			BufferedOutputStream outBuffer=  new BufferedOutputStream(fileOut);
			//keeping listening potential incoming packet
			while(true){
				try{
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
				}catch(SocketTimeoutException e) {}
			}
		}  else {
			System.out.println("Please initialize connection first.");
		}
	}
	
	/**
	 * Pack the header and data together into a single byte[] array 
	 * so that we can send the data to the UDP socket
	 * @param header header byte[]
	 * @param data data byte[]
	 * @return result the single byte[] array
	 */
	synchronized static byte[] packData(byte[] header, byte[] data){
		
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
	synchronized public RTPHeader getHeader(byte[] receiveData){
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
	synchronized public byte[] getContentByte(byte[] receiveData){
		int headerLen = receiveData[4];
		byte[] content = new byte[receiveData.length - headerLen];
		System.arraycopy(receiveData, headerLen, content, 0, receiveData.length - headerLen);
		return content;
	}
	
	/**
	 * Convert the ASCII byte[] data into String 
	 */
	synchronized public String byteArrayToString(byte[] data){
		  StringBuilder buffer = new StringBuilder(data.length);
		    for (int i = 0; i < data.length; ++ i) {
		        if (data[i] < 0) throw new IllegalArgumentException();
		        buffer.append((char) data[i]);
		    }
		    return buffer.toString();
	}
	
	
}
