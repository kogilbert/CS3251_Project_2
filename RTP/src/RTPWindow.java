import java.io.FileInputStream;


public class RTPWindow {
	int windowSize;
	int startWindow;
	int endWindow;
	int nextToSend;
	
	
	public RTPWindow() {
		super();
		this.windowSize = 2;
		this.startWindow = 0;
		this.endWindow = 0 + this.windowSize - 1;
		this.nextToSend = 0;
	}
	
	//for sending files
	public void send(FileInputStream myFile){
		//3 way handshake to initiate connection
		
		boolean finishedSending = false;
		
		while(!finishedSending){
			
			//if not all available window is sent, send next packet if latestSent <= endWindow, latestSent++
			if(nextToSend <= endWindow){
				
				
				nextToSend++;
			}

			
			//if received ACK that is the beginning of window size move start and end window size
			
			//if timer times out then retransmit entire window from start window point
			
			//if last packet received set finished sending to true
		}
		
	}




	public int getNextToSend() {
		return nextToSend;
	}

	public void setNextToSend(int nextToSend) {
		this.nextToSend = nextToSend;
	}

	public int getWindowSize() {
		return windowSize;
	}


	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}


	public int getStartWindow() {
		return startWindow;
	}


	public void setStartWindow(int startWindow) {
		this.startWindow = startWindow;
	}


	public int getEndWindow() {
		return endWindow;
	}


	public void setEndWindow(int endWindow) {
		this.endWindow = endWindow;
	}
	
	

}
