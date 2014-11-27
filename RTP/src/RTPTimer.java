
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
			//System.out.println("--------Timeout--------");
			return true;
		}
		else{
			//System.out.println("No timeout");
			return false;
		}
	}
	public double getTime(){
		return (double)((System.currentTimeMillis() - time)/1000);
	}
	
	public void printTransTho(byte size, long time){
		System.out.println("Transmission ThroughPut: " + size/(time*1024));
	}
	public void printTransTime(){
		System.out.println("Transmission Time: " + (System.currentTimeMillis() - time)/1000);
	}
}
