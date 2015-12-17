package mmlib4j.math;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ArrayUtils {

	private ArrayUtils () {
        
    }
    
    /*-- maxValue() --*/
	/**
	 * Returns the largest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the value
	 */
	public static int maxValue(final int[] arr) {
		if (arr.length < 0)
			return 0;

		int max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}
	
    /*-- maxValue() --*/
	/**
	 * Returns the largest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the value
	 */
	public static float maxValue(final float[] arr) {
		if (arr.length < 0)
			return 0;

		float max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

    /*-- minValue() --*/
	/**
	 * Returns the smallest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the value
	 */
	public static int minValue(final int[] arr) {
		if (arr.length < 0)
			return 0;

		int min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}
	
	   /*-- minValue() --*/
		/**
		 * Returns the smallest value in the array.
		 * 
		 * @param arr
		 *            array of #t#
		 * @return the value
		 */
		public static float minValue(final float[] arr) {
			if (arr.length < 0)
				return 0;

			float min = arr[0];
			for (int i = 1; i < arr.length; i++) {
				if (arr[i] < min) {
					min = arr[i];
				}
			}

			return min;
		}

    /*-- maxIndex() --*/
	/**
	 * Returns the index to the biggest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the index
	 */
	public static int maxIndex(final int[] arr) {
		int max = Integer.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

    /*-- maxIndex() --*/
	/**
	 * Returns the index to the biggest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the index
	 */
	public static int maxIndex(final float[] arr) {
		float max = Float.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}
	
    /*-- minIndex() --*/
	/**
	 * Returns the index to the smallest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the index
	 */
	public static int minIndex(final int[] arr) {
		int min = Integer.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
				index = i;
			}
		}

		return index;
	}
	
	/*-- minIndex() --*/
	/**
	 * Returns the index to the smallest value in the array.
	 * 
	 * @param arr
	 *            array of #t#
	 * @return the index
	 */
	public static int minIndex(final float[] arr) {
		float min = Float.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
				index = i;
			}
		}

		return index;
	}

    /*-- range() (not templated) --*/
	/**
	 * Extract a range
	 * 
	 * @param start
	 * @param length
	 * @return [start...length] (inclusive)
	 */
	public static int[] range(final int start, final int length) {
		final int[] range = new int[length - start + 1];
		for (int i = start; i <= length; i++) {
			range[i - start] = i;
		}
		return range;
	}

}