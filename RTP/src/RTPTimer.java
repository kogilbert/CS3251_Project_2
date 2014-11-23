
public class RTPTimer {
	
	private long time;
	public static final int TIMEOUT = 10;
	
	public RTPTimer() {
		super();
		this.time = 0;
	}
	
	public void start(){
		time = System.currentTimeMillis();
	}
	
	public boolean checkTimeout(){
		if(System.currentTimeMillis() - this.time > 1000 * TIMEOUT){
			return true;
		}
		else{
			return false;
		}
	}
}
