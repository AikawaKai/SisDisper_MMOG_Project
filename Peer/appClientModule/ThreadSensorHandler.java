import java.util.ArrayList;

import peer.objects.Bomb;
import peer.objects.BufferMoves;
import sensor.AccelerometerSimulator;
import sensor.BufferMeasurements;
import sensor.Measurement;

public class ThreadSensorHandler extends Thread {
	
	private AccelerometerSimulator mine_sim;
	private BufferMeasurements measurementsQueue;
	private BufferMoves bombs;
	private double a;
	private double threshold;
	
	public ThreadSensorHandler(BufferMoves buffer_b, double a_input, double th){
		a = a_input;
		threshold = th;
		measurementsQueue = new BufferMeasurements();
		bombs = buffer_b;
		mine_sim = new AccelerometerSimulator(measurementsQueue);
	}
	
	public void run(){
		mine_sim.run();
		double mean_i;
		double EMA_i;
		double EMA_i_p;
		String color = "";
		Bomb b;
		ArrayList<Measurement> measures = (ArrayList<Measurement>) measurementsQueue.readAllAndClean();
		EMA_i_p = calculateMean(measures);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				measures = (ArrayList<Measurement>) measurementsQueue.readAllAndClean();
				mean_i = calculateMean(measures);
				Thread.sleep(1000);
				// EM A i = EM A i−1 + α(m i − EM A i−1 )
				EMA_i = EMA_i_p + a * (mean_i-EMA_i_p);
				if(EMA_i-EMA_i_p>threshold){ //bomba trovata
					switch(((int)EMA_i)%4){
					case 0:
						color = "verde";
						break;
					case 1:
						color = "rossa";
						break;
					case 2:
						color = "blu";
						break;
					case 3:
						color = "gialla";
						break;
					}
				b = new Bomb(color);
				synchronized(bombs){
					System.out.println("Hai trovato una bomba "+color+"!");
					bombs.addMove(b);
				}
				}
				EMA_i_p = EMA_i;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private double calculateMean(ArrayList<Measurement> list){
		double tot = 0;
		for(Measurement val: list){
			tot = tot + val.getValue();
		}
		return tot/list.size();
	}

}
