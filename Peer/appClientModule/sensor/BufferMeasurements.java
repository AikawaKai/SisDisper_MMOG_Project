package sensor;

import java.util.List;

public class BufferMeasurements implements Buffer<Measurement> {
	
	public BufferMeasurements() {
	}

	@Override
	public void addNewMeasurement(Measurement t) {
		
		System.out.println(t.toString());
	}

	@Override
	public List readAllAndClean() {
		return null;
	}

}
