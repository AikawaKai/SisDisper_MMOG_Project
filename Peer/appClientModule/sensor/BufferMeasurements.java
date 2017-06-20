package sensor;

import java.util.ArrayList;
import java.util.List;

public class BufferMeasurements implements Buffer<Measurement> {
	ArrayList<Measurement> measurements = new ArrayList<Measurement>();
	
	public BufferMeasurements() {
	}

	@Override
	public synchronized void addNewMeasurement(Measurement t) {
		measurements.add(t);
	}

	@Override
	public synchronized List readAllAndClean() {
		ArrayList<Measurement> newList = new ArrayList<Measurement>();
		for(int i=0;i<measurements.size();i++){
			newList.add(measurements.remove(i));
		}
		return newList;
	}

}
