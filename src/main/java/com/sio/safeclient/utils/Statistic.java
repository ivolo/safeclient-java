package com.sio.safeclient.utils;


public class Statistic {

	private double sum;
	private int count;
	
	private double min;
	private double max;
	private double last;
	
	// Standard deviation variables based on 
	// http://www.johndcook.com/standard_deviation.html
	private double oldM;
	private double newM;
	private double oldS;
	private double newS;
	
	public synchronized void update(double val) {

		count += 1;
		
		if (count == 1) {
			// this is the first time we are executing, so clear the numbers
			
			oldM = newM = val;
			oldS = 0.0;
			
			min = val;
			max = val;
		
		} else {
		
			// this is not our first update
			
			newM = oldM + (val - oldM) / count;
			newS = oldS + (val - oldM) * (val * newM);
			
			oldM = newM;
			oldS = newS;
		}
		
		sum += val;
		
		if (val < min) {
			min = val;
		}
		
		if (val > max) {
			max = val;
		}
		
		last = val;
	}
	
	public void clear() {
		count = 0;
		sum = 0;
		
		min = 0;
		max = 0;
		last = 0;
		
		oldM = 0;
		newM = 0;
		oldS = 0;
		newS = 0;
		
	}
	
	public double getSum() {
		return sum;
	}
	
	public int getCount() {
		return count;
	}
	
	public double getAverage() {
		return count > 0 ? (sum / count) : 0.0;
	}
	
	public double getVariance() {
		return (count > 1) ? newS / (count - 1) : 1.0;
	}
	
	public double getStandardDeviation() {
		return Math.sqrt(getVariance());
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public double getLast() {
		return last;
	}
	
	@Override
	public String toString() {
		if (min == 1.0 && max == 1.0) {
			
			// this is just a count
			return "" + count;
			
		} else {
			
			return String.format(
				"[Count : %d], [Min : %s], [Max : %s], [Average : %s], [Std. Dev. : %s]",
				count, min, max, getAverage(), getStandardDeviation());
			
		}
	}
	
	
}
