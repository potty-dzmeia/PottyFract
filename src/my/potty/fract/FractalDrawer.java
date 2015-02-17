package my.potty.fract;

import android.graphics.Canvas;


/**
 * 
 * @author Pottry
 *
 * Class responsible for drawing a fractal onto a bitmap. 
 * 
 * Drawing is done using three buffers:
 * 1) iDrawingBuffer 	      - Each value in this buffer tells us how many number of iterations are done on each point of the complex plain.
 *					   			This buffer maybe incomplete if the thread is interrupted.	   
 * 2) iCompletedDrawingBuffer - Same as iDrawingBuffer, with the difference that this buffer holds valid values and drawing onto it can't be interrupted.
 * 								An intermediate buffer is used so that we can the coloring of each pixel of the complex plain.
 * 3) exportBitmap 			  - A bitmap which the user of this class is seeing
 */
public class FractalDrawer extends Thread
{	
	public static final int CALCULATION_STAGE_FINISHED = 5;
	
	private	int iDrawingStage = 0;  // The whole picture is drawn on 5 stages, controlled by this constant
	private boolean doInit = true;  // The the object must go through initialization
	
	private FractalCalculator fractalCalculator; // For inspecting points from the 2d plane for fractal property
	private	FractalSettings   fractalSettings; 	     // The fractal settings currently used by the Thread for calculating fractal points
	private ColorCreator      colorCreator;
	private int[]			  ongoingFractalImage;   // Buffer which holds how many number of iterations were done for each point represented onto the display
	private int[]			  completedFractalImage; // Holds the last valid representation of iDrawingBuffer
	private int               iterationsCount; 
	
	
	private double  dCurrentX; 		 // The current point that is inspected (in complex coordinates). Defined here for performance reasons
	private double 	dCurrentY;		
	private double  dThirdDistanceX; // The distance from the current point that we are inspecting to it's nearest neighbor, divided by 3.3 (Defined here for performance reasons)
	private double  dThirdDistanceY;  
	private int 	iCurrentX;	     // The current point that is inspected (in drawing/display coordinates). Defined here for performance reasons
	private int		iCurrentY; 
	
	
	private boolean bAntialiasing = false;
	//----------------------

   
	/** Starts a thread which is building the fractal image
	 *  
	 * @param coordMngr
	 */
	public FractalDrawer(FractalSettings settings)
	{	
		fractalSettings = settings;
	    colorCreator 	= new ColorCreator();
	    init();
		// Start the Thread and thus the drawing of the fractal
		this.setPriority(NORM_PRIORITY);
		this.start();
	}
	
	
	/** Checks if there is calculation  ongoing
	 * 
	 * @return true - the thread is currently processing fractal points
	 */
	public synchronized boolean isCurrentlyProcessing()
	{
		if(iDrawingStage < CALCULATION_STAGE_FINISHED)
		{
			return true;
		}
		return false;
	}
	
	public synchronized boolean isAnyDataAvailable()
	{
		// If we are at calculation stage 0 - we don't have anything to draw yet
		if(iDrawingStage==0)
			return false;
		
		return true;
	}
	
	
	
	/**
	 *  Point inspection is done here based on the supplied FractalSettings. 
	 *  When all points have been inspected the thread will go to sleep waiting for interrupt event.
	 */
	@Override
	public void run()
	{
		while(true)
		{
		    if(fractalProcessing() ==  false)
		    {
		        synchronized(this)
                {
                    try{wait();}catch(Exception e){e.printStackTrace();}
                }
		    }
		}
	}
	
	

	/**Draws the current state of the fractal onto the canvas.
	 * 
	 * @param canvas	- Drawing destination
	 * @param settings	- The current settings used for drawing.
	 * @return - Calculation stages:
	 * 			0 - Nothing has been drawn (calculation ongoing)
	 *			1 - First stage has been calculated and drawn (calculation ongoing)
	 *			...
	 *			4 - Fractal has been fully drawn (calculation finished)
	 */
    public int drawFractal(Canvas canvas, FractalSettings settings)
    {
        int[]  bitmapArray;
        int    stage;
        
    	
        synchronized(this)
        {     	
        	// Do not draw if nothing has been calculated or if we need to draw a new fractal 
        	if(iDrawingStage == 0 || doInit)
        		return 0;
        	
            bitmapArray = colorCreator.getColorArray(completedFractalImage,
			                                         fractalSettings.iterationsLimmit, 
			                                         settings.getColorMode(),
			                                         settings.getColorPeriodicity());
        
            canvas.drawBitmap(
            			  bitmapArray,
                          0,           // Offset into the array of colors for the first pixel
                          fractalSettings.width, // The number of of colors in the array between rows (must be >= width or <= -width).
                          0, 0,        // The X and Y coordinates for where to draw the bitmap
                          fractalSettings.width, 
                          fractalSettings.height,
                          false,       // True if the alpha channel of the colors contains valid values. If false, the alpha byte is ignored  (assumed to be 0xFF for every pixel).
                          null   );    // May be null. The paint used to draw the bitmap 
        
        
        	stage = iDrawingStage;
        }
        
        return stage;
    }
	

    /**
     * Start drawing the fractal from the beginning
     */
	public void resetProcessing()
	{
	    doInit = true;
	    bAntialiasing = false;
	    
	    // If the thread is calculating, interrupt it
        if(this.isAlive())
            this.interrupt();
        // If the thread is waiting, notify it
        else
            this.notifyAll();
	}
	
	 /**
     * Start drawing the fractal from the beginning
     */
	public void resetProcessingWithAntialiasing()
	{
	    doInit = true;
	    bAntialiasing = true;
	    
	    // If the thread is calculating, interrupt it
        if(this.isAlive())
            this.interrupt();
        // If the thread is waiting, notify it
        else
            this.notifyAll();
	}
	
	
	
	/** Draws a fractal of the type iFractID on a bitmap using the information
	 *  supplied by CoordSysManager
	 * 
	 * @param fractalSettings Class containing all the info about 
	 * @param iFractID	ID specifying which fractal type is to be drawn
	 * @return			True - if more stages of drawing remain and we should call the function again
	 * 					False - if no more stages of drawing remain and calling is not needed
	 */
	private boolean fractalProcessing ()
	{	
		if(doInit)
		{
			doInit = false;
		    init();
		    
		}
		
		
		// Implementation of the algorithm for drawing the fractal image. 
		// It is made so that it looks like that the fractal is coming out gradually
		switch (iDrawingStage) 
		{
			case 0:
				for(iCurrentX=0; iCurrentX<fractalSettings.width; iCurrentX+=4)
				{
					for(iCurrentY=0; iCurrentY<fractalSettings.height; iCurrentY+=4)
					{			
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);

							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		    dCurrentX,
													   		    dCurrentY,
													   		    fractalSettings.iterationsLimmit
													    	   );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}
											
						
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width] = iterationsCount;	
					}//for
				}//for
						
				
				synchronized(this)
				{
					// Save the result from the current drawing phase 
				    System.arraycopy(ongoingFractalImage, 0, completedFractalImage, 0, completedFractalImage.length); 
					// Go to the next stage when this function is called again
					iDrawingStage++;
				}
			
				return true; // More drawing to come   
				
				
			case 1:
				for(iCurrentX=2; iCurrentX<fractalSettings.width; iCurrentX+=4)
				{
					for(iCurrentY=2; iCurrentY<fractalSettings.height; iCurrentY+=4)
					{
						// The thread should restart drawing
						if(Thread.interrupted())
							return true;
						
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain.
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
                            
							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		  dCurrentX,
													   		  dCurrentY,
													   		  fractalSettings.iterationsLimmit
													    	  );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width]   = iterationsCount;	
					}
				}//for
				
				synchronized(this)
				{
					// Save the result from the current drawing phase 
				    System.arraycopy(ongoingFractalImage, 0, completedFractalImage, 0, completedFractalImage.length); 
					// Go to the next stage when this function is called again
					iDrawingStage++;
				}
				return true; // More drawing to come   
				
				
			case 2:
				for(iCurrentX=0; iCurrentX<fractalSettings.width; iCurrentX+=4)
				{
					for(iCurrentY=2; iCurrentY<fractalSettings.height; iCurrentY+=4)
					{
						// The thread should restart drawing
						if(Thread.interrupted())
							return true;
						
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain.
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
                            
							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		  dCurrentX,
													   		  dCurrentY,
													   		  fractalSettings.iterationsLimmit
													    	  );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width]   = iterationsCount;	
					}
				}//for
				for(iCurrentX=2; iCurrentX<fractalSettings.width; iCurrentX+=4)
				{
					for(iCurrentY=0; iCurrentY<fractalSettings.height; iCurrentY+=4)
					{
						// The thread should restart drawing
						if(Thread.interrupted())
							return true;
						
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain.
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
                            
							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		  dCurrentX,
													   		  dCurrentY,
													   		  fractalSettings.iterationsLimmit
													    	  );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width]   = iterationsCount;	
					}
				}//for
				
				synchronized(this)
				{
					// Save the result from the current drawing phase 
				    System.arraycopy(ongoingFractalImage, 0, completedFractalImage, 0, completedFractalImage.length); 
					// Go to the next stage when this function is called again
					iDrawingStage++;
				}
				return true; // More drawing to come   
				
				
			case 3:
				for(iCurrentX=1; iCurrentX<fractalSettings.width; iCurrentX+=2)
				{
					for( iCurrentY=1; iCurrentY<fractalSettings.height; iCurrentY+=2)
					{
						// The thread should restart drawing
						if(Thread.interrupted())
							return true;
						
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain.
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
                            
							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		  dCurrentX,
													   		  dCurrentY,
													   		  fractalSettings.iterationsLimmit
													    	  );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width]   = iterationsCount;	
					}
				}//for
				
				synchronized(this)
				{
					// Save the result from the current drawing phase 
				    System.arraycopy(ongoingFractalImage, 0, completedFractalImage, 0, completedFractalImage.length); 
					// Go to the next stage when this function is called again
					iDrawingStage++;
				}
				return true; // More drawing to come   
				
				
			case 4:
				for(iCurrentX=0; iCurrentX<fractalSettings.width; iCurrentX+=2)
				{
					for(iCurrentY=1; iCurrentY<fractalSettings.height; iCurrentY+=2)
					{
						// The thread should restart drawing
						if(Thread.interrupted())
							return true;
						
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain.
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
                            
							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		  dCurrentX,
													   		  dCurrentY,
													   		  fractalSettings.iterationsLimmit
													    	  );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}	
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width]   = iterationsCount;	
					}
				}//for
				for(iCurrentX=1; iCurrentX<fractalSettings.width; iCurrentX+=2)
				{
					for(iCurrentY=0; iCurrentY<fractalSettings.height; iCurrentY+=2)
					{
						// The thread should restart drawing
						if(Thread.interrupted())
							// Stop Drawing the current image
							return true;
						
						// Anti Aliasing is OFF. Test just the points from the display.
						if ( bAntialiasing == false)
						{
							// Converts current point from the display to a point from the complex plain.
							dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
							dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
                            
							// Inspects if the current point belongs to the Mandelbrot (other) set
							iterationsCount = fractalCalculator.testPoint(
													   		  dCurrentX,
													   		  dCurrentY,
													   		  fractalSettings.iterationsLimmit
													    	  );
						}
						// Anti Aliasing is ON. Test neighboring points also.
						else
						{ 
							iterationsCount = testPointAntiAliasing();
						}
						
						// Save the number of iterations for the current point 
						ongoingFractalImage[iCurrentX+iCurrentY*fractalSettings.width]   = iterationsCount;	
					}
				}//for
			
				synchronized(this)
				{
					// Save the result from the current drawing phase 
				    System.arraycopy(ongoingFractalImage, 0, completedFractalImage, 0, completedFractalImage.length); 
					// Go to the next stage when this function is called again
					iDrawingStage++;
				}		
				return true; // More drawing to come   

				
			default:
				return false; // Finished drawing 		
		}
		
	}//drawFract()
	
	
	
	
	/*  Tests the current and 8 more points around the current. This function is called
	 *  in case we need we want an Anti Aliased picture of the fractal. Using this
	 *  function we test 8 more points around the current.
	 *  
	 *  Example:
	 *  Usually we test as much points as we have on our display. Let's say we have a 
	 *  640x400 display (pixels are denoted as stars). This means that we will test 256000 dots: 
	 *              ........................
	 *              ... * * * * * * * * *...
	 *              ... * * * * * * * * *... 
	 *              ... * * * * * * * * *...
	 *              ... * * * * * * * * *...
	 *              ........................
	 *  However with this function we test additionally 8 more points (denoted as +) 
	 *  for each point on our display:
	 *                  
	 *                + + + + + + + + + + +
	 *                + + * + + * + + * + + 
	 *                + + + + + + + + + + +
	 *                + + + + + + + + + + +
	 *                + + * + + * + + * + + 
	 *                + + + + + + + + + + +
	 * 
	 */
	final private int testPointAntiAliasing() 
	{
		dCurrentX = fractalSettings.getRealCoord_X(iCurrentX, iCurrentY);
		dCurrentY = fractalSettings.getRealCoord_Y(iCurrentX, iCurrentY);
       

		// Calculate the distance between two points on the drawing plane and divide by three
		dThirdDistanceX = Math.abs(fractalSettings.getDistanceBetweenPixels_X()) / 3.3;
		dThirdDistanceY = Math.abs(fractalSettings.getDistanceBetweenPixels_Y()) / 3.3;
		
		
		iterationsCount = 0;
		
		// Test the current point and the 8 of it's neighbors
        // --------------------------------------------------
		// Current point (denoted as a star on the example)
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX,
											 dCurrentY,
											 fractalSettings.iterationsLimmit
										     );	
		// Right of the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX + dThirdDistanceX,
											 dCurrentY,
											 fractalSettings.iterationsLimmit
										     );	
		// Left of the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX - dThirdDistanceX,
											 dCurrentY,
											 fractalSettings.iterationsLimmit
										     );	
		// Up from the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX,
											 dCurrentY - dThirdDistanceY,
											 fractalSettings.iterationsLimmit
										     );	
		// Down from the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX,
											 dCurrentY + dThirdDistanceY,
											 fractalSettings.iterationsLimmit
										     );	
		// Right and up from the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX + dThirdDistanceX,
											 dCurrentY - dThirdDistanceY,
											 fractalSettings.iterationsLimmit
										     );	
		// Right and down from the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX + dThirdDistanceX,
											 dCurrentY + dThirdDistanceY,
											 fractalSettings.iterationsLimmit
										     );	
		//Left and up from the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX - dThirdDistanceX,
											 dCurrentY - dThirdDistanceY,
											 fractalSettings.iterationsLimmit
										     );	
		// Left and down from the current
		iterationsCount += fractalCalculator.testPoint(
											 dCurrentX - dThirdDistanceX,
											 dCurrentY + dThirdDistanceY,
											 fractalSettings.iterationsLimmit
										     );	
		
		
		
		// calculate the average and return it as result
		iterationsCount /= 9;
		
		return iterationsCount;
	}
	
	

	/** Inits the data fields of the class.
	 *  
	 * @param settings
	 */
    private synchronized void init()
    {
    	
    	iDrawingStage = 0; // Start drawing the fractal from the beginning
    	 
        fractalCalculator = FractalCalculator.getInstance(fractalSettings.getFractalType()); 
        fractalCalculator.setConstant(fractalSettings.getImaginaryConstantRe(), 
                                      fractalSettings.getImaginaryConstantIm() ); 
        
        // Create the buffer where the currently processed image is going to be stored
        if(ongoingFractalImage == null ||    // if it has not been created
           ongoingFractalImage.length != fractalSettings.height*fractalSettings.width)       // if the screen size has changed
        {
            ongoingFractalImage = new int[fractalSettings.width*fractalSettings.height];
        }
           
        // Create the buffer which will hold the results after each drawing phase.
        if (completedFractalImage == null || 
            completedFractalImage.length != fractalSettings.height*fractalSettings.width)                                                
        {
        	completedFractalImage = new int[fractalSettings.width*fractalSettings.height];
        }
          
        // Init the two buffers
        for (int j = 0; j < completedFractalImage.length; j++)
        {
        	completedFractalImage[j] = -1; // -1 mean the point was not inspected
        	ongoingFractalImage[j]   = -1;
        	
        }    
    }
	
}
