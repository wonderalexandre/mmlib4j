package mmlib4j.representation.tree.attribute.bitquads;

import java.io.IOException;
import java.io.InputStream;

import mmlib4j.images.GrayScaleImage;

import com.sun.corba.se.impl.ior.ByteBuffer;

public class PatternsCounter {
	public enum PatternType {
		Q1(0), Q2(1), Q3(2), Q4(3), QD(4), Q1T(5), Q2T(6), Q3T(7), QDT(8);
		
		private final int value;
		
		private PatternType(int value) { this.value = value; }
		public int getValue() { return this.value; }
	}
	
	private final int N_LEAVES = 6561; // number of leaves in the decision tree (3**8)
	private final int N_PATTERNS_TYPE = 9; // patterns type = q1, q2, q3, q4, qd, q1t, q2t, q3t, qdt
	private final int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
	private final int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
	private byte[][] leavesMap;
	private char[] tempKey;
	
	public PatternsCounter(String filename) {
		InputStream in = PatternsCounter.class.getResourceAsStream(filename);	
		leavesMap = new byte[N_LEAVES][];
		tempKey = new char[8];
		
		try {
			for (int i = 0; i < leavesMap.length; ++i) {
				leavesMap[i] = new byte[N_PATTERNS_TYPE];	
				in.read(leavesMap[i]);
			}
		} catch(IOException e) {
			System.err.println("mmlib4j got an error, when it tried to read a decision tree for bit-quads computation.");
			e.printStackTrace();
		}		
	}
	
	private int getBase10IntFromABase3String(String base3string) {
		return Integer.parseInt(base3string, 3);
	}
	
	/* 0 - lower than relation (<) , 1 - equals relation (=), 2 - greater than relation (>) */
	private char computeRelationType(int px, int py, int qx, int qy, GrayScaleImage img) {
		if (!img.isPixelValid(qx, qy) || (img.getValue(qx, qy) < img.getValue(px, py)))
			return '0';
		else if (img.getValue(qx, qy) > img.getValue(px, py))
			return '2';
		else
			return '1';
	}
	
	private int computeLeavesMapKey(int px, int py, GrayScaleImage img) {
		for (int i = 0; i < dx.length; i++)
			tempKey[i] = computeRelationType(px, py, px + dx[i], py + dy[i], img);
		
		return getBase10IntFromABase3String(new String(tempKey));
	}
	
	public byte[] count(int px, int py, GrayScaleImage img) {
		int key = computeLeavesMapKey(px, py, img);
		return leavesMap[key];
	}
}
