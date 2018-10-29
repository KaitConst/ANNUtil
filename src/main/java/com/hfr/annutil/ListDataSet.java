package com.hfr.annutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ListDataSet implements DataSet{
	private int dimension;
	private List<Vector> list;
	private AtomicLong ider = new AtomicLong(0);
	
	public ListDataSet(int dimension) {
		this.dimension = dimension;
		this.list = new ArrayList<>();
	}
	
	public void add(float[] v) {
		Vector vec = new Vector();
		vec.v = v;
		vec.id = ider.getAndIncrement();
		list.add(vec);
	}
	
	public List<Vector> getList() {
		return list;
	}
	
	@Override
	public Iterator<Vector> iterator() {
		return list.iterator();
	}

	@Override
	public int getDimension() {
		return dimension;
	}

}
