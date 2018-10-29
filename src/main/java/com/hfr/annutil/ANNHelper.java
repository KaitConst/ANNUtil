package com.hfr.annutil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import com.google.common.hash.Hashing;

/**
 * @author hufei
 *
 */
public class ANNHelper {
	public static class NearestVector {
		public float distance;
		public Vector v;
	}
	
	private static List<NearestVector> topNNearestInner(DataSet ds, float[] query, int n, DistanceOperator oper, Comparator<Float> distanceComparator) {
		Comparator<NearestVector> listComparator =  (a,b)-> {
			return distanceComparator.compare(a.distance, b.distance);
		};
		Comparator<NearestVector> pqComparator =  (a,b)-> {
			return -distanceComparator.compare(a.distance, b.distance);
		};
		PriorityQueue<NearestVector> pq = new PriorityQueue<>(n, pqComparator);
		
		for (Vector v : ds) {
			float distance = oper.distance(v.v, query);
			if (pq.size() < n || distanceComparator.compare(distance , pq.peek().distance) < 0) {
				NearestVector nv = new NearestVector();
				nv.distance = distance;
				nv.v = v;
				pq.offer(nv);
			}
			if (pq.size() > n) {
				pq.poll();
			}
		}
		
		List<NearestVector> ret = new ArrayList<>();
		ret.addAll(pq);
		ret.sort(listComparator);
		return ret;
	}
	
	public static List<NearestVector> topNNearest(DataSet ds, float[] query, int n, DistanceOperator oper) {
		Comparator<Float> comparator =  (a,b)-> {
			return a < b ? -1 : a == b ? 0 : 1;
		};
		return topNNearestInner(ds, query, n, oper, comparator);
	}
	
	public static List<NearestVector> topNFarest(DataSet ds, float[] query, int n, DistanceOperator oper) {
		Comparator<Float> comparator =  (a,b)-> {
			return a < b ? 1 : a == b ? 0 : -1;
		};
		return topNNearestInner(ds, query, n, oper, comparator);
	}
	
	public static void printNearestListEx(List<NearestVector> nvList, VectorExPrinter p) {
		int i = 0;
		for (NearestVector nv : nvList) {
			i++;
			String ps = p.makeDescription(nv.v);
			System.out.println(String.format("%d. \t<%d> \t%g \t%s", i, nv.v.id, nv.distance, ps));
		}
	}
	
	public static void printNearestList(List<NearestVector> nvList) {
		printNearestListEx(nvList, (v)->"");
	}
	
	
	public static class LSHHashFun {
		float[] a;
		float b;
	}
	
	/**
	 * pStable的局部性Hash中的向量投影，公式为⌊(a⋅v+b)/r⌋
	 */
	public static int[] lshProjection(float[] v, LSHHashFun hashFun, float r) {
		int dimension = v.length;
		int baseDimension = hashFun.a.length;
		int dimensionTimes = dimension / baseDimension;
		int[] ret = new int[dimensionTimes];
		for (int i = 0; i < dimensionTimes; i++) {
			double t = 0;
			for (int j = 0; j < baseDimension; j++) {
				t += (double)v[baseDimension*i + j]*hashFun.a[j];
			}
			t += hashFun.b;
			t /= r;
			ret[i] = (int)t;
		}
		return ret;
	}
	
	/**
	 * 生成pStable的局部性Hash函数族，多个”公式⌊(a⋅v+b)/r⌋中的a和b“, 所生成的b都不大于r
	 */
	public static LSHHashFun[] makeLshAArray(int dimension, int seed, int count, float r) {
		Random random = new Random(seed);
		LSHHashFun[] funFamily = new LSHHashFun[count];
		for (int i = 0; i < count; i++) {
			LSHHashFun fun = new LSHHashFun();
			fun.a = new float[dimension];
			fun.b = (float)(random.nextGaussian() * r);
			double vsum = 0;
			for (int j = 0; j < dimension; j++) {
				double t = Math.pow(random.nextGaussian(),1);
				vsum += t;
				fun.a[j] = (float)t;
			}
			// 归一化
			for (int j = 0; j < dimension; j++) {
				fun.a[j] = (float)(fun.a[j] / vsum);
			}
			funFamily[i] = fun;
		}
		return funFamily;
	}
	
	/**
	 * 生成多个lsh projection(投影)的值，作为向量的多个BoW(词袋)特征，单次公式为⌊(a⋅v+b)/r⌋
	 */
	public static int[] makeBoW(float[] v, LSHHashFun[] funFamily, float r) {
		int dimension = v.length;
		int baseDimension = funFamily[0].a.length;
		int dimensionTimes = dimension / baseDimension;
		
		int hashCount = funFamily.length;
		int[] ret = new int[hashCount*dimensionTimes];
		for (int i = 0; i < hashCount; i++) {
			int[] t = lshProjection(v, funFamily[i], r);
			for (int j = 0; j < t.length; j++) {
				ret[i*dimensionTimes + j] = t[j];
			}
		}
		return ret;
	}
	
	public static long makeSimHash(int[] bow) {
		long[] sums = new long[64];
		int count = bow.length;
		for (int i = 0; i < count; i++) {
			long hash64 = Hashing.murmur3_128().hashLong((long)i<<32 |  ((long)bow[i]&0x00000000ffffffffL)).asLong();
			for (int j = 0; j < 64; j++) {
				long val = (hash64 >>> j) & 0x01L;
				if (val > 0) sums[j]++;
				else sums[j]--;
			}
		}
		
		long ret = 0;
		for (int i = 0; i < 64; i++) {
			if (sums[i] > 0) {
				ret |= (0x01L<<i);
			}
		}
		return ret;
	}
	
}
