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
public class FractalSettings 
{
	
    public static final double INITIAL_ZOOM = 4; // Initial Magnification of the image
	public static final int    INITIAL_ITER_LIMMIT = 20;
	public static final int    INITIAL_CLR_PER = 1;
	
	/**Color options for drawing the fractals*/
	public static enum ColorMode{COLOR, COLOR_INVERTED, BW;}
	
	
	/**
	 * Dimensions of the window (drawing area) 
	 */
	public int width, height; 
	
	/**
	 * The maximum number of iterations for a point in the Mandelbrot set
	 */
	public int iterationsLimmit;
	
	private WindowCoords drawingAreaCoords; // Maps pixels to Cartesian coordinates(real numbers)
	private ColorMode    colorMode = ColorMode.COLOR;    // The current color mode 
	private	FractalTypes fractalType; 		// The type of fractal that is drawn
	
	private double	dAspectRatio;	   // The width/height of the drawing area 
	private double 	dOrbitPointX, dOrbitPointY;	   // The coordinates of the point which orbits should be drawn(in Window coordinates)
	private double  dComplexConstantRe = 0.233;     // Constant that will be used when drawing a custom fractal - real part
	private double  dComplexConstantIm = 0.53780;   // Constant that will be used when drawing a custom fractal - Imaginary 
	private int 	iColorPeriodicity = INITIAL_CLR_PER; // A variable specifying how often should a color repeat when we have selected Periodic color option (see currentColor for more info)
	private boolean bOrbitMode    	  = false; // Indicating if we should draw the orbit of a certain point in the complex plain.
	private boolean bIsOrbitVisible   = false; // Indicates if the orbit should be drawn onto the display


	
	


	/** Creates an object which will hold all the coordinates need for drawing a fractal and the orbit of a certain point
	 * 
	 * @param iDrawAreaWidth - the width of the drawing area
	 * @param iDrawAreaHeight - the height of the drawing area
	 */
	public FractalSettings(int width, int height)
	{
		init(width, height);
	}


	private void init(int inputWidth, int inputHeight) {
		this.width = inputWidth;
		this.height = inputHeight;
		dAspectRatio = (double)width/height; // Calculate the aspect ratio
		
		try {
			drawingAreaCoords = new WindowCoords(width, height); 
			drawingAreaCoords.setDimensions(-2*dAspectRatio, -2, // lower left corner
									        2*dAspectRatio,  2  );// upper right corner
		} catch (Exception e) {
			e.printStackTrace();
		}  
		
		iterationsLimmit  	= INITIAL_ITER_LIMMIT; // Number of iterations that each point is checked if contained into the set
		fractalType 	  	= FractalTypes.MANDELBROT; // Set the initial fractal type to Mandelbrot
		iColorPeriodicity 	= INITIAL_CLR_PER;
		colorMode 			= ColorMode.COLOR; 
		dOrbitPointX 		= 0;
		dOrbitPointY		= 0;
		
		bIsOrbitVisible = false;
		bOrbitMode 		= false;
	}

	
	public synchronized double getRealCoord_X(int x, int y)
	{
		return drawingAreaCoords.getCartCoord_X(x, y); 
	}
	
	public synchronized double getRealCoord_Y(int x, int y)
	{
		return drawingAreaCoords.getCartCoord_Y(x, y);
	}
	
	public long getWindowCoord_X(double x, double y)
	{
		return drawingAreaCoords.getWindowCoord_X(x, y);
	}
	    
	public long getWindowCoord_Y(double x, double y)
	{
		return drawingAreaCoords.getWindowCoord_Y(x, y);
	}
	    
	    
	public synchronized double getDistanceBetweenPixels_X()
	{
		return drawingAreaCoords.getCartCoord_X(0,0)-drawingAreaCoords.getCartCoord_X(1,0);
	}
	
	public synchronized double getDistanceBetweenPixels_Y()
	{
		return drawingAreaCoords.getCartCoord_Y(0,0)-drawingAreaCoords.getCartCoord_Y(0,1);
	}
	
	
	/** Function for acquiring the Cartesian coordinates of the lower left pixel 
	 *  of the fractal being viewed.
	 * 
	 * @return The X-Cartesian coordinate of the lower left pixel of the visible fractal.
	 */
	public double getCartX1()
	{
		return drawingAreaCoords.getCartCoordOfLowLeftCorner_X();
		
	}
	
	/** Function for acquiring the Cartesian coordinates of the lower left pixel 
	 *  of the fractal being viewed.
	 * 
	 * @return The Y-Cartesian coordinate of the lower left pixel of the visible fractal.
	 */
	public double getCartY1()
	{
		return drawingAreaCoords.getCartCoordOfLowLeftCorner_Y();
	}
	
	/** Function for acquiring the Cartesian coordinates of the upper right pixel 
	 *  of the fractal being viewed.
	 * 
	 * @return The X-Cartesian coordinate of the upper right pixel of the visible fractal.
	 */
	public double getCartX2()
	{
		return drawingAreaCoords.getCartCoordOfUpRighCorner_X();
	}
	
	/** Function for acquiring the Cartesian coordinates of the upper right pixel 
	 *  of the fractal being viewed.
	 * 
	 * @return The Y-Cartesian coordinate of the upper right pixel of the visible fractal.
	 */
	public double getCartY2()
	{
		return drawingAreaCoords.getCartCoordOfUpRighCorner_Y();
	}
	
	
	/** Set location of the point for which we must draw the orbit
	 * 
	 * @param x - X-Cartesian coordinates of the point
	 * @param y - Y-Cartesian coordinates of the point
	 */
	public synchronized void setOrbitPoint(double x, double y)
	{
		dOrbitPointX = x;
		dOrbitPointY = y;
	}
	
	/** Returns x-coordinate of the point which orbit is to be drawn
	 *   
	 * @return Cartesian coordinates of the X of the point
	 */
	public synchronized double getOrbitPointX()
	{
		return dOrbitPointX;
	}
	
	/** Returns y-coordinate of the point which orbit is to be drawn
	 * 
	 * @return Cartesian coordinates of the Y of the point
	 */
	public synchronized double getOrbitPointY()
	{
		return dOrbitPointY;
	}
	
	
	public synchronized double getAspectRatioOfDrawingArea()
	{
		return dAspectRatio;
	}

	
	/** Move the fractal with in the specified direction
	 * 
	 * @param dx - delta-X in window coordinates
	 * @param dy - delta-Y in window coordinates
	 */
	public synchronized void translate(float dx, float dy)
	{
		drawingAreaCoords.translate(dx, dy);
	}
	
	
	public synchronized void scale(float scale)
	{
		drawingAreaCoords.scale(scale);
	}
	
	public synchronized void rotate(float degrees)
	{
		drawingAreaCoords.rotate(degrees);
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
	
	
	/** Changes the current fractal coordinates. 
	 *  This function lets the user to jump to any location.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 */
	public synchronized void setNewLocation(double x1, double y1, double x2, double y2) 
	{
		try {
			drawingAreaCoords.setDimensions(x1, y1, x2, y2);
		} catch (Exception e) {
			// we won't do anything in case of some exception
			e.printStackTrace();
		}
	}

	
	
	/** Resets to initial view of the Mandelbrot fractal
	 * 
	 * @param w -width of the drawing area
	 * @param h -height of the drawing area 
	 */
	public synchronized void reset(int w, int h)
	{
		init(w, h);
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
	
	
	public synchronized ColorMode getColorMode() 
	{
		return colorMode;
	}

	public synchronized void setColorMode(ColorMode currentColor) 
	{
		this.colorMode = currentColor;
	}
	
	/** Current ColorPeriodicity setting.
	 * 
	 * @return Return value ranges from 1 to 
	 */
	public synchronized int getColorPeriodicity() 
	{
		return iColorPeriodicity;
	}


	/** Current ColorPeriodicity setting.
	 * 
	 * @return Return value ranges from 1 to 
	 * @throws Exception 
	 */
	public synchronized void setColorPeriodicity(int colorPeriodicity) throws Exception 
	{
		if(colorPeriodicity <= 0)
			throw new Exception("Invalid colorPeriodicity value!"); //$NON-NLS-1$
		
		this.iColorPeriodicity = colorPeriodicity;
	}
	
	/** When in OrbitMode the orbit is drawn for each point being touched.
	 *  When not in OrbitMode (i.e. BrowsingMode) the user is  transforming the fractal view. 
	 * 
	 * @param bIsOrbitMode
	 */
	public synchronized void setOrbitMode(boolean bIsOrbitMode)
	{
	    bOrbitMode = bIsOrbitMode;
	    if(bOrbitMode)
	    {
	    	// If we are in Orbit mode this automatically means that the Orbit is visible
	    	bIsOrbitVisible = true;
	    }
	}
	
	public synchronized boolean isOrbitMode() 
	{
		return bOrbitMode;
	}
	

	public boolean isOrbitVisible() 
	{
		return bIsOrbitVisible;
	}


	public void setOrbitVisibility(boolean visible) 
	{
		bIsOrbitVisible = visible;
		if(bIsOrbitVisible == false)
		{
			// If the orbit is not visible - this means also that we should go to BrowsingMode
			bOrbitMode = false;
		}
	}

	
}
