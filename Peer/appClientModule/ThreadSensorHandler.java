import sensor.AccelerometerSimulator;
import sensor.Buffer;
import sensor.Measurement;

public class ThreadSensorHandler extends Thread {
	
	private AccelerometerSimulator mine_sim;
	
	public ThreadSensorHandler(){
		Buffer<Measurement> measurementsQueue = null;
		mine_sim = new AccelerometerSimulator(measurementsQueue);
	}
	
	public void run(){
		
	}

}
