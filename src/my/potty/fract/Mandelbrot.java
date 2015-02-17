package my.potty.fract;

/** Mandelbrot class which checks if a point belongs to the Mandelbrot set or not.
 *  It also calculates the orbit of each point of the complex plain.
 *  
 *  "The" Mandelbrot set is the set obtained from the quadratic recurrence equation:
 *  
 *  Zn+1 = squared(Zn)+C, 
 *  
 *  where Z0 = C; where points C in the complex plane for which the orbit of Zn does 
 *  not tend to infinity are in the set.
 *  
 *  
 * @author Pottry
 *
 */
 class Mandelbrot extends FractalCalculator  {
	
	/** Functions that gives us the orbit of a certain point from the complex plain.
	 *  Orbit is the values that we get on each iteration of the equation:
	 *  Zn+1 = squared(Zn)+C.
	 * 
	 * @param x0 - The real part (Re) of the point in the complex plain
	 * @param y0 - The imaginary part (Im) of the point 
	 * @param path - Array that will hold calculated values of Zn. Because Zn is composed
	 *				 of two real numbers. The number of values of Zn this function will calculate
	 *				 will be path.size()/2;
	 * @return - The number of iteration it took for absolute(Zn) to get above 2
	 */
	@Override
	public int getPath(double x0, double y0, double[] path)
	{
		double xtemp;
		double x = x0;
		double y = y0;
		int iteration = 0;
		int maxIterations = path.length/2;
		

		while ( (x*x + y*y <= 4) &&  (iteration < maxIterations) )
		{
			
			xtemp = x*x - y*y + x0;
			y = 2*x*y + y0;	
		    x = xtemp;

		    path[iteration*2] = x;
		    path[iteration*2+1] = y;
		    
		    iteration = iteration + 1;
		}
		
		return iteration;
	}
	
	
	/** Function which tells us if a certain point in the complex plain, belongs to the Mandelbrot
	 *  set or not. If it belongs the return value will be equal to the input value of maxIteration;
	 *  
	 * 
	 * @param x0 - Real part of the point in the complex plain
	 * @param y0 - The imaginary part of the point 
	 * @param maxIteration - How many numbers of times we want [Zn+1 = squared(Zn)+C] to be calculated
	 * @return - The number of iteration it took for absolute(Zn) to get above 2
	 */
	@Override
	public int testPoint(double x0, double y0, int maxIteration)
	{
		double xtemp;
		double x = x0;
		double y = y0;
		int iteration = 0;
			
		// If the absolute value of Zn ever becomes larger than 2, the sequence 
		// will escape to infinity, which means that the point is not part of the
		// Mandelbrot set and we should stop iterating.
		while ( (x*x + y*y <= 4) &&  (iteration < maxIteration) )
		{
			// Calculate Squared(Zn)
			xtemp = x*x - y*y + x0;
			// Calculate Squared(Zn)+ Zn-1
			y = 2*x*y + y0;	
			x = xtemp;

			// Mark that iteration was done
		    iteration++;
		}
			
		// Return the number of iterations done
		return iteration;
	}//isMandelbrotPoint


	@Override
	public void setConstant(double Re, double Img) {
		// nothing to do
		
	}

}
