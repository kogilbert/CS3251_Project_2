import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

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
	LinkedQueue<byte[]> queueBuffer;
	private int recvFileIndex;
	private int getFlag;
	private int postFlag;
	private RTPTimer transTimer;
	private ArrayList<SendFileThread> threadList;
	
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
		getFlag = 0;
		postFlag = 0;
		queueBuffer = new LinkedQueue<byte[]>();
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);
		transTimer = new RTPTimer();
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
		getFlag = 0;
		postFlag = 0;
		queueBuffer = new LinkedQueue<byte[]>();
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);
		transTimer = new RTPTimer();
		threadList = new ArrayList<SendFileThread>();
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

	synchronized public RTPHeader getHeader() {
		return header;
	}

	synchronized public void setHeader(RTPHeader header) {
		this.header = header;
	}

	synchronized public int getConFlag() {
		return conFlag;
	}


	synchronized public void setConFlag(int conFlag) {
		this.conFlag = conFlag;
	}
	
	synchronized public int getGetFlag() {
		return getFlag;
	}


	synchronized public void setGetFlag(int getFlag) {
		this.getFlag = getFlag;
	}


	synchronized public int getPostFlag() {
		return postFlag;
	}


	synchronized public void setPostFlag(int postFlag) {
		this.postFlag = postFlag;
	}
	
	public ArrayList<SendFileThread> getThreadList() {
		return threadList;
	}


	public void setThreadList(ArrayList<SendFileThread> threadList) {
		this.threadList = threadList;
	}
//--------------------------------------------------Functions---------------------------------------------------------------//
	
	



	public void initialize() {
		window = new RTPWindow();
		timer = new RTPTimer();
		conFlag = 0;
		getFlag = 0;
		postFlag = 0;
		queueBuffer = new LinkedQueue<byte[]>();
		recvPacket = new DatagramPacket(new byte[BUFFERMAX],
				BUFFERMAX);	
		recvFileIndex = 0;
	}
	
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
		this.initialize();
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
			 actualRecvData = new byte[recvPacket.getLength()];
			 System.arraycopy(recvPacket.getData(), 0, actualRecvData, 0, recvPacket.getLength());
			 if(!this.checkChecksum(actualRecvData)){
				//System.out.println("corrupted data"); 
			 } else {
				 RTPHeader tmp = this.getHeader(actualRecvData);
				 if(tmp.isCon()){
					 recvConMsg(actualRecvData);
				 } else if(tmp.isGet()){
					 recvGetMsg(actualRecvData);
				 } else if(tmp.isPost()){
					 recvPostMsg(actualRecvData);
				 } else if(tmp.isDat()){
					 recvDataMsg(actualRecvData);
				 } 
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
		int seq = tmp.getSeqNum();
		header.setAckNum(seq);
		if(this.getConFlag() == 0) {
			if(tmp.isSyn()){
				//System.out.println("Received connection initializing msg [SYN=1]");
				header.setCon(true);
				this.sendAck();
				this.setConFlag(1);
			} 
			//Client Side
			else if(tmp.isAck()  && header.isSyn()){
				header.setSyn(false);
				header.setSeqNum(1);
				this.send(null);
				//System.out.println("Received first SYN ack, sending second msg[SYN=0].");
				this.setConFlag(1);
			}
			
			else if(!tmp.isFin() && !tmp.isAck()){
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
				System.out.flush();
			}
			if(tmp.isSyn() && tmp.getSeqNum() == 0){
				header.setCon(true);
				this.sendAck();
				header.setCon(false);
			}
			// Client side
			if(tmp.isAck()) {
				this.setConFlag(2);
			}
		} else if (this.getConFlag() == 2) {
			if(tmp.isFin()){
				//System.out.println("Received connection closing msg [FIN=1]");
				header.setCon(true);
				this.sendAck();
				this.setConFlag(3);
			} 
			//Client Side
			else if(tmp.isAck() && header.isFin()){
				header.setFin(false);
				header.setSeqNum(1);
				this.send(null);
				//System.out.println("Received first FIN ack, sending second msg[FIN=0].");
				this.setConFlag(3);
			}
			
			else if(!tmp.isSyn() && !tmp.isAck()){
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
				this.initialize();
			}
			else if(tmp.isFin() && tmp.getSeqNum() == 0){
				header.setCon(true);
				this.sendAck();
				header.setCon(false);
			}
			// Client side
			else if(tmp.isAck()) {
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
		if(tmp.isAck()){
			//System.out.println("Received Data ACK Packet --- ACK Num:" + tmp.getAckNum());
			if(tmp.getAckNum() == window.getStartWindow()){
				timer.start();
				window.setStartWindow(window.getStartWindow()+1);
				window.setEndWindow(window.getEndWindow()+1);
				queueBuffer.dequeue();
			}
		} else {
			if(outBuffer != null){
				//System.out.println("Received Data Packet --- Seq Num: " + tmp.getSeqNum());
				if(recvFileIndex == tmp.getSeqNum()){
					byte[] payload = this.getContentByte(packet);
					outBuffer.write(payload, 0, payload.length);
					outBuffer.flush();
					recvFileIndex++;
					if(tmp.isLst()){
						fileOut.close();
						System.out.println("-------File is succesfully received.-------" );
					}
				}
				 
				 int seq = tmp.getSeqNum();
				 if(recvFileIndex > seq){
					 header.setAckNum(seq);
				 } else if(seq > recvFileIndex){
					 header.setAckNum(recvFileIndex-1);
				 }
				 header.setDat(true);
				 this.sendAck();
				 header.setDat(false);
			} else {
				throw new IOException("outBuffer havent been initialized");
			}
		}
		this.recvPacketFlush();
	}
	
	/**
	 * Handle get file message
	 * @param packet
	 * @throws IOException
	 */
	public void recvGetMsg(byte[] packet) throws IOException{
		RTPHeader tmp = this.getHeader(packet);
		int seq = tmp.getSeqNum();
		header.setAckNum(seq);
		if(tmp.isAck()){
			this.setGetFlag(1);
		} else {
			if(this.getGetFlag() == 0){
				byte[] payload = this.getContentByte(packet);
				String fileName = new String(payload);
				this.setGetFlag(1);
				SendFileThread sendThread = new SendFileThread(this, fileName);
				threadList.add(sendThread);
				sendThread.start();
			}
			header.setGet(true);
			this.sendAck();
			header.setGet(false);
		}
		recvPacketFlush();
	}
	
	/**
	 * Handle post file message
	 * @param packet
	 * @throws IOException
	 */
	public void recvPostMsg(byte[] packet) throws IOException{
		RTPHeader tmp = this.getHeader(packet);
		int seq = tmp.getSeqNum();
		header.setAckNum(seq);
		if(this.getPostFlag() == 0){
			if(tmp.isAck()){
				this.setPostFlag(1);
			} else {
				byte[] payload = this.getContentByte(packet);
				String fileName = new String(payload);
				fileOut =  new FileOutputStream(System.getProperty("user.dir") + "/" + fileName, true);
				outBuffer=  new BufferedOutputStream(fileOut);
				header.setPost(true);
				this.sendAck();
				header.setPost(false);
			}
		}
		recvPacketFlush();
	}
	
	/**
	 * Post file to server.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void sendFile(String filename) throws IOException{
		 if(conFlag == 2) {
			//---------------- Initialize Post file -------------------
			FileInputStream fileIn = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
			byte[] name = filename.getBytes();
			
			header.setPost(true);
			header.setSeqNum(0);
			this.send(name);
			header.setPost(false);
			//System.out.println("Sending Post initialize msg.");
			timer.start();
			
			while(this.getPostFlag() == 0){
				if(timer.checkTimeout()){
					header.setPost(true);
					header.setSeqNum(0);
					this.send(name);
					header.setPost(false);
					//System.out.println("Re-send Post initialize msg.");
					timer.start();
				}
			}
			
			//---------------- Start sending file-------------------
			transTimer.start();
			int filesize = 0;
			
			byte[] buffer = new byte[RTP.BUFFERMAX - RTPHeader.headerLen];
			int payloadLen = fileIn.read(buffer);
			byte[] payload;
			timer.start();
			while( payloadLen != -1 || !queueBuffer.isEmpty()){
				if(timer.checkTimeout()){
					window.setNextToSend(window.getStartWindow());
					timer.start();
					ArrayList<byte[]> queue =  queueBuffer.returnArrayList();
					for(int i=0; i< queue.size(); i++){
						if(payloadLen == -1){
							if(i == (queue.size()-1)){
								header.setLst(true);
							}
						}
						int seq = window.getNextToSend();
						header.setSeqNum(seq);
						header.setDat(true);
						this.send(queue.get(i));
						header.setDat(false);
						header.setLst(false);
						window.setNextToSend(seq+1);
					}
				}
				if(window.getNextToSend() <= window.getEndWindow() && payloadLen != -1){
					filesize += payloadLen;
					
					payload =  new byte[payloadLen];
					System.arraycopy(buffer, 0, payload, 0, payloadLen);
					payloadLen = fileIn.read(buffer);
					if(payloadLen == -1){
						header.setLst(true);
					}
					int seq = window.getNextToSend();
					header.setSeqNum(seq);
					header.setDat(true);
					this.send(payload);
					header.setDat(false);
					header.setLst(false);
					window.setNextToSend(seq+1);
					queueBuffer.enqueue(payload);
				}
			}
			
			double transTime = transTimer.getTime();
			System.out.println("Transmission Time: " + transTime + "secs");
			System.out.println("Transmission ThroughPut: " + (double)filesize/(transTime*1024) + "Kbps");
			
			fileIn.close();
			this.setPostFlag(0);
			this.setGetFlag(0);
			header.setLst(false);
			System.out.println("-------File " + filename + " has been succesfully transimtted.-------" );
		} else {
			System.out.println("Please initialize connection first.");
		}
	}
	 
	/**
	* Post file to server.
	* 
	* @param filename
	* @throws IOException
	*/
	public void getFile(String filename) throws IOException{
		if(conFlag == 2){
			//---------------- Request Get file -------------------
			byte[] name = filename.getBytes();
			header.setGet(true);
			header.setSeqNum(0);
			this.send(name);
			header.setGet(false);
			//System.out.println("Sending Get initialize msg.");
			timer.start();
			
			while(this.getGetFlag() == 0){
				if(timer.checkTimeout()){
					header.setGet(true);
					header.setSeqNum(0);
					this.send(name);
					header.setGet(false);
					//System.out.println("Re-send Get initialize msg.");
					timer.start();
				}
			}
			
			//System.out.println("Start receiving file.");
			//---------------- Start Getting file in the listening thread-------------------
		} else {
			System.out.println("Please initialize connection first.");
		}
	}
	
	
	 synchronized public void send(byte[] data) throws IOException {
		header.setAck(false);
		byte[] dataWithHeader = packData(header.getHeader(), data);
		dataWithHeader = this.addChecksum(dataWithHeader);
		//System.out.println("Sending packet--" + "Seq Num:" + header.getSeqNum());
		sendPacket = new DatagramPacket(dataWithHeader, dataWithHeader.length,
					serverAddress, emuPort);
		socket.send(sendPacket);
	}
	
	 synchronized public void sendAck() throws IOException {
		//System.out.println("SendingAck--" + "ACK Num:" + header.getAckNum());
		 header.setAck(true);
		 byte[] dataToSend = this.addChecksum(header.getHeader());
		 sendPacket = new DatagramPacket(dataToSend, RTPHeader.headerLen,
				 serverAddress, emuPort);
		 socket.send(sendPacket);
	}
	
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
		int headerLen = receiveData[12];
		byte[] headerArray = new byte[headerLen];
		System.arraycopy(receiveData, 0, headerArray, 0, headerLen);
		header.headerFromArray(headerArray);
		
		return header;
	}
	
	/**
	 * Extract the data information from received RTP packet
	 */
	 public byte[] getContentByte(byte[] receiveData){
		int headerLen = receiveData[12];
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
	
	/**
	 * Before send the packet, call this function the add checksum field.
	 * @param packet
	 * @return
	 */
	public byte[] addChecksum(byte[] packet){
		int len = packet.length;
		short[] words = new short[len/2];
		
		for(int i = 0; i+1 < len; i =i + 2){
			if(i+1 >= len){

				short a = (short) (((((int)(packet[i])) & 0x0000FFFF)<<8) | (int)0);
				words[i/2]=a;
			}else{
				short a = (short) (((((int)(packet[i])) & 0x0000FFFF)<<8)  | (((int)(packet[i+1])) & 0x000000FF));
				words[i/2]=a;
			}
		}
		short checksum = 0;
		//adding
		for(int i = 0; i < words.length; i++){			
			int tmp = ((int)checksum & 0x0000FFFF) + ((int)words[i] & 0x0000FFFF);
			if((tmp & 0x10000) == 0x10000){
				//System.out.println("overflow");
				tmp++;
			}
			checksum = (short)tmp;
		}
		checksum = (short)(((int)checksum & 0x0000FFFF) ^ 0xFFFF);
		packet[14] = (byte)(checksum>> 8);
		packet[15] = (byte)(checksum& 0xFF);
		return packet;
	}
	
	/**
	 * After received the packet call this function to check corruption.
	 * @param packet
	 * @return
	 */
	public boolean checkChecksum(byte[] packet){
		boolean check = false;
		int len = packet.length;
		short[] words = new short[len/2];
		
		for(int i = 0; i+1 < len; i =i + 2){
			if(i+1 >= len){

				short a = (short) (((((int)(packet[i])) & 0x0000FFFF)<<8) | (int)0);
				words[i/2]=a;
			}else{
				short a = (short) (((((int)(packet[i])) & 0x0000FFFF)<<8)  | (((int)(packet[i+1])) & 0x000000FF));
				words[i/2]=a;
			}
		}
		short checksum = 0;
		//adding
		for(int i = 0; i < words.length; i++){

			int tmp = 0;
			tmp = ((int)checksum & 0x0000FFFF) + ((int)words[i] & 0x0000FFFF);
			if((tmp & 0x10000) == 0x10000){
				//System.out.println("overflow");
				tmp++;
			}
			checksum = (short)tmp;
		}
		if ((checksum & 0xFFFF) == 0xFFFF){
			check = true;
		}
		return check;
	}
	
	/**
	 * Set the window size.
	 * @param windowSize
	 */
	public void changeWinSize(int windowSize){
		if(conFlag == 2){
			this.window.setWindowSize(windowSize);
			System.out.println("The window size has been changed to " + windowSize);
		} else {
			System.out.println("Please initialize connection first.");
		}
	}
	
}
