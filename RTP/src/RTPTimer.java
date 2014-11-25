
public class RTPTimer {
	
	private long time;
	public static final int TIMEOUT = 1;
	
	public RTPTimer() {
		super();
		this.time = 0;
	}
	
	public void start(){
		this.time = System.currentTimeMillis();
	}
	
	public boolean checkTimeout(){
		if(System.currentTimeMillis() - this.time > 1000 * TIMEOUT){
			System.out.println("timeout");
			return true;
		}
		else{
			//System.out.println("No timeout");
			return false;
		}
	}
}
