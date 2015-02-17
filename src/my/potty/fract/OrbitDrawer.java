package my.potty.fract;

import android.graphics.Canvas;
import android.graphics.Paint;


public class OrbitDrawer {
	
	private	static final int MAX_ORBIT_POINTS = 400;  	      // How far should we examine the Orbit of a certain point
	
	private	static 	double[] 	arrayOrbit = new double[MAX_ORBIT_POINTS*2]; 	// Array used to hold the x,y coordinates for the Orbit path of a certain point
	private static 	int 		numberOfPointsInTheOrbit; // Specifies the number of valid points inside arrayOrbit

	private static 	FractalCalculator fractalCalculator;
	
	// The point for which we must draw the orbit (init with some random value).
	// We need to remember those so that if they change we will know that we have to recalculate the orbit again.
	private static 	double point_cartX = 66.03343434; 
	private static 	double point_cartY = 55.34343434; 

	// The Cartesian coordinates of the Window lower left corner (init with some random value). 
	// We need to remember those so that if they change we will know that we have to recalculate the orbit again.
	private static  double lowerLeftCornerX = 0.1231231; 
	private static  double lowerLeftCornerY = 12313;
	
	private static 	Paint  myPaint  = new Paint();	
	

	
	/**
	 * Draws the orbit onto the supplied bitmap. The orbit are drawn with a constant scaling factor,
	 * which does not change regardless of the current zoom factor we have in coordMngr.
	 * 
	 * @param coordMngr - object containing all the coordinates needed for drawing
	 * @param bitmap - the bitmap on which drawing will be done
	 */
	public static void draw(FractalSettings settings, Canvas canvas)
	{
		if(settings.isOrbitVisible() == false)
		{
			return; // Do nothing
		}
		
		
		
		calculateOrbit(settings);
			
			
		myPaint.setStrokeWidth(5);
		myPaint.setAntiAlias(true);
		
		
		// Draw a dot where you are touching onto the drawing display
		myPaint.setARGB(255, 255, 255, 255);
		canvas.drawPoint(settings.getWindowCoord_X(point_cartX, point_cartY),
						 settings.getWindowCoord_Y(point_cartX, point_cartY), 
					     myPaint);
		
	       
		// There are no points in the orbit 
		if(numberOfPointsInTheOrbit == 0)
		{
			return;
		}
		
	    myPaint.setStrokeWidth(0);
	    
	    // Draw line from the dot to the first orbit point
	    canvas.drawLine(settings.getWindowCoord_X(point_cartX, point_cartY),
				 		settings.getWindowCoord_Y(point_cartX, point_cartY), 
				 		(float)arrayOrbit[0], 
				 		(float)arrayOrbit[1], 
						myPaint);
	    
	    // Draw the rest of the orbit
		for(int i=1; i<numberOfPointsInTheOrbit;i++)
		{
			canvas.drawLine((float)arrayOrbit[i*2-2],
							(float)arrayOrbit[i*2-1], 
							(float)arrayOrbit[i*2],
							(float)arrayOrbit[i*2+1],
							myPaint);
		}//for
		
	}//drawOrbit()



	
	private static void calculateOrbit(FractalSettings settings) 
	{
		long tempX;
		long tempY;
		
		
		// We will calculate new orbit only:
		// - if the user has selected another point of interest
		// - or if the window has been moved (i.e. within the Cartesian plane)
		if(point_cartX != settings.getOrbitPointX() ||
		   point_cartY != settings.getOrbitPointY()	||
		   lowerLeftCornerX != settings.getCartX1() || 
		   lowerLeftCornerY != settings.getCartY1())
		{
			lowerLeftCornerX = settings.getCartX1();
			lowerLeftCornerY = settings.getCartY1();
			
			point_cartX = settings.getOrbitPointX();
			point_cartY = settings.getOrbitPointY();
		
			
			// Get the points composing the orbit of the current point
			fractalCalculator = FractalCalculator.getInstance(settings.getFractalType());
			// Set the C constant to current value	
			fractalCalculator.setConstant(settings.getImaginaryConstantRe(), settings.getImaginaryConstantIm()); 	
			// Calculate the Orbit for the selected point
			numberOfPointsInTheOrbit = fractalCalculator.getPath(point_cartX, 
																 point_cartY, 
															     arrayOrbit);
			
			// Convert the orbit points from Cartesian into Window coordinates (i.e. ready to be drawn)
			for(int i=0; i<numberOfPointsInTheOrbit;i++)
			{
				tempX = settings.getWindowCoord_X(arrayOrbit[i*2], arrayOrbit[i*2+1]);
				tempY = settings.getWindowCoord_Y(arrayOrbit[i*2], arrayOrbit[i*2+1]);
				
				arrayOrbit[i*2]   = tempX;
				arrayOrbit[i*2+1] = tempY;
			}//for
		}
	}


}//class
