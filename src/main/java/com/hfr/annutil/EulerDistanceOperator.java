package com.hfr.annutil;

public class EulerDistanceOperator implements DistanceOperator{

	@Override
	public float distance(float[] a, float[] b) {
		int dimension = a.length;
		double t = 0;
		for (int i = 0; i < dimension; i++) {
			t += Math.pow(a[i] - b[i], 2);
		}
		return (float)Math.sqrt(t);
	}
	
	public static EulerDistanceOperator INSTANCE = new EulerDistanceOperator();

}
