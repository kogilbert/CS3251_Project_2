import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Stack;

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
	RTPTimer timer;
	private int conFlag;
	private Stack<byte[]> packetBuffer;
	
	private boolean conSignal;
	private boolean dataSignal;
	private boolean ackSignal;
	
	BufferedOutputStream outBuffer;
	FileOutputStream fileOut;
	
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
		timer = new RTPTimer();
		conFlag = 0;
		packetBuffer = new Stack<byte[]>();
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);
	}

	
	/**
	 * Constructor for host
	 * @throws FileNotFoundException 
	 */
	public RTP(InetAddress serverAddress, int emuPort, int hostPort, int destPort) throws SocketException, FileNotFoundException {
		this.serverAddress = serverAddress;
		this.emuPort = emuPort;
		this.hostPort = hostPort;
		this.destPort = destPort;
		socket = new DatagramSocket(hostPort);
		header = new RTPHeader((short)hostPort, (short)destPort, 0, 0);
		window = new RTPWindow();
		timer = new RTPTimer();
		conFlag = 0;
		packetBuffer = new Stack<byte[]>();
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);
		fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + "recvFile.txt", true);
		outBuffer=  new BufferedOutputStream(fileOut);
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
	
	public boolean isConSignal() {
		return conSignal;
	}


	public void setConSignal(boolean conSignal) {
		this.conSignal = conSignal;
	}


	synchronized public boolean isDataSignal() {
		return dataSignal;
	}


	synchronized public void setDataSignal(boolean dataSignal) {
		this.dataSignal = dataSignal;
	}


	public boolean isAckSignal() {
		return ackSignal;
	}


	public void setAckSignal(boolean ackSignal) {
		this.ackSignal = ackSignal;
	}
	
//--------------------------------------------------Functions---------------------------------------------------------------//
	


	 public void connect() throws IOException{
		header.setCon(true);
		
		 /* Sending initial msg SYN = 1 */
		header.setSyn(true);
		header.setSeqNum(0);
		this.send(null);
		timer.start();
		System.out.println("Send first msg[SYN=1].");
		
		while(this.getConFlag() == 0){
			if(timer.checkTimeout()){
				header.setSyn(true);
				header.setSeqNum(0);
				this.send(null);
				System.out.println("Re-Send first msg[SYN=1].");
				timer.start();
			}
		}
		
		/* Sending second msg SYN = 0 */
		header.setSyn(false);
		header.setSeqNum(1);
		this.send(null);
		System.out.println("Received first SYN ack, sending second msg[SYN=0].");
		timer.start();
		
		while(this.getConFlag() == 1){
			if(timer.checkTimeout()){
				header.setSyn(false);
				header.setSeqNum(1);
				this.send(null);
				System.out.println("Re-Send first msg[SYN=0].");
				timer.start();
			} 
		}
		
		header.setCon(false);
		System.out.println("-------------------Connection established--------------------");
	}
	
	 public void close() throws IOException{
		header.setCon(true);
		
		 /* Sending initial msg FIN = 1 */
		header.setFin(true);
		header.setSeqNum(0);
		this.send(null);
		System.out.println("Send first msg[FIN=1].");
		timer.start();
		
		while(this.getConFlag() == 2) {
			if(timer.checkTimeout()){
				header.setFin(true);
				header.setSeqNum(0);
				this.send(null);
				System.out.println("Re-Send first msg[FIN=1].");
				timer.start();
			}
		}
		
		/* Sending second msg FIN = 0 */
		header.setFin(false);
		header.setSeqNum(1);
		this.send(null);
		System.out.println("Received first FIN ack, sending second msg[FIN=0].");
		timer.start();
		
		while(this.getConFlag() == 3){
			if(timer.checkTimeout()){
				header.setFin(false);
				header.setSeqNum(1);
				this.send(null);
				System.out.println("Re-Send first msg[FIN=0].");
				timer.start();
			} 
		}

		header.setCon(false);
		System.out.println("-------------------Connection closed--------------------");
		
	}

	/**
	 * conFlag : connection flag. 
	 * 0 : listening for connection 
	 * 1 : received first SYN = 1 packet.  
	 * 2 : connection established. 
	 * 3 : closing wait 
	 * @throws IOException
	 */
	 public void listen() throws IOException{
		 while(true){
			 socket.receive(recvPacket);
			 byte[] actualRecvData = null;
			 System.out.println("Received packet at "
					 + recvPacket.getAddress().getHostAddress() + " on port "
					 + recvPacket.getPort());
			 actualRecvData = new byte[recvPacket.getLength()];
			 System.arraycopy(recvPacket.getData(), 0, actualRecvData, 0, recvPacket.getLength());
			 RTPHeader tmp = this.getHeader(actualRecvData);
			 int seq = tmp.getSeqNum();
			 header.setAckNum(seq);
			 
			 if(tmp.isCon()){
				 recvConMsg(actualRecvData);
			 } else if(tmp.isDat()){
				 recvDataMsg(actualRecvData);
			 }
		 }
	}
	 
	/**
	 * Handle received connection message
	 * @param packet
	 * @throws IOException
	 */
	public void recvConMsg(byte[] packet) throws IOException{
		RTPHeader tmp = this.getHeader(packet);
		if(this.getConFlag() == 0) {
			if(tmp.isSyn()){
				header.setCon(true);
				this.sendAck();
				this.setConFlag(1);
			} 
			//Client Side
			else if(tmp.isAck()){
				header.setSyn(false);
				header.setSeqNum(1);
				this.send(null);
				System.out.println("Received first SYN ack, sending second msg[SYN=0].");
				this.setConFlag(1);
			}
			
			else if(!tmp.isFin()){
				header.setCon(true);
				this.sendAck();
				header.setCon(false);
			}
		} else if (this.getConFlag()  == 1){
			if(!tmp.isSyn() && !tmp.isAck()){
				this.setConFlag(2);
				this.sendAck();
				header.setCon(false);
				System.out.println("-------------------Connection established--------------------");
			}
			if(tmp.isSyn()){
				this.setConFlag(0);
			}
			// Client side
			if(tmp.isAck()) {
				this.setConFlag(2);
			}
		} else if (this.getConFlag() == 2) {
			if(tmp.isFin()){
				header.setCon(true);
				this.sendAck();
				this.setConFlag(1);
			} 
			//Client Side
			else if(tmp.isAck()){
				header.setFin(false);
				header.setSeqNum(1);
				this.send(null);
				System.out.println("Received first FIN ack, sending second msg[FIN=0].");
				this.setConFlag(3);
			}
			
			else if(!tmp.isSyn()){
				header.setCon(true);
				this.sendAck();
				header.setCon(false);
			}
			
		} else if (this.getConFlag()  == 3){
			if(!tmp.isFin() && !tmp.isAck()){
				this.setConFlag(0);
				this.sendAck();
				header.setCon(false);
				System.out.println("-------------------Connection closed--------------------");
			}
			if(tmp.isFin()){
				this.setConFlag(2);
			}
			// Client side
			if(tmp.isAck()) {
				this.setConFlag(0);
			}
		}
		
		recvPacketFlush();
	}
	
	/**
	 * Handle received data transmission message. 
	 * @param packet
	 * @throws IOException 
	 */
	synchronized public void recvDataMsg(byte[] packet) throws IOException{
		RTPHeader tmp = this.getHeader(packet);
		System.out.println("recvData.");
		if(tmp.isAck()){
			if(tmp.getAckNum() == window.getStartWindow()){
				timer.start();
				window.setStartWindow(window.getStartWindow()+1);
				window.setEndWindow(window.getEndWindow()+1);
				packetBuffer.pop();
			}
		} else {
			System.out.println("write data.");
			header.setDat(true);
			this.sendAck();
			if(outBuffer != null){
				byte[] payload = this.getContentByte(recvPacket.getData());
				outBuffer.write(payload, 0, payload.length);
				outBuffer.flush(); 
			} else {
				throw new IOException("outBuffer havent been initialized");
			}
			this.recvPacketFlush();
		}
	}
	
	 public void sendFile(FileInputStream fileIn) throws IOException{
		if(conFlag == 2) {
			byte[] buffer = new byte[RTP.BUFFERMAX - RTPHeader.headerLen];
			int payloadLen = fileIn.read(buffer);
			byte[] payload;
			timer.start();
			
			while( payloadLen != -1 || (window.getStartWindow() != window.getNextToSend()-1)){
				if(timer.checkTimeout()){
					window.setNextToSend(window.getStartWindow());
					timer.start();
					for(byte[] eachPacket: packetBuffer){
						int seq = window.getNextToSend();
						header.setSeqNum(seq);
						header.setDat(true);
						this.send(eachPacket);
						window.setNextToSend(seq+1);
					}
				}
				if(window.getNextToSend() <= window.getEndWindow()){
					payload =  new byte[payloadLen];
					System.arraycopy(buffer, 0, payload, 0, payloadLen);
					int seq = window.getNextToSend();
					header.setSeqNum(seq);
					header.setDat(true);
					this.send(payload);
					packetBuffer.push(payload);
					window.setNextToSend(seq+1);
					payloadLen = fileIn.read(buffer);
				}
			}
			fileIn.close();
		} else {
			System.out.println("Please initialize connection first.");
		}
	}
	
	
	 synchronized public void send(byte[] data) throws IOException {
		byte[] dataWithHeader = packData(header.getHeader(), data);
		header.setAck(false);
		System.out.println("Sending packet--" + "Seq Num:" + header.getSeqNum());
		sendPacket = new DatagramPacket(dataWithHeader, dataWithHeader.length,
					serverAddress, emuPort);
		 socket.send(sendPacket);
	}
	
	 synchronized public void sendAck() throws IOException {
		System.out.println("SendingAck--" + "ACK Num:" + header.getAckNum());
		 header.setAck(true);
		 sendPacket = new DatagramPacket(header.getHeader(), RTPHeader.headerLen,
				 serverAddress, emuPort);
		 socket.send(sendPacket);
	}
	
//	
//	 synchronized public byte[] receive() throws IOException{
//		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
//				BUFFERMAX);
//		socket.receive(recvPacket);
//		byte[] actualRecvData = null;
//		if(recvPacket.getData() != null){
//			System.out.println("Received packet at "
//					+ recvPacket.getAddress().getHostAddress() + " on port "
//					+ recvPacket.getPort());
//			actualRecvData = new byte[recvPacket.getLength()];
//			System.arraycopy(recvPacket.getData(), 0, actualRecvData, 0, recvPacket.getLength());
//			RTPHeader tmp = this.getHeader(actualRecvData);
//			int seq = tmp.getSeqNum();
//			header.setAckNum(seq);
//		}
//		
//		// Once the datagram socket receive data the buffer will be reset to
//		// the length of the data it received.
//		// So we need to reset the buffer size back to the Maximum size
//		recvPacket.setLength(BUFFERMAX);
//		return actualRecvData;
//	}
	
//	 public void recvFile(String receFileName) throws IOException{
//		if(conFlag == 2) {
//			FileOutputStream fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + receFileName, true);
//			BufferedOutputStream outBuffer=  new BufferedOutputStream(fileOut);
//			
//			while(true){
//				if(this.isDataSignal()){
//					this.sendAck();
//					if(outBuffer != null){
//						byte[] payload = this.getContentByte(recvPacket.getData());
//						outBuffer.write(payload, 0, payload.length);
//						outBuffer.flush(); 
//						System.out.println("Write recvPacket to buffer.");
//					} else {
//						throw new IOException("outBuffer havent been initialized");
//						
//					}
//					
//					this.recvPacketFlush();
//					this.setDataSignal(false);
//				}
//			}
//			
//		} else {
//			System.out.println("Please initialize connection first.");
//		}
//	}
	
	 synchronized public void recvPacketFlush(){
		 recvPacket = new DatagramPacket(new byte[BUFFERMAX],
					BUFFERMAX);
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
