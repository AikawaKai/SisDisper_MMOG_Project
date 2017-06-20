package sensor;

import java.util.ArrayList;
import java.util.List;

public class BufferMeasurements implements Buffer<Measurement> {
	ArrayList<Measurement> measurements = new ArrayList<Measurement>();
	
	public BufferMeasurements() {
	}

	@Override
	public void addNewMeasurement(Measurement t) {
		synchronized(measurements){
			measurements.add(t);
			measurements.notify();
		}
	}

	@Override
	public List readAllAndClean() {
		synchronized(measurements){
			if(measurements.size()==0){
				try {
					measurements.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ArrayList<Measurement> newList = new ArrayList<Measurement>();
			for(int i=0;i<measurements.size();i++){
				newList.add(measurements.remove(i));
			}
			return newList;
		}
			
	}

}
