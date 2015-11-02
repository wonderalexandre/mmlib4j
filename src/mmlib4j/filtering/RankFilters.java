package mmlib4j.filtering;
import java.util.Arrays;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.math.ArrayUtils;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 * 
 * Fast implementation filters
 * Adapted of ImageJ - ij.plugin.filter.RankFilters 
 */
public class RankFilters  {
	public static final int	 MEAN=0, MIN=1, MAX=2, VARIANCE=3, MEDIAN=4;
	
	// Current state of processing is in class variables. Thus, stack parallelization must be done
	// ONLY with one thread for the image (not using these class variables):
	private int highestYinCache;		// the highest line read into the cache so far
	private boolean threadWaiting;		// a thread waits until it may read data
	private boolean copyingToCache;		// whether a thread is currently copying data to the cache

	public GrayScaleImage rank(GrayScaleImage img, AdjacencyRelation adj, int filterType) {
		GrayScaleImage ipOut = img.duplicate();
		rankProcess(img, adj, filterType, ipOut);
		return ipOut;
	}
	

	/** Filters an image by any method except 'despecle' (for 'despeckle', use 'median' and radius=1)
	 * @param ip The image subject to filtering
	 * @param radius The kernel radius
	 * @param filterType as defined above; DESPECKLE is not a valid type here; use median and
	 *		  a radius of 1.0 instead
	 * @param whichOutliers BRIGHT_OUTLIERS or DARK_OUTLIERS for 'outliers' filter
	 * @param threshold Threshold for 'outliers' filter
	 */
	public void rankProcess(final GrayScaleImage ip, AdjacencyRelation adj, final int filterType, final GrayScaleImage ipOut) {
		final int[] lineRadii = makeLineRadii(adj);
		
		
		int numThreads = (adj.getSize() < 10? 10 : ip.getHeight());

		int kHeight = kHeight(lineRadii);
		int kRadius	 = kRadius(lineRadii);
		final int cacheWidth = ip.getWidth() + 2*kRadius;
		final int cacheHeight = kHeight + (numThreads>1 ? 2*numThreads : 0);
		
		final float[] cache = new float[cacheWidth*cacheHeight];
		highestYinCache = Math.max(-kHeight/2, 0) - 1; //this line+1 will be read into the cache first 

		final int[] yForThread = new int[numThreads];		//threads announce here which line they currently process
		for(int i=0; i < yForThread.length; i++){
			yForThread[i] = -1;
		}
		
		final Thread[] threads = new Thread[numThreads-1];	//thread number 0 is this one, not in the array
		for (int t=numThreads-1; t>0; t--) {
			final int ti=t;
			final Thread thread = new Thread(
					new Runnable() {
						final public void run() {
							doFiltering(ip, ipOut, lineRadii, cache, cacheWidth, cacheHeight,filterType, yForThread, ti);
						}
					},
			"RankFilters-"+t);
			thread.setPriority(Thread.currentThread().getPriority());
			thread.start();
			threads[ti-1] = thread;
		}

		doFiltering(ip, ipOut, lineRadii, cache, cacheWidth, cacheHeight, filterType, yForThread, 0);
		for (final Thread thread : threads){
			try {
				if (thread != null) 
					thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();	
			}
		}
		
	}

	
	// Filter a grayscale image or one channel of an RGB image using one thread
	//
	// Synchronization: unless a thread is waiting, we avoid the overhead of 'synchronized'
	// statements. That's because a thread waiting for another one should be rare.
	//
	// Data handling: The area needed for processing a line is written into the array 'cache'.
	// This is a stripe of sufficient width for all threads to have each thread processing one
	// line, and some extra space if one thread is finished to start the next line.
	// This array is padded at the edges of the image so that a surrounding with radius kRadius
	// for each pixel processed is within 'cache'. Out-of-image
	// pixels are set to the value of the nearest edge pixel. When adding a new line, the lines in
	// 'cache' are not shifted but rather the smaller array with the start and end pointers of the
	// kernel area is modified to point at the addresses for the next line.
	//
	// Algorithm: For mean and variance, except for very small radius, usually do not calculate the
	// sum over all pixels. This sum is calculated for the first pixel of every line only. For the
	// following pixels, add the new values and subtract those that are not in the sum any more.
	// For min/max, also first look at the new values, use their maximum if larger than the old
	// one. The look at the values not in the area any more; if it does not contain the old
	// maximum, leave the maximum unchanged. Otherwise, determine the maximum inside the area.
	// For outliers, calculate the median only if the pixel deviates by more than the threshold
	// from any pixel in the area. Therfore min or max is calculated; this is a much faster
	// operation than the median.
	private void doFiltering(GrayScaleImage ip, GrayScaleImage ipOut, int[] lineRadii, float[] cache, int cacheWidth, int cacheHeight, int filterType,  int [] yForThread, int threadNumber) {
		
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		
		int kHeight = kHeight(lineRadii);
		int kRadius	 = kRadius(lineRadii);
		int kNPoints = kNPoints(lineRadii);

		int xmin = - kRadius;
		int xmax = ip.getWidth() + kRadius;
		int[]cachePointers = makeCachePointers(lineRadii, cacheWidth);

		int padLeft = xmin<0 ? -xmin : 0;
		int padRight = xmax>width? xmax-width : 0;
		int xminInside = xmin>0 ? xmin : 0;
		int xmaxInside = xmax<width ? xmax : width;
		int widthInside = xmaxInside - xminInside;
		
		boolean minOrMax = filterType == MIN || filterType == MAX;
		boolean sumFilter = filterType == MEAN || filterType == VARIANCE;
		boolean medianFilter = filterType == MEDIAN;
		double[] sums = sumFilter ? new double[2] : null;
		float[] medianBuf1 = (medianFilter) ? new float[kNPoints] : null;
		float[] medianBuf2 = (medianFilter) ? new float[kNPoints] : null;
		float sign = filterType==MIN ? -1f : 1f;
		
		boolean smallKernel = kRadius < 2;

		//int[] pixels = ip.getPixels();
		
		float maxValue =  (float)ip.maxValue();
		float[] values = new float[ip.getWidth()];

		int numThreads = yForThread.length;
		long lastTime = System.currentTimeMillis();
		int previousY = kHeight/2-cacheHeight;
		
		
		while (true) {
			int y = arrayMax(yForThread) + 1;		// y of the next line that needs processing
			yForThread[threadNumber] = y;
			//IJ.log("thread "+threadNumber+" @y="+y+" needs"+(y-kHeight/2)+"-"+(y+kHeight/2)+" highestYinC="+highestYinCache);
			boolean threadFinished = y >= ip.getHeight();
			if (numThreads>1 && (threadWaiting || threadFinished))		// 'if' is not synchronized to avoid overhead
				synchronized(this) {
					notifyAll();					// we may have blocked another thread
					//IJ.log("thread "+threadNumber+" @y="+y+" notifying");
				}
			if (threadFinished)
				return;								// all done, break the loop

			if (threadNumber==0) {					// main thread checks for abort and ProgressBar
				long time = System.currentTimeMillis();
				if (time-lastTime>100) {
					lastTime = time;
					if (Thread.currentThread().isInterrupted() ) {
						synchronized(this) {notifyAll();}
						return;
					}
				}
			}
			
			for (int i=0; i<cachePointers.length; i++)	//shift kernel pointers to new line
				cachePointers[i] = (cachePointers[i] + cacheWidth * (y-previousY)) % cache.length;
			previousY = y;

			if (numThreads>1) {							// thread synchronization
				int slowestThreadY = arrayMinNonNegative(yForThread); // non-synchronized check to avoid overhead
				if (y - slowestThreadY + kHeight > cacheHeight) {	// we would overwrite data needed by another thread
					synchronized(this) {
						slowestThreadY = arrayMinNonNegative(yForThread); //recheck whether we have to wait
						if (y - slowestThreadY + kHeight > cacheHeight) {
							do {
								notifyAll();			// avoid deadlock: wake up others waiting
								threadWaiting = true;
								//IJ.log("Thread "+threadNumber+" waiting @y="+y+" slowest@y="+slowestThreadY);
								try {
									wait();
								} catch (InterruptedException e) {
									notifyAll();
									Thread.currentThread().interrupt(); //keep interrupted status (PlugInFilterRunner needs it)
									return;
								}
								slowestThreadY = arrayMinNonNegative(yForThread);
							} while (y - slowestThreadY + kHeight > cacheHeight);
						} //if
						threadWaiting = false;
					}
				}
			}

			if (numThreads==1) {															// R E A D
				int yStartReading = y ==0 ? Math.max(-kHeight/2, 0) : y+kHeight/2;
				for (int yNew = yStartReading; yNew<=y+kHeight/2; yNew++) { //only 1 line except at start
					readLineToCacheOrPad(ip, width, height, xminInside, widthInside, cache, cacheWidth, cacheHeight, padLeft, padRight, kHeight, yNew);
				}
			} else {
				if (!copyingToCache || highestYinCache < y+kHeight/2) synchronized(cache) {
					copyingToCache = true;				// copy new line(s) into cache
					while (highestYinCache < arrayMinNonNegative(yForThread) - kHeight/2 + cacheHeight - 1) {
						int yNew = highestYinCache + 1;
						readLineToCacheOrPad(ip, width, height, xminInside, widthInside,
							cache, cacheWidth, cacheHeight, padLeft, padRight, kHeight, yNew);
						highestYinCache = yNew;
					}
					copyingToCache = false;
				}
			}

			int cacheLineP = cacheWidth * (y % cacheHeight) + kRadius;	//points to pixel (roi.x, y)
			filterLine(values, width, cache, cachePointers, kNPoints, cacheLineP, y,	// F I L T E R
					sums, medianBuf1, medianBuf2, sign, maxValue, filterType,
					smallKernel, sumFilter, minOrMax);
			
			writeLineToPixels(values, ipOut, y*width, ip.getWidth());	// W R I T E
			
		} 
	}

	private int arrayMax(int[] array) {
		int max = Integer.MIN_VALUE;
		for (int i=0; i<array.length; i++)
			if (array[i] > max) max = array[i];
		return max;
	}

	//only checks non-negative numbers in the array. Returns Integer.MAX_VALUE if no such values.
	private int arrayMinNonNegative(int[] array) {
		int min = Integer.MAX_VALUE;
		for (int i=0; i<array.length; i++)
			if (array[i]>=0 && array[i]<min) min = array[i];
		return min;
	}
	
	
	private void filterLine(float[] values, int width, float[] cache, int[] cachePointers, int kNPoints, int cacheLineP, int y,
			double[] sums, float[] medianBuf1, float[] medianBuf2, float sign, float maxValue, int filterType,
			boolean smallKernel, boolean sumFilter, boolean minOrMax) {
			int valuesP = 0;
			float max = 0f;
			float median = Float.isNaN(cache[cacheLineP]) ? 0 : cache[cacheLineP];	// a first guess
			boolean fullCalculation = true;
			for (int x=0; x < width; x++, valuesP++) {							// x is with respect to roi.x
				if (fullCalculation) {
					fullCalculation = smallKernel;	//for small kernel, always use the full area, not incremental algorithm
					if (minOrMax){
						max = getAreaMax(cache, x, cachePointers, 0, -Float.MAX_VALUE, sign);
						values[valuesP] = max*sign;
						continue;
					}
					else if (sumFilter)
						getAreaSums(cache, x, cachePointers, sums);
				} else {
					if (minOrMax) {
						float newPointsMax = getSideMax(cache, x, cachePointers, true, sign);
						if (newPointsMax >= max) { //compare with previous maximum 'max'
							max = newPointsMax;
						} else {
							float removedPointsMax = getSideMax(cache, x, cachePointers, false, sign);
							if (removedPointsMax >= max)
								max = getAreaMax(cache, x, cachePointers, 1, newPointsMax, sign);
						}
						values[valuesP] = max*sign;
						continue;
						
					} else if (sumFilter) {
						addSideSums(cache, x, cachePointers, sums);
						if (Double.isNaN(sums[0])) //avoid perpetuating NaNs into remaining line
							fullCalculation = true;
					}
				}
				if (sumFilter) {
					if (filterType == MEAN)
						values[valuesP] = (float)(sums[0]/kNPoints);
					else	{// Variance: sum of squares - square of sums
						float value = (float)((sums[1] - sums[0]*sums[0]/kNPoints)/kNPoints);
						if (value>maxValue) value = maxValue;
						values[valuesP] = value;
					}
				} else if (filterType == MEDIAN) {
					median = getMedian(cache, x, cachePointers, medianBuf1, medianBuf2, kNPoints, median);
					values[valuesP] = median;
				} 
			} // for x
	}
	/** Get max (or -min if sign=-1) within the kernel area.
	 *	@param x between 0 and cacheWidth-1
	 *	@param ignoreRight should be 0 for analyzing all data or 1 for leaving out the row at the right
	 *	@param max should be -Float.MAX_VALUE or the smallest value the maximum can be */
	private static float getAreaMax(float[] cache, int xCache0, int[] kernel, int ignoreRight, float max, float sign) {
		for (int kk=0; kk<kernel.length; kk++) {	// y within the cache stripe (we have 2 kernel pointers per cache line)
			for (int p=kernel[kk++]+xCache0; p<=kernel[kk]+xCache0-ignoreRight; p++) {
				float v = cache[p]*sign;
				if (max < v) max = v;
			}
		}
		return max;
	}

	/** Get max (or -min if sign=-1) at the right border inside or left border outside the kernel area.
	 *	x between 0 and cacheWidth-1 */
	private static float getSideMax(float[] cache, int xCache0, int[] kernel, boolean isRight, float sign) {
		float max = -Float.MAX_VALUE;
		if (!isRight) xCache0--;
		for (int kk= isRight ? 1 : 0; kk<kernel.length; kk+=2) {	// y within the cache stripe (we have 2 kernel pointers per cache line)
			float v = cache[xCache0 + kernel[kk]]*sign;
			if (max < v) max = v;
		}
		return max;
	}
	
	
	/** Read a line into the cache (including padding in x).
	 *	If y>=height, instead of reading new data, it duplicates the line y=height-1.
	 *	If y==0, it also creates the data for y<0, as far as necessary, thus filling the cache with
	 *	more than one line (padding by duplicating the y=0 row).
	 */
	private static void readLineToCacheOrPad(GrayScaleImage img, int width, int height, int xminInside, int widthInside, float[]cache, int cacheWidth, int cacheHeight, int padLeft, int padRight, int kHeight, int y) {
		int lineInCache = y%cacheHeight;
		if (y < height) {
			readLineToCache(img, y*width, xminInside, widthInside, cache, lineInCache*cacheWidth, padLeft, padRight);
			if (y==0) for (int prevY = -kHeight/2; prevY<0; prevY++) {	//for y<0, pad with y=0 border pixels 
				int prevLineInCache = cacheHeight+prevY;
				System.arraycopy(cache, 0, cache, prevLineInCache*cacheWidth, cacheWidth);
			}
		} else
			System.arraycopy(cache, cacheWidth*((height-1)%cacheHeight), cache, lineInCache*cacheWidth, cacheWidth);
	}

	/** Read a line into the cache (includes conversion to flaot). Pad with edge pixels in x if necessary */
	private static void readLineToCache(GrayScaleImage img, int pixelLineP, int xminInside, int widthInside, float[] cache, int cacheLineP, int padLeft, int padRight) {
		for (int pp=pixelLineP+xminInside, cp=cacheLineP+padLeft; pp<pixelLineP+xminInside+widthInside; pp++,cp++)
			cache[cp] = img.getPixel(pp);// & 0xff;
		for (int cp=cacheLineP; cp<cacheLineP+padLeft; cp++)
			cache[cp] = cache[cacheLineP+padLeft];
		for (int cp=cacheLineP+padLeft+widthInside; cp<cacheLineP+padLeft+widthInside+padRight; cp++)
			cache[cp] = cache[cacheLineP+padLeft+widthInside-1];
	}

	/** Write a line to pixels arrax, converting from float (not for float data!)
	 *	No checking for overflow/underflow
	 */
	private static void writeLineToPixels(float[] values, GrayScaleImage ipOut, int pixelP, int length) {
		for (int i=0, p=pixelP; i<length; i++,p++)
			ipOut.setPixel(p, (int) (values[i] + 0.5f));
		
	}



	/** Get sum of values and values squared within the kernel area.
	 *	x between 0 and cacheWidth-1
	 *	Output is written to array sums[0] = sum; sums[1] = sum of squares */
	private static void getAreaSums(float[] cache, int xCache0, int[] kernel, double[] sums) {
		double sum=0, sum2=0;
		for (int kk=0; kk<kernel.length; kk++) {	// y within the cache stripe (we have 2 kernel pointers per cache line)
			for (int p=kernel[kk++]+xCache0; p<=kernel[kk]+xCache0; p++) {
				float v = cache[p];
				sum += v;
				sum2 += v*v;
			}
		}
		sums[0] = sum;
		sums[1] = sum2;
		return;
	}

	/** Add all values and values squared at the right border inside minus at the left border outside the kernal area.
	 *	Output is added or subtracted to/from array sums[0] += sum; sums[1] += sum of squares  when at 
	 *	the right border, minus when at the left border */
	private static void addSideSums(float[] cache, int xCache0, int[] kernel, double[] sums) {
		double sum=0, sum2=0;
		for (int kk=0; kk<kernel.length; /*k++;k++ below*/) {
			float v = cache[kernel[kk++]+(xCache0-1)];
			sum -= v;
			sum2 -= v*v;
			v = cache[kernel[kk++]+xCache0];
			sum += v;
			sum2 += v*v;
		}
		sums[0] += sum;
		sums[1] += sum2;
		return;
	}

	/** Get median of values within kernel-sized neighborhood. Kernel size kNPoints should be odd.
	 */
	private static float getMedian(float[] cache, int xCache0, int[] kernel,
			float[] aboveBuf, float[]belowBuf, int kNPoints, float guess) {
		int nAbove = 0, nBelow = 0;
		for (int kk=0; kk<kernel.length; kk++) {
			for (int p=kernel[kk++]+xCache0; p<=kernel[kk]+xCache0; p++) {
				float v = cache[p];
				if (v > guess) {
					aboveBuf[nAbove] = v;
					nAbove++;
				}
				else if (v < guess) {
					belowBuf[nBelow] = v;
					nBelow++;
				}
			}
		}
		int half = kNPoints/2;
		if (nAbove>half)
			return findNthLowestNumber(aboveBuf, nAbove, nAbove-half-1);
		else if (nBelow>half)
			return findNthLowestNumber(belowBuf, nBelow, half);
		else
			return guess;
	}

	/** Find the n-th lowest number in part of an array
	 *	@param buf The input array. Only values 0 ... bufLength are read. <code>buf</code> will be modified.
	 *	@param bufLength Number of values in <code>buf</code> that should be read
	 *	@param n which value should be found; n=0 for the lowest, n=bufLength-1 for the highest
	 *	@return the value */
	public final static float findNthLowestNumber(float[] buf, int bufLength, int n) {
		// Hoare's find, algorithm, based on http://www.geocities.com/zabrodskyvlada/3alg.html
		// Contributed by Heinz Klar
		int i,j;
		int l=0;
		int m=bufLength-1;
		float med=buf[n];
		float dum ;

		while (l<m) {
			i=l ;
			j=m ;
			do {
				while (buf[i]<med) i++ ;
				while (med<buf[j]) j-- ;
				dum=buf[j];
				buf[j]=buf[i];
				buf[i]=dum;
				i++ ; j-- ;
			} while ((j>=n) && (i<=n)) ;
			if (j<n) l=i ;
			if (n<i) m=j ;
			med=buf[n] ;
		}
	return med ;
	}



	protected int[] makeLineRadii(AdjacencyRelation adj){
		int vetX[] = adj.getVectorX();
		int vetY[] = adj.getVectorY();
		int numPontos = vetX.length;
		
		int x2 = ArrayUtils.maxValue(vetX);
		int y1 = ArrayUtils.minValue(vetY);
		int y2 = ArrayUtils.maxValue(vetY);
		
		int kernel[] = new int[((y2 - y1 + 1)*2) +2 ];
		int minX, maxX;
		int k=0;
		for(int y=y1; y<= y2; y++){
			minX = maxX = Integer.MAX_VALUE;
			for(int i=0; i<vetY.length; i++){
				if(vetY[i] == y){
					if(minX == Integer.MAX_VALUE){
						minX = maxX = vetX[i];
					}
					if(minX > vetX[i]){
						minX = vetX[i];
					}
					if(maxX < vetX[i]){
						maxX = vetX[i];
					}
				}
			}
			kernel[k++] = minX;
			kernel[k++] = maxX;
		}
			
		kernel[kernel.length-1] = x2;
		kernel[kernel.length-2] = numPontos; 
		return kernel;
	}
	

	/** Create a circular kernel (structuring element) of a given radius.
	 *	@param radius:
	 *	Radius = 0.5 includes the 4 neighbors of the pixel in the center,
	 *	radius = 1 corresponds to a 3x3 kernel size.
	 *	@param width: width of the roi (or image if roi width=image width) for filtering.
	 *	@return:
	 *	The output is an array that gives the length of each line of the structuring element
	 *	(kernel) to the left (negative) and to the right (positive):
	 *	[0] left in line 0, [1] right in line 0,
	 *	[2] left in line 2, ...
	 *	The maximum (absolute) value should be kernelRadius.
	 *	Array elements at the end:
	 *	length-2: nPoints, number of pixels in the kernel area
	 *	length-1: kernelRadius in x direction (kernel width is 2*kernelRadius+1)
	 *	Kernel height can be calculated as (array length - 1)/2 (odd number);
	 *	Kernel radius in y direction is kernel height/2 (truncating integer division).
	 *	Note that kernel width and height are the same for the circular kernels used here,
	 *	but treated separately for the case of future extensions with non-circular kernels.
	 */
	protected int[] makeLineRadiiOLD(double radius) {
		if (radius>=1.5 && radius<1.75) //this code creates the same sizes as the previous RankFilters
			radius = 1.75;
		else if (radius>=2.5 && radius<2.85)
			radius = 2.85;
		int r2 = (int) (radius*radius) + 1;
		int kRadius = (int)(Math.sqrt(r2+1e-10));
		int kHeight = 2*kRadius + 1;
		int[] kernel = new int[2*kHeight + 2];
		kernel[2*kRadius]	= -kRadius;
		kernel[2*kRadius+1] =  kRadius;
		int nPoints = 2*kRadius+1;
		for (int y=1; y<=kRadius; y++) {		//lines above and below center together
			int dx = (int)(Math.sqrt(r2-y*y+1e-10));
			kernel[2*(kRadius-y)]	= -dx;
			kernel[2*(kRadius-y)+1] =  dx;
			kernel[2*(kRadius+y)]	= -dx;
			kernel[2*(kRadius+y)+1] =  dx;
			nPoints += 4*dx+2;	//2*dx+1 for each line, above&below
		}
		kernel[kernel.length-2] = nPoints;
		kernel[kernel.length-1] = kRadius;
		//for (int i=0; i<kHeight;i++)IJ.log(i+": "+kernel[2*i]+"-"+kernel[2*i+1]);
		return kernel;
	}
	
	public static void main(String args[]){
		RankFilters r = new RankFilters();
		float raio = 5f;
		int radii[] = r.makeLineRadiiOLD(raio);
		System.out.println("Altura:" + r.kHeight(radii));
		System.out.println("Largura:" + r.kRadius(radii));
		System.out.println("NumPontos:" + r.kNPoints(radii));
		//int vet[] = r.makeCachePointers(radii, 10);
		for(int i=0; i < radii.length-2; i++){
			System.out.println("Linha: "+radii[i++] + " ate " + radii[i]);
		}
		
		System.out.println();
		
		AdjacencyRelation adj = AdjacencyRelation.getFastCircular(5.2);
		int vetX[] = adj.getVectorX();
		int vetY[] = adj.getVectorY();
		int numPontos = vetX.length;
		
		int x2 = ArrayUtils.maxValue(vetX);
		int y1 = ArrayUtils.minValue(vetY);
		int y2 = ArrayUtils.maxValue(vetY);
		
		int kernel[] = new int[((y2 - y1 + 1)*2) +2 ];
		
		int minX, maxX;
		int k=0;
		for(int y=y1; y<= y2; y++){
			minX = maxX = Integer.MAX_VALUE;
			for(int i=0; i<vetY.length; i++){
				if(vetY[i] == y){
					if(minX == Integer.MAX_VALUE){
						minX = maxX = vetX[i];
					}
					if(minX > vetX[i]){
						minX = vetX[i];
					}
					if(maxX < vetX[i]){
						maxX = vetX[i];
					}
				}
			}
			kernel[k++] = minX;
			kernel[k++] = maxX;
			System.out.println("Linha:" + minX + " ate " + maxX);
			
		}
			
		System.out.println("Altura:" + (y2 - y1 + 1));
		System.out.println("Largura:" + x2);
		System.out.println("NumPontos:" + numPontos);
		
		kernel[kernel.length-1] = x2;
		kernel[kernel.length-2] = numPontos; 
		
		System.out.println(Arrays.equals(kernel, radii));
		/*for(int i=0; i < kernel.length; i++){
			System.out.println(kernel[i] + " = " + radii[i]);	
		}*/
		
		
		
	}
	
	//kernel height
	private int kHeight(int[] lineRadii) {
		return (lineRadii.length-2)/2;
	}

	//kernel radius in x direction. width is 2+kRadius+1
	private int kRadius(int[] lineRadii) {
		return lineRadii[lineRadii.length-1];
	}
	
	//number of points in kernal area
	private int kNPoints(int[] lineRadii) {
		return lineRadii[lineRadii.length-2];
	}

	//cache pointers for a given kernel
	private int[] makeCachePointers(int[] lineRadii, int cacheWidth) {
		int kRadius = kRadius(lineRadii);
		int kHeight = kHeight(lineRadii);
		int[] cachePointers = new int[2*kHeight];
		for (int i=0; i<kHeight; i++) {
			cachePointers[2*i]	 = i*cacheWidth+kRadius + lineRadii[2*i];
			cachePointers[2*i+1] = i*cacheWidth+kRadius + lineRadii[2*i+1];
		}
		return cachePointers;
	}

	

}
