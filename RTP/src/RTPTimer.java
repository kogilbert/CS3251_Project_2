
public class RTPTimer {
	
	private long time;
	public static final double TIMEOUT = 0.5;
	
	public RTPTimer() {
		super();
		this.time = 0;
	}
	
	public void start(){
		this.time = System.currentTimeMillis();
	}
	
	public boolean checkTimeout(){
		if(System.currentTimeMillis() - this.time > 1000 * TIMEOUT){
			System.out.println("--------Timeout--------");
			return true;
		}
		else{
			//System.out.println("No timeout");
			return false;
		}
	}
}
