package com.hfr.annutil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.hfr.annutil.ANNHelper.LSHHashFun;
import com.hfr.annutil.ANNHelper.NearestVector;



public class Main {
	

	public static void case1() throws IOException {
		final int DIMENSIONTIMES = 4;
		final int BASEDIMENSION = 2;
		final float ZOOM = (float)1;
		final float R = (float)1;
		final int HASHCOUNT = 200000;
		final int COUNT = 1024*1024;		
		
		int dimension = DIMENSIONTIMES * BASEDIMENSION;
		LSHHashFun[] funFamily = ANNHelper.makeLshAArray(BASEDIMENSION, 333, HASHCOUNT, R);
		
		DataSet ds = new RandomDataSet(333, dimension, COUNT, ZOOM);

		float[] query = ds.iterator().next().v;
		long querySimHash;
		int[] queryBow;
		{
			queryBow = ANNHelper.makeBoW(query, funFamily, R);
			querySimHash = ANNHelper.makeSimHash(queryBow);
		}
		
		VectorExPrinter vep = (v) -> {
			int[] bow = ANNHelper.makeBoW(v.v, funFamily, R);
			int sameW = 0;
			for (int i = 0; i < queryBow.length; i++) {
				if (queryBow[i] == bow[i]) sameW++;
			}
			long simHash = ANNHelper.makeSimHash(bow);
			int val = Long.bitCount(simHash ^ querySimHash);
			int[] showBow = Arrays.copyOf(bow, 100);
			return val + "\t" + sameW + "\t" + Long.toBinaryString(simHash) + "\t" + Arrays.toString(showBow);
		};
		
		System.out.println("Begin");
		System.out.println("Nearest:");
		List<NearestVector> nvListNearest = ANNHelper.topNNearest(ds, query, 10, EulerDistanceOperator.INSTANCE);
		ANNHelper.printNearestListEx(nvListNearest,vep);
		System.out.println("Farest:");
		List<NearestVector> nvListFarest = ANNHelper.topNFarest(ds, query, 10, EulerDistanceOperator.INSTANCE);
		ANNHelper.printNearestListEx(nvListFarest, vep);
		System.out.println("End");
		
	}
	
	public static void case2() throws IOException {
		
		final float R = (float)2.5e-41;
		final int HASHCOUNT = 2000;
		final int BASEDIMENSION = 2;
		final int DIMENSIONTIMES = 64; // fix	128
		
		int dimension = DIMENSIONTIMES * BASEDIMENSION;
		System.out.println("Begin");
		ListDataSet ds = loadFVECS("E:\\soft\\es\\dataset\\siftsmall\\siftsmall_base.fvecs", dimension);
		LSHHashFun[] funFamily = ANNHelper.makeLshAArray(dimension, 333, HASHCOUNT, R);
		
		float[] query = ds.getList().get(0).v;
		long querySimHash;
		int[] queryBow;
		{
			queryBow = ANNHelper.makeBoW(query, funFamily, R);
			querySimHash = ANNHelper.makeSimHash(queryBow);
		}
		
		VectorExPrinter vep = (v) -> {
			int[] bow = ANNHelper.makeBoW(v.v, funFamily, R);
			int sameW = 0;
			for (int i = 0; i < queryBow.length; i++) {
				if (queryBow[i] == bow[i]) sameW++;
			}
			long simHash = ANNHelper.makeSimHash(bow);
			int val = Long.bitCount(simHash ^ querySimHash);
			int[] showBow = Arrays.copyOf(bow, 100);
			return val + "\t" + sameW + "\t" + Long.toBinaryString(simHash) + "\t" + Arrays.toString(showBow);
		};
		
		System.out.println("Nearest:");
		List<NearestVector> nvListNearest = ANNHelper.topNNearest(ds, query, 100, EulerDistanceOperator.INSTANCE);
		ANNHelper.printNearestListEx(nvListNearest,vep);
		System.out.println("Farest:");
		List<NearestVector> nvListFarest = ANNHelper.topNFarest(ds, query, 10, EulerDistanceOperator.INSTANCE);
		ANNHelper.printNearestListEx(nvListFarest, vep);
		System.out.println("End");
				
	}
	
	
	public static void main(String[] args) throws IOException {
		case1();
	}
	
	public static ListDataSet loadFVECS(String path, int dimension) throws IOException {
		ListDataSet lds = new ListDataSet(dimension);
		try (FileInputStream file = new FileInputStream(path)) {
			try (BufferedInputStream bis = new BufferedInputStream(file)) {
				try (DataInputStream is = new DataInputStream(bis)) {
					while(is.available() > 0) {
						long rawDim;
						try {
							rawDim = is.readInt();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						long v1 = ((rawDim & 0xffL) << 24);
						long v2 = ((rawDim & 0xff00L) << 8);
						long v3 = ((rawDim & 0xff0000L) >> 8);
						long v4 = ((rawDim & 0xff000000L) >> 24);
						int dimCount = (int) (v1 | v2 | v3 | v4);
						if (dimCount < dimension) throw new RuntimeException("dimension not enough");
						float[] v = new float[dimension]; 
						for (int i = 0; i < dimCount; i++) {
							try {
								float t= is.readFloat();
								if (i < dimension) v[i] = t;
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						lds.add(v);
					}
				}
			}
		}
		return lds;
	}
	
	

}
