package mmlib4j.representation.tree.attribute.bitquads;

import java.io.IOException;
import java.io.InputStream;

import mmlib4j.images.GrayScaleImage;


public class PatternCounter {
	public enum PatternType {
		P1(0), P2(1), P3(2), P4(3), PD(4), P1T(5), P2T(6), P3T(7), PDT(8);
		
		private final int value;
		
		private PatternType(int value) { this.value = value; }
		public int getValue() { return this.value; }
	}
	
	private final int N_LEAVES = 6561; // number of leaves in the decision tree (3**8)
	private final int N_PATTERNS_TYPE = 9; // patterns type = p1, p2, p3, p4, pd, p1t, p2t, p3t, pdt
	private final int[] dx = {-1, 0, 1,-1, 1,-1, 0, 1};
	private final int[] dy = {-1,-1,-1, 0, 0, 1, 1, 1};
	private final byte[][] leavesMap;
	private char[] tempKey;
	
	public PatternCounter(String filename) {
		InputStream in = PatternCounter.class.getResourceAsStream(filename);
		leavesMap = new byte[N_LEAVES][];
		tempKey = new char[8];
		
		try {
			for (int i = 0; i < leavesMap.length; ++i) {
				leavesMap[i] = new byte[N_PATTERNS_TYPE];
				in.read(leavesMap[i]);
			}
		}
		catch(IOException e) {
			System.err.println("mmlib4j got an error, when it tried to read a decision tree for bit-quads "
					+ "computation.");
			e.printStackTrace();
		}
	}
	
	private int getBase10IntfromABase3String(String base3string)
	{
		return Integer.parseInt(base3string, 3);
	}
	
	// 0 - lower than relation (<), 1 - equal relation (=), 2 - greater than relation (>) */
	private char computeRelationType(int px, int py, int qx, int qy, GrayScaleImage img) {
		if (!img.isPixelValid(qx, qy) || (img.getValue(qx, qy) < img.getValue(px, py)))
			return '0';
		else if (img.getValue(qx, qy) > img.getValue(px, py))
			return '2';
		else
			return '1';
	}
	
	private int computeLeavesMapKey(int px, int py, GrayScaleImage img) {
		for (int i = 0; i < dx.length; i++) {
			tempKey[i] = computeRelationType(px, py, px + dx[i], py + dy[i], img);
		}	
		return getBase10IntfromABase3String(new String(tempKey));
	}
	
	public byte[] count(int px, int py, GrayScaleImage img) {
		int key = computeLeavesMapKey(px, py, img);
		return leavesMap[key];
	}
}	