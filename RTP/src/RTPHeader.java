
public class RTPHeader {
	short sourcePort;
	short destPort;
	int seqNum;
	int ackNum;
	final int headerLen = 17;
	boolean urg;
	boolean ack;
	boolean psh;
	boolean rst;
	boolean syn;
	boolean fin;
	short checksum;
	byte[] header;

	public RTPHeader() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RTPHeader(short sourcePort, short destPort, int seqNum, int ackNum) {
		super();
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.seqNum = seqNum;
		this.ackNum = ackNum;
		this.urg = false;
		this.ack = false;
		this.psh = false;
		this.rst = false;
		this.syn = false;
		this.fin = false;
		this.checksum = 0;
		this.header = new byte[headerLen];
	}
	
	public byte[] setHeader(){
		this.header[0] = (byte)(this.sourcePort >> 8);
		this.header[1] = (byte)(this.sourcePort & 0xFF);
		this.header[2] = (byte)(this.destPort >> 8);
		this.header[3] = (byte)(this.destPort & 0xFF);
		this.header[4] = (byte)(this.headerLen & 0xFF);
		this.header[5] = (byte)(this.seqNum >> 24);
		this.header[6] = (byte)(this.seqNum >> 16);		
		this.header[7] = (byte)(this.seqNum >> 8);
		this.header[8] = (byte)(this.seqNum & 0xFF);
		this.header[9] = (byte)(this.ackNum >> 24);
		this.header[10] = (byte)(this.ackNum >> 16);		
		this.header[11] = (byte)(this.ackNum >> 8);
		this.header[12] = (byte)(this.ackNum & 0xFF);
		
		this.header[13] = 0;
		this.header[14] = 0;
		if (fin) {
			this.header[14]= (byte)(this.header[14] | 0x1);
		}
		if (syn) {
			this.header[14]= (byte)(this.header[14] | 0x2);
		}
		if (rst) {
			this.header[14]= (byte)(this.header[14] | 0x4);
		}
		if (psh) {
			this.header[14]= (byte)(this.header[14] | 0x8);
		}
		if (ack) {
			this.header[14]= (byte)(this.header[14] | 0x10);
		}
		if (urg) {
			this.header[14]= (byte)(this.header[14] | 0x20);
		}
		this.header[15] = (byte)(this.checksum >> 8);
		this.header[16] = (byte)(this.checksum & 0xFF);
		return this.header;
	}
	
	public void headerFromArray(byte[] header){
		this.sourcePort = (short)(header[0]<<8 | ((short)0 | 0xFF) & header[1]);
		this.destPort = (short)(header[2]<<8 | ((short)0 | 0xFF) & header[3]);
		this.seqNum = (int)(header[5]<<24 | header[6] << 16 | header[7] << 8 | ((short)0 | 0xFF) & header[8]);
		this.ackNum = (int)(header[9]<<24 | header[10] << 16 | header[11] << 8 | ((short)0 | 0xFF) & header[12]);
		if((byte)(header[14] & 0x1) == (byte)0x1){
			this.fin = true;
		}
		if((byte)(header[14] & 0x2) == (byte)0x2){
			this.syn = true;
		}
		if((byte)(header[14] & 0x4) == (byte)0x4){
			this.rst = true;
		}
		if((byte)(header[14] & 0x8) == (byte)0x8){
			this.psh = true;
		}
		if((byte)(header[14] & 0xF) == (byte)0x10){
			this.ack = true;
		}
		if((byte)(header[14] & 0x10) == (byte)0x20){
			this.urg = true;
		}
		this.checksum = (short)(header[15]<<8 | ((short)0 | 0xFF) & header[16]);
	}

	public byte[] getHeader() {
		this.header = setHeader();
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}

	public short getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(short sourcePort) {
		this.sourcePort = sourcePort;
	}

	public short getDestPort() {
		return destPort;
	}

	public void setDestPort(short destPort) {
		this.destPort = destPort;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public int getAckNum() {
		return ackNum;
	}

	public void setAckNum(int ackNum) {
		this.ackNum = ackNum;
	}

	public boolean isUrg() {
		return urg;
	}

	public void setUrg(boolean urg) {
		this.urg = urg;
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public boolean isPsh() {
		return psh;
	}

	public void setPsh(boolean psh) {
		this.psh = psh;
	}

	public boolean isRst() {
		return rst;
	}

	public void setRst(boolean rst) {
		this.rst = rst;
	}

	public boolean isSyn() {
		return syn;
	}

	public void setSyn(boolean syn) {
		this.syn = syn;
	}

	public boolean isFin() {
		return fin;
	}

	public void setFin(boolean fin) {
		this.fin = fin;
	}

	public short getChecksum() {
		return checksum;
	}

	public void setChecksum(short checksum) {
		this.checksum = checksum;
	}
	
	public int getHeaderLen() {
		return headerLen;
	}

	public String toString(){
		return header.toString();
	}

}
