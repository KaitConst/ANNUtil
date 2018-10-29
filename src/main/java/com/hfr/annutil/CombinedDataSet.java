package com.hfr.annutil;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class CombinedDataSet implements DataSet{
	private DataSet rawset;
	private int combinedLevel;
	
	public CombinedDataSet(DataSet rawset, int combinedLevel) {
		this.rawset = rawset;
		this.combinedLevel = combinedLevel;
	}

	@Override
	public Iterator<Vector> iterator() {
		return new Iterator<Vector>() {
			private AtomicLong ider = new AtomicLong(0);
			
			@Override
			public Vector next() {
				Vector ret = new Vector();
				ret.v = new float[rawset.getDimension() * combinedLevel];
				ret.id = ider.getAndIncrement();
				for (int i = 0; i < combinedLevel; i++) {
					float[] v = rawset.iterator().next().v;
					for (int j = 0; j < rawset.getDimension(); j++) {
						ret.v[i*combinedLevel + j] = v[j];
					}
				}
				return ret;
			}
			
			@Override
			public boolean hasNext() {
				return rawset.iterator().hasNext();
			}
		};
	}


	@Override
	public int getDimension() {
		return rawset.getDimension() * combinedLevel;
	}

}
