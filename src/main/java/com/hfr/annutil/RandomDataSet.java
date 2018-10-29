package com.hfr.annutil;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomDataSet implements DataSet{

	private long seed;
	private final int dimension;
	private final int count;
	private float zoom;
	
	public RandomDataSet(long seed, int dimension, int count, float zoom) {
		this.seed = seed;
		this.dimension = dimension;
		this.count = count;
		this.zoom = zoom;
	}
	
	@Override
	public Iterator<Vector> iterator() {
		return new Iterator<Vector>() {
			private Random r = new Random(seed);
			private AtomicInteger current = new AtomicInteger(0);
			
			@Override
			public Vector next() {
				Vector ret = new Vector();
				ret.v = new float[dimension];
				ret.id = current.getAndIncrement();
				for (int i=0; i<dimension; i++) {
					double t = r.nextGaussian();
					ret.v[i] =  (float)(t) * zoom; // (-1 , 1) * zoom
				}
				return ret;
			}
			
			@Override
			public boolean hasNext() {
				return current.intValue() < count;
			}
		};
	}

	@Override
	public int getDimension() {
		return dimension;
	}

}
