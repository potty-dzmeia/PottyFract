package my.potty.fract;


/** CustomFractal class which uses the formula Z[n+1]=Z[n]^2+C, where C is entered by the user
 *  of the class thru the constructor
 *  It also calculates the orbit of each point of the complex plain.
 *  
 *  "The" JuliaSet set is the set obtained from the quadratic recurrence equation:
 *  
 *  
 * @author Pottry
 *
 */
class JuliaSet extends FractalCalculator
{
	
	// Default values for C
	private double cRe = 0.233; 
	private double cIm = 0.53780;
	
	/** Functions that gives us the orbit of a certain point from the complex plain.
	 *  Orbit is the values that we get on each iteration of the equation:
	 *  Z[n+1] = Z[n]^2+C.
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
			
			xtemp = x*x - y*y + cRe;
			y = 2*x*y + cIm;	
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
	public  int testPoint(double x0, double y0, int maxIteration)
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
			xtemp = x*x - y*y + cRe;
			// Calculate Squared(Zn)+ Zn-1
			y = 2*x*y  + cIm;	
			x = xtemp;

			// Mark that iteration was done
		    iteration++;
		}
			
		// Return the number of iterations done
		return iteration;
	}//isMandelbrotPoint


	@Override
	public void setConstant(double Re, double Img) 
	{
		this.cIm = Img;
		this.cRe = Re;
		
	}
	
	

}
