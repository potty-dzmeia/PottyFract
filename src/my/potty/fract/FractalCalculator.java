package my.potty.fract;

public abstract class FractalCalculator 
{
    
	public static enum FractalTypes{MANDELBROT, JULIA}
	
	
	/** Returns a object of the type Fractal with which we will be drawing different types of
	 *  fractals.
	 * 
	 * @param type - the type of fractal we wish to calculate
	 * @return -  Instance to this object
	 */
	public static FractalCalculator getInstance(FractalTypes type)
	{
		if( FractalTypes.MANDELBROT.ordinal() == type.ordinal() )
			return new Mandelbrot();
		if( FractalTypes.JULIA.ordinal() == type.ordinal() )
			return new JuliaSet();
		
		// If unknown set, return Mandelbrot
		return new Mandelbrot();
	}
	
	/** Function which tells us if a certain point in the complex plain, belongs to a given set
	 *  or not. If it belongs the returned value will be equal to the input value of maxIteration;
	 *  Else the return value tells us how fast the orbit is getting away to infinity.
	 *  
	 * @param x0 - Real part of the point in the complex plain
	 * @param y0 - The imaginary part of the point 
	 * @param maxIteration - How many numbers of times we want [Zn+1 =Fn(Zn)] to be calculated
	 * @return - The number of iteration it took for absolute(Zn) to get above certain boundary
	 */
	 public abstract int testPoint(double x0, double y0, int maxIteration);
	 /** Functions that gives us the orbit of a certain point from the complex plain.
	  *  Orbit is the values that we get on each iteration of the equation:
	  *  Zn+1 = fn(Zn)
	  * 
	  * @param x0 - The real part (Re) of the point in the complex plain
      * @param y0 - The imaginary part (Im) of the point 
	  * @param path - Array that will hold calculated values of Zn. Because Zn is composed
	  *				 of two real numbers. The number of values of Zn this function will calculate
	  *				 will be path.size()/2;
	  * @return - The number of iteration it took for absolute(Zn) to get above certain boundary
	  */
	  public abstract int getPath(double x0, double y0, double[] path);
	  
	  /** Sets the C into the following formula Z[n+1] = Z[n]^2+C. Note that C is a complex number.
	   *  
	   * @param Re  - Real part of the C
	   * @param Img - Imaginary part of C 
	   * @return 
	   */
	  public abstract void setConstant(double Re, double Img);
}
