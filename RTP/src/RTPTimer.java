
public class RTPTimer {
	
	private long time;
	public static final double TIMEOUT = 0.5;
	
	/**
	 * Constructor
	 */
	public RTPTimer() {
		super();
		this.time = 0;
	}
	
	/**
	 * Set the timer to zero.
	 */
	public void start(){
		this.time = System.currentTimeMillis();
	}
	
	/**
	 * Checks the timeout, return true if timeout.
	 * @return 
	 */
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
	
	/**
	 * Get the time since the timer start in second.
	 * @return
	 */
	public double getTime(){
		return (double)((System.currentTimeMillis() - time)/1000);
	}
	
}
