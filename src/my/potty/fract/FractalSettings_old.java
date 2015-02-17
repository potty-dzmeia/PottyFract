package my.potty.fract;

import my.potty.fract.FractalCalculator.FractalTypes;

/** Class that takes care of the fractal settings.
 * 
 *  Here we have the location we are viewing at, the magnification, type of fractal we want
 *  to draw and many more characteristics.
 *  
 *  The class works with two types of coordinates:
 *  1. Window coordinates - Coordinates of the drawing area (main View) (e.g. 480x800)
 *  2. Complex coordinates - Coordinates of the 2d Euclidian plain - for example 0.0004x0.0002
 * 
 *  Make sure when calling functions that the appropriate coordinates are used!
 *  
 * @author Pottry
 *
 */
public class FractalSettings_old {
	
    public static final double INITIAL_ZOOM = 4; // Initial Magnification of the image
	public static final int    INITIAL_ITER_LIMMIT = 20;
	public static final int    INITIAL_CLR_PER = 1;
	
	public  int        iDrawAreaWidth, iDrawAreaHeight;// In drawing coordinates
	public  int        iterationsLimmit;   // The maximum number of iterations for a point in the Mandelbrot set
	
	private double	   dAspectRatio;	   // The width/height of the drawing area 
	private double 	   dOrbitPointX, dOrbitPointY;	   // The coords of the point which orbits should be drawn(in 2D complex plain coordinates)
	private double	   dCenterXOfComplexPlain, dCenterYOfComplexPlain; // Determines how the 2D complex coordinate system is centered onto the display (in 2D complex plain coordinates)
	private double 	   dXRange, dYRange;   // This is the zoom factor. It determines what portion of the 2D complex plain is drawn onto the display(in 2D complex plain coordinates)
	private double     dComplexConstantRe = 0.233;     // Constant that will be used when drawing a custom fractal - real part
	private double	   dComplexConstantIm = 0.53780;   // Constant that will be used when drawing a custom fractal - Imaginary 
	private ColorMode  colorMode = ColorMode.COLOR;    // The current color mode 
	private	FractalTypes   fractalType; // The type of fractal that is drawn
	private int 	iColorPeriodicity = INITIAL_CLR_PER; // A variable specifying how often should a color repeat when we have selected Periodic color option (see currentColor for more info)
	private boolean bOrbitMode    = false; // Indicating if we should draw the orbit of a certain point in the complex plain.
	private boolean bAntiAliasing = false; // If anti aliasing is ON.
	


	
	/** Color options for drawing the fractals
	 */
	public static enum ColorMode{COLOR, COLOR_INVERTED, BW;}
	
	
	/** Creates an object which will hold all the coordinates need for drawing a fractal and the orbit of a certain point
	 * 
	 * @param iDrawAreaWidth - the width of the drawing area
	 * @param iDrawAreaHeight - the height of the drawing area
	 */
	public FractalSettings_old(int width, int height)
	{
		// Get the size of the drawing area
		this.iDrawAreaWidth  = width;
		this.iDrawAreaHeight = height;
		
		// Calculate the aspect ratio
		dAspectRatio = (double)iDrawAreaWidth/iDrawAreaHeight;
		
		//Set the default view of the Mandelbrot set so that in the beginning 
		//the whole fractal is seen
		dCenterXOfComplexPlain = 0f;
		dCenterYOfComplexPlain = 0f;
		dYRange 	= INITIAL_ZOOM;	
		dXRange		= INITIAL_ZOOM*dAspectRatio;
		
		iterationsLimmit = INITIAL_ITER_LIMMIT; // Number of iterations that each point is checked if contained into the set
		
		fractalType = FractalTypes.MANDELBROT; // Set the initial fractal type to Mandelbrot
	}
	
	
	@Override
    public FractalSettings_old clone()
	{
	    FractalSettings_old clonedObject = new FractalSettings_old(iDrawAreaWidth, iDrawAreaHeight);
	    clonedObject.bAntiAliasing = bAntiAliasing;
	    clonedObject.bOrbitMode    = bOrbitMode;
	    clonedObject.colorMode     = colorMode;
	    clonedObject.dAspectRatio  = dAspectRatio;
	    clonedObject.dCenterXOfComplexPlain = dCenterXOfComplexPlain;
	    clonedObject.dCenterYOfComplexPlain = dCenterYOfComplexPlain;
	    clonedObject.dComplexConstantIm = dComplexConstantIm;
	    clonedObject.dComplexConstantRe = dComplexConstantRe;
	    clonedObject.dOrbitPointX  = dOrbitPointX;
	    clonedObject.dOrbitPointY  = dOrbitPointY;
	    clonedObject.dXRange       = dXRange;
	    clonedObject.dYRange       = dYRange;
	    clonedObject.fractalType   = fractalType;
	    clonedObject.iColorPeriodicity = iColorPeriodicity;
	    clonedObject.iterationsLimmit  = iterationsLimmit;
	    
        return clonedObject;
	    
	}
	
	/**Converts complex plain coordinates to drawing coordinates. 
	 * 
	 * @param x - Coordinate of the point onto the 2D complex plain. For example -0.000123
	 * @return - Corresponding drawing coordinate of the point. For example 332.
	 */
	synchronized public int convertXtoDrawingCoordinates(double x)
	{
		int output;
		
		output = (int) ((x+dXRange/2 - dCenterXOfComplexPlain)*iDrawAreaWidth/dXRange + 0.5);
						  
		return output;
	}
	/**Converts complex plain coordinates to corresponding drawing coordinates.
	 * 
	 * @param y - Coordinate of the point onto the 2D complex plain. For example -0.000123
	 * @return - Corresponding drawing coordinate of the point. For example 332
	 */
	synchronized public int convertYtoDrawingCoordinates(double y)
	{
		int output;
		
		output = (int) ((y+dYRange/2f - dCenterYOfComplexPlain)*iDrawAreaHeight/dYRange  + 0.5); 
					
		return output;
	}
	
	/**Converts drawing coordinates to corresponding complex plain coordinates. 
	 * 
	 * @param x - Corresponding drawing coordinate of the point. For example 332 
	 * @return - Coordinate of the point onto the 2D complex plain. For example -0.000123
	 */
	synchronized public double convertXtoComplexCoordinates(double x)
	{
		double output;
		// output = center + X - (half the horizontal range) 
		output = dCenterXOfComplexPlain + dXRange*x/iDrawAreaWidth-dXRange/2;
					
		return output;
	}
	/**Converts drawing coordinates to corresponding complex plain coordinates. 
	 * 
	 * @param y - Corresponding drawing coordinate of the point. For example 332 
	 * @return - Coordinate of the point onto the 2D complex plain. For example -0.000123
	 */
	synchronized public double convertYtoComplexCoordinates(double y)
	{
		double output;
		
		output = dCenterYOfComplexPlain + dYRange*y/iDrawAreaHeight-dYRange/2;
					
		return output;
	}
	/**Zooms into the fractal.
	 * 
	 * @param fMagnitude - the amount of zoom wanted. The viewing range is divided by the fMagnitude.
	 * For example, if we set fMagnitude=3, the image will be 3 times bigger.
	 */
	public synchronized void doZoom(double dMagnitude)
	{
		//Reduce the viewing range by the desired factor
		dXRange /= dMagnitude;
		dYRange /= dMagnitude;
	}
	
	/** Set's the range of the complex plain that we are viewing to some specific value
	 * 
	 * @param dMagnification - the magnification we want (this is relative to the starting
	 * 						   magnification we have at the beginning).
	 */
	public synchronized void setMagnification(double dMagnification)
	{
		// Set the viewing range by the desired factor
		dXRange = INITIAL_ZOOM*dAspectRatio/dMagnification;
		dYRange = INITIAL_ZOOM/dMagnification;
	} 
	
	/**Moves the fractal
	 * 
	 * @param dX - The size of the horizontal move in percentage. For example -0.1 will move the fractal
	 * by 10percent of the current view range to the LEFT.
	 * @param dY - The size of the vertical move in percentage. For example -0.1 will move the fractal
	 * by 10percent of the current view range UP.
	 */
	synchronized public void moveFractal(double dX, double dY)
	{
		dCenterXOfComplexPlain +=dXRange*dX;
		dCenterYOfComplexPlain +=dYRange*dY;
	}
	/** Set orbit of which point should be drawn.
	 * 
	 * @param x - location of the point that we are interested in (drawing coordinates)
	 * @param y - location of the point that we are interested in (drawing coordinates)
	 */
	synchronized public void setOrbitPoint(double x, double y)
	{
		dOrbitPointX = dCenterXOfComplexPlain + dXRange*x/iDrawAreaWidth - dXRange/2;
		dOrbitPointY = dCenterYOfComplexPlain + dYRange*y/iDrawAreaHeight - dYRange/2;
		
	}
	
	//
	synchronized public double getXRangeOfComplexPlain()
	{
		return dXRange;
	}
	synchronized public double getYRangeOfComplexPlain()
	{
		return dYRange;
	}
	
	/** Returns at which point of the 2d complex coordinate system our view is centered 
	 * 
	 * @return the X value of the point 
	 */
	synchronized public double getCenterXOfComplexPlain()
	{
		return dCenterXOfComplexPlain;
	}
	/** Returns at which point of the 2d complex coordinate system our view is centered 
	 * 
	 * @return the Y value of the point 
	 */
	synchronized public double getCenterYOfComplexPlain()
	{
		return dCenterYOfComplexPlain;
	}

	
	/**
	 *  Sets which part of the complex plain will be in the center of the graphic display - X coordinate
	 * @param input - Accepts coordinates of the drawing area
	 */
	synchronized public void setCenterXOfComplexPlain(double input)
	{
		dCenterXOfComplexPlain =   convertXtoComplexCoordinates(input);
	}
	/**
	 * Sets which part of the complex plain will be in the center of the graphic display - Y coordinate
	 * @param input -  Accepts coordinates of the drawing area
	 */
	synchronized public void setCenterYOfComplexPlain(double input)
	{
		dCenterYOfComplexPlain =  convertYtoComplexCoordinates(input);
	}
	
	
	/** Same as setCenterXOfComplexPlain, only that this function accepts complex coordinates
	 * 
	 * @param input - Accepts complex coordinates - the real part
	 */
	synchronized public void setCenterXOfComplexPlain_(double input)
	{
		dCenterXOfComplexPlain =   input;
	}
	/** Same as setCenterYOfComplexPlain, only that this function accepts complex coordinates
	 * 
	 * @param input - Accepts complex coordinates, the imaginary part
	 */
	synchronized public void setCenterYOfComplexPlain_(double input)
	{
		dCenterYOfComplexPlain =  input;
	}
	
	
	synchronized public double getOrbitPointX()
	{
		return dOrbitPointX;
	}
	synchronized public double getOrbitPointY()
	{
		return dOrbitPointY;
	}
	synchronized public double getAspectRatioOfDrawingArea()
	{
		return dAspectRatio;
	}

	/** Returns the maximum number of iteration for each point
	 * 
	 * @return
	 */
	public synchronized int getIterationsLimmit() 
	{
		return iterationsLimmit;
	}

	public synchronized void setIterationsLimmit(int iMaxDepth) 
	{
		this.iterationsLimmit = iMaxDepth;
	}
	
	public synchronized double getZoomFactor()
	{
		return INITIAL_ZOOM/dYRange;
	}
	
	
	
	/** Reset the view so we can see the whole fractal. 
	 * 
	 * @param type - the type of fractal we want to see( Julia, Mandelbrot etc...);
	 */
	public synchronized void reset(FractalTypes type)
	{
		//Set the default view of the Mandelbrot set so that in the beginning 
		//the whole fractal is seen
		dCenterXOfComplexPlain = 0f;
		dCenterYOfComplexPlain = 0f;
		dYRange = INITIAL_ZOOM;	
		dXRange	= INITIAL_ZOOM*dAspectRatio;
		
		iterationsLimmit  = INITIAL_ITER_LIMMIT;  // Number of iterations that each point is checked if contained into the set
		fractalType 	  = type;// Reset with new fractal type
		iColorPeriodicity = INITIAL_CLR_PER;	 // Set the color periodicity to 1
		colorMode		  = FractalSettings_old.ColorMode.COLOR; // Set the color option to "Color"
	}
	
	/** Sets the Fractal type that is to be drawn.
	 * 
	 * @param iNumber fractal type - check the class FractalType for different types
	 */
	public synchronized void setFractalType(FractalTypes type)
	{
	    fractalType = type; // Save new fractal type
	}
	/** Returns the Fractal type that should be drawn (Mandelbrot, Julia etc.)
	 * 
	 * @return - the type of fractal to be drawn.
	 */
	public synchronized FractalTypes getFractalType()
	{
		return fractalType;
	}
	
	/** Set the real part of C that will be used for the following equation Z[n+1]=Z[n]^2+C.
	 *  Warning! This function should be called before setFractalType() so that the
	 *  changes will affect the creation of the custom fractal type.
	 * 
	 * @param Re - C is complex number and this is the real part
	 */
	public synchronized void setImaginaryConstantRe(double Re)
	{
		dComplexConstantRe = Re;
	}
	/** Set the imaginary part of C that will be used for the following equation Z[n+1]=Z[n]^2+C.
	 *  Warning! This function should be called before setFractalType() so that the
	 *  changes will affect the creation of the custom fractal type.
	 * 
	 * @param Im - C is complex number and this is the imaginary part
	 */
	public synchronized void setImaginaryConstantIm(double Im)
	{  
		dComplexConstantIm = Im;
	}
	
		
	/** Gets the C that will be used for the following equation Z[n+1]=Z[n]^2+C
	 * 
	 * @return - Returns the real part of the number
	 */
	public synchronized double getImaginaryConstantRe()
	{
		return dComplexConstantRe;
	}
	/** Gets the C that will be used for the following equation Z[n+1]=Z[n]^2+C
	 * 
	 * @return - Returns the Imaginary part of the number
	 */
	public synchronized double getImaginaryConstantIm()
	{
		return dComplexConstantIm;
	}
	
	
	public synchronized FractalSettings_old.ColorMode getCurrentColor() 
	{
		return colorMode;
	}

	public synchronized void setCurrentColor(FractalSettings_old.ColorMode currentColor) 
	{
		this.colorMode = currentColor;
	}
	
	public synchronized int getColorPeriodicity() {
		return iColorPeriodicity;
	}


	public synchronized void setColorPeriodicity(int colorPeriodicity) {
		if(colorPeriodicity <= 0)
			this.iColorPeriodicity = 1;
		else
			this.iColorPeriodicity = colorPeriodicity;
	}
	
	
	public synchronized boolean isOrbitMode() {
		return bOrbitMode;
	}

	public synchronized void setAntiAliasing(boolean isEnabled) 
	{   
		this.bAntiAliasing = isEnabled;		
	}
	
	public synchronized boolean getAntiAliasing() 
	{
	    return bAntiAliasing;
	}
	
	
	public synchronized void setOrbitMode(boolean bIsOrbitMode)
	{
	    bOrbitMode = bIsOrbitMode;
	}
	
	
	
	
}
