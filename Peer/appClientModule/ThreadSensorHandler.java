import sensor.AccelerometerSimulator;
import sensor.BufferMeasurements;

public class ThreadSensorHandler extends Thread {
	
	private AccelerometerSimulator mine_sim;
	
	public ThreadSensorHandler(){
		BufferMeasurements measurementsQueue = new BufferMeasurements();
		mine_sim = new AccelerometerSimulator(measurementsQueue);
	}
	
	public void run(){
		mine_sim.run();
	}

}
