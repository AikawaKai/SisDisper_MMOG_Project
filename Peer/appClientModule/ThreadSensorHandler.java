import java.util.ArrayList;

import peer.objects.Bomb;
import peer.objects.BufferMoves;
import peer.objects.SingletonFactory;
import sensor.AccelerometerSimulator;
import sensor.BufferMeasurements;
import sensor.Measurement;

public class ThreadSensorHandler extends Thread {
	
	private BufferMeasurements measurementsQueue;
	private double a;
	private double threshold;
	
	public ThreadSensorHandler(double a_input, double th){
		a = a_input;
		threshold = th;
		measurementsQueue = new BufferMeasurements();
	}
	
	public void run(){
		Thread simulator = new Thread(new AccelerometerSimulator(measurementsQueue));
		simulator.start();
		double mean_i;
		double EMA_i;
		double EMA_i_p;
		String color = "";
		BufferMoves bombs = SingletonFactory.getSingletonBombMoves();
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
						System.out.println("[INFO] Hai trovato una bomba "+color+"!");
						bombs.addMove(b);
					}
				}
				EMA_i_p = EMA_i;
				Thread.sleep(1000);
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
