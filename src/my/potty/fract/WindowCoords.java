package my.potty.fract;

import my.potty.tools.AffineTransform;
import my.potty.tools.Point2D;
import android.graphics.Matrix;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * This class defines which part of the 2D Cartesian plane (in real numbers) is 
 * visualized by the drawing area. In other words the class defines the position
 * and the size of the drawing area inside the 2D plane.<br>
 * 
 * <b>Important notions:</b><br>
 * Window coordinates(in pixels) - these are the coordinates of individual pixels
 *      inside the drawing area. The pixel(0,0) is located at the top left 
 *      corner of the drawing are. The pixel(Width-1, Height-1) is located 
 *      at the bottom right corner of the drawing area.<br>
 * 
 * Cartesian Coordinates - These are coordinates in the 2D Cartesian plane which define
 *      the position and size of the drawing area inside this plane. 
 *      The 2D coordinates grow from bottom to top and from left to right. <br>
 * 
 * @author levkov
 */
public class WindowCoords 
{
    
    public int widthInPixels;     // The width of the drawing area in pixels
    public int heightInPixels;    // The height of the drawing area in pixels
     
    /**
     *  Array of double precision coordinates which describes the 2D location of each 
     *  pixel from the drawing area.
     *  In other words this array maps window coordinates to 2D real number coordinates.
     *  Each point in the array is stored as a pair of x, y coordinates.
     *  
     *  The size of the buffer equals the number of pixels of the drawing area.
     *  
     *  As an example here are the coordinates of some pixels inside the drawing area:
     *  upper left corner (x,y) = (drawingAreaCoords[0],drawingAreaCoords[1])
     *  lower left corner (x,y) = (drawingAreaCoords[width*(height-1)*2],drawingAreaCoords[width*(height-1)*2+1])
     *  upper right corner (x,y) = (drawingAreaCoords[0],drawingAreaCoords[1])
     */
    public  double[] cartesianCoords;
    
  
    /** The Intial Cartesian coords of the lower left corner (i.e. x1,y1)
     *  and the upper right corner (i.e. x2, y2) of the drawing area.
     */
    private double x1, x2, y1, y2;
  
    
    // When an object is created x1, x2, y1 and y2 will be initialized using the values below
    private static final double X1_INIT = 0;
    private static final double Y1_INIT = 0;
    private static final double X2_INIT = 2;
    private static final double Y2_INIT = 2;
    
    
    // Some work objects
    Point2D pointX2Y2, pointXY;
    
    double printRad;
    
    /** Creates an object which holds the real coordinates of each pixel from the
     *  drawing area.
     *  The size of the Window inside the Cartesian plane is of some random value.
     *  setDimensions() function should be used in order to set it to the desired
     *  location.
     * 
     * @param pixelWidth - The width of the drawing area in pixels. For example
     *      if width is 800 this means that the leftmost pixel will have coord
     *      0 and the rightmost pixel will have coord 799.
     * @param pixelHeight - The height of the drawing area in pixels. For
     *      example if the height is 600 this means that the topmost pixel will
     *      have coord 0 and the lowermost pixel will have coord 599.
     */
    public WindowCoords(int pixelWidth, int pixelHeight) throws Exception 
    {  	
    	setDimensions(pixelWidth, 
    				  pixelHeight, 
    				  X1_INIT, 
    				  Y1_INIT, 
    				  X2_INIT,
    				  Y2_INIT);
    	
    	
    	// Init the work objects (so that we save execution time later)
    	pointX2Y2 = new Point2D.Double();
    	pointXY   = new Point2D.Double();
    }   

    

    /** Sets the size Window size (in pixels) and the Window location inside the Cartesian 2D plane
     * 
     * @param pixelWidth -  width of the drawing area (window) in pixels
     * @param pixelHeight - height of the drawing area (window) in pixels
     * @param x1 - x-coordinate inside the Cartesian plane of the window lower left corner
     * @param y1 - y-coordinate inside the Cartesian plane of the window lower left corner
     * @param x2 - x-coordinate inside the Cartesian plane of the window upper right corner
     * @param y2 - y-coordinate inside the Cartesian plane of the window upper right corner
     * @throws Exception
     */
    public void setDimensions(int pixelWidth, int pixelHeight, 
    						   double x1, double y1, 
    						   double x2, double y2) throws Exception
    {
    	setSizeInPixels(pixelWidth, pixelHeight);   
    	setInitialCartCoords(x1, y1, x2, y2); 
    	initBuffers();
    }
    
    
    
    /** Sets the Window location inside the Cartesian 2D plane
     * 
     * @param x1 - x-coordinate inside the Cartesian plane of the window lower left corner
     * @param y1 - y-coordinate inside the Cartesian plane of the window lower left corner
     * @param x2 - x-coordinate inside the Cartesian plane of the window upper right corner
     * @param y2 - y-coordinate inside the Cartesian plane of the window upper right corner
     * @throws Exception
     */
    public void setDimensions(double x1, double y1, double x2, double y2) throws Exception
    {
    	setInitialCartCoords(x1, y1, x2, y2);
    	initBuffers();
    }
    
    
    
    /** Sets the Window location inside the Cartesian 2D plane
     * 
     * @param x1 - x-coordinate inside the Cartesian plane of the window lower left corner
     * @param y1 - y-coordinate inside the Cartesian plane of the window lower left corner
     * @param x2 - x-coordinate inside the Cartesian plane of the window upper right corner
     * @param y2 - y-coordinate inside the Cartesian plane of the window upper right corner
     * @throws Exception
     */
    public void setDimensions(int pixelWidth, int pixelHeight) throws Exception
    {
    	setSizeInPixels(pixelWidth, pixelHeight);
    	initBuffers();
    }
    
    
    
    /** Returns the real coordinates(X-part) of a pixel.
     * 
     * @param pixel_x - x-coordinate of the pixel that we are interested in
     * @param pixel_y - y-coordinate of the pixel that we are interested in
     * @return - real number specifying the location of the pixel in the Cartesian plane
     */
    public double getCartCoord_X(int pixel_x, int pixel_y)
    {
    	return cartesianCoords[(pixel_y*widthInPixels+pixel_x)*2];
    }
    
    
    
    /** Returns the real coordinates(Y-part) of a pixel.
     * 
     * @param pixel_x - x-coordinate of the pixel that we are interested in
     * @param pixel_y - y-coordinate of the pixel that we are interested in
     * @return - real number specifying the location of the pixel in the Cartesian plane 
     */
    public double getCartCoord_Y(int pixel_x, int pixel_y)
    {
    	return cartesianCoords[(pixel_y*widthInPixels+pixel_x)*2 +1];
    }
    
   
    /** Returns the window coordinates (in pixels) of the selected point of the Cartesian plane.
     *  Throws exception if the point lies outside the window area.
     * 
     * @param cart_x - X-Cartesian coordinates of the point that we are interested in
     * @return The X position of the pixel on the window.
     */
    public long getWindowCoord_X(double cart_X, double cart_Y)
    {
    	double X1 = getCartCoordOfLowLeftCorner_X();
    	double Y1 = getCartCoordOfLowLeftCorner_Y();
    	
    	double X2 = getCartCoordOfLowRightCorner_X();
    	double Y2 = getCartCoordOfLowRightCorner_Y();
    
    	// Find the angle of rotation of our Window inside the Cartesian plane
    	double rad = Math.atan2((Y2-Y1), (X2-X1));
    	if(rad<0) // Range should be from 0 to 2*pi rad
    		rad += Math.PI*2;
    	   	
   
    	// We need to rotate the Window inside the Cartesian plane so that
    	// the Window_abscissa is perpendicular to the Cartesian_abscissa
    	// This will make the conversion from Cartesian to Window coordinates easy.
    	// Note: There might be an easier way but I suck in math
    	AffineTransform at = new AffineTransform();
    	at.rotate(-rad, X1, Y1); // do rotation around the lower left corner of the Window
    	pointX2Y2.setLocation(X2, Y2);	
    	at.transform(pointX2Y2, pointX2Y2); // Now transform (rotate) the lower right corner of the Window
    	pointXY.setLocation(cart_X, cart_Y);
    	at.transform(pointXY, pointXY);// Transform also the input point
    	
    	
    	// Now we are ready to do the linear conversion from Cartesian to Window coordinates
        // We use linear equation of the type f(x)= ax + b, where: a= ((width-1)/(x2-x1))*(X-x1);  b = 0;  
    	
    	return Math.round(((widthInPixels-1)/(pointX2Y2.getX()-X1))*(pointXY.getX()-X1));
    }
    
    /** Returns the window coordinates (in pixels) of the selected point of the Cartesian plane.
     *  Throws exception if the point lies outside the window area.
     * 
     * @param cart_y - Y-Cartesian coordinates of the point that we are interested in
     * @return The Y position of the pixel on the window.
     */
    public long getWindowCoord_Y(double cart_X, double cart_Y)
    {
    	double X1 = getCartCoordOfLowLeftCorner_X();
    	double Y1 = getCartCoordOfLowLeftCorner_Y();
    	
    	double X2 = getCartCoordOfLowRightCorner_X();
    	double Y2 = getCartCoordOfLowRightCorner_Y();
    
    	// Find the angle of rotation of our Window inside the Cartesian plane
    	double rad = Math.atan2((Y2-Y1), (X2-X1));
    	if(rad<0)// Range should be from 0 to 2*pi rad
    		rad += Math.PI*2;
    	   	
   
    	// Now we want (X2, Y2) to be the upper right corner
    	X2 = getCartCoordOfUpRighCorner_X();
    	Y2 = getCartCoordOfUpRighCorner_Y();
    	
    	
    	// We need to rotate the Window inside the Cartesian plane so that
    	// the Window_abscissa is perpendicular to the Cartesian_abscissa
    	// This will make the conversion from Cartesian to Window coordinates easy.
    	// Note: There might be an easier way but I suck in math
    	AffineTransform at = new AffineTransform();
    	at.rotate(-rad, X1, Y1); // do rotation around the lower left corner of the Window
    	pointX2Y2.setLocation(X2, Y2);	// put X1 and Y2 into an Point2d object
    	at.transform(pointX2Y2, pointX2Y2); // Now transform (rotate) the upper right corner of the Window
    	pointXY.setLocation(cart_X, cart_Y);
    	at.transform(pointXY, pointXY);// Transform the input
    	
    	
    	// Now we are ready to do the linear conversion from Cartesian to Window coordinates
        // We use linear equation of the type f(x)= ax + b, where: 
    	//a= (-(height-1)/(x2-x1))*(X-x1)  
    	//b= (height-1)
    	return Math.round(-((heightInPixels-1)/(pointX2Y2.getY()-Y1))*(pointXY.getY()-Y1) + (heightInPixels-1));
    	 
    }
    
    
    /** 
     * Translates the location of the drawing area inside the Cartesian plane.
     * 
     * @param dx - Delta X in window coordinates 
     * @param dy - Delta Y in window coordinates
     */
    public void translate(float dx, float dy)
    {
    
    	AffineTransform at = new AffineTransform();
    	
    	// Amount of scroll 
    	double deltaX = getCartWidth() *(-dx/widthInPixels);
    	double deltaY = getCartHeight()*(dy/heightInPixels);
    	
    	double X1 = getCartCoordOfLowLeftCorner_X();
    	double Y1 = getCartCoordOfLowLeftCorner_Y();
    	
    	double X2 = getCartCoordOfLowRightCorner_X();
    	double Y2 = getCartCoordOfLowRightCorner_Y();
    
    	// Find the angle of rotation of our Window inside the Cartesian plane
    	double rad = Math.atan2((Y2-Y1), (X2-X1));
    	
    	/// Range should be from 0 to 2*pi rad
    	if(rad<0)
    		rad += Math.PI*2;

    	if(printRad!=rad)
    	{
    		printRad = rad;
    		System.out.println("rad= "+printRad);
    	}
    	
    	// Correct the scroll direction for the amount of rotation
    	double FinalDeltaX = deltaX*Math.cos(rad) - deltaY*Math.sin(rad); 
    	double FinalDeltaY = deltaX*Math.sin(rad) + deltaY*Math.cos(rad); 
    	
    	// 
    	at.translate(FinalDeltaX, FinalDeltaY);
    	at.transform(cartesianCoords, 0, cartesianCoords, 0, cartesianCoords.length/2);    	
    }
    
    
    /** Scales the window inside the Cartesian plane. The pivot point of the scale is
     *  the center of the window.
     * 
     * @param scaleFactor
     */
    public void scale(float scaleFactor)
    {
    	AffineTransform at = new AffineTransform();
    	at.setToTranslation(getCartCoordOfCenter_X(), getCartCoordOfCenter_Y());
    	at.scale(1/scaleFactor, 1/scaleFactor);
    	at.translate(-getCartCoordOfCenter_X(), -getCartCoordOfCenter_Y());
    	at.transform(cartesianCoords, 0, cartesianCoords, 0, cartesianCoords.length/2);
    }
    
   
    /** Rotates the window inside the Cartesian plane. The pivot point is the center
     *  of the window.
     * 
     * @param degrees Rotation in degrees
     */
    public void rotate(float degrees)
    {
    	double rads = Math.toRadians(degrees);
    	AffineTransform at = new AffineTransform();
    	at.rotate(rads, getCartCoordOfCenter_X(), getCartCoordOfCenter_Y());
    	at.transform(cartesianCoords, 0, cartesianCoords, 0, cartesianCoords.length/2);
    }
    
    
    
    
    /** 
     * Initializes the drawingAreaCoords_original[] and drawingAreaCoords[]  
     */
    private void initBuffers() 
    {
    	int iNewBufferSize = widthInPixels*heightInPixels*2;
    	
    	// Allocate new size if ...
    	if(cartesianCoords == null 				    || // has not been allocated till now
    	   cartesianCoords.length != iNewBufferSize   )// we need different buffer size
    	{
    		cartesianCoords = new double[iNewBufferSize];	
    	}
    		        	
    	int iter = 0;	
    	try 
    	{
    		for(int y=0; y<heightInPixels; y++)
    		{
    			for(int x=0; x<widthInPixels; x++)
    			{
    				cartesianCoords[iter++] = convertXtoCartCoord(x);
    				cartesianCoords[iter++] = convertYtoCartCoord(y);
    			}
    		}
    	} catch (Exception e) {
			e.printStackTrace();
		}
		
   }

    
    
    /** Function to be used only by initBuffers() (i.e. when the window is not rotated
     *  inside the Cartesian plane).  
     * 
     * Remark: instead use getCartCoord_X() 
     * 
     * @param xToConvert
     * @return
     * @throws Exception
     */
    private double convertXtoCartCoord(int xToConvert) throws Exception
    {
        // Check if the coordinate is lying outside windows area
        if(xToConvert < 0)
        	throw new Exception("Coordinate is outside drawing area!"); //$NON-NLS-1$
        if(xToConvert >= widthInPixels)
        	throw new Exception("Coordinate is outside drawing area!"); //$NON-NLS-1$
        
        
        // Now make the linear conversion to Cartesian real number coordinates
        // Linear equation of the type f(x)= ax + b, where:  b = x1; a= (x2-x1)/(width-1)
        double result = ((x2-x1)/(widthInPixels-1)) * xToConvert + x1 ;
        
        
        return  result;
    }
    
    
  
    /** Function to be used only by initBuffers() (i.e. when the window is not rotated
     *  inside the Cartesian plane). 
     * 
     * Remark: instead use getCartCoord_Y()
     * 
     * @param yToConvert
     * @return
     * @throws Exception
     */
    private double convertYtoCartCoord(int yToConvert)  throws Exception
    {
    	 // Check if the coordinate is lying outside windows area
        if(yToConvert < 0)
        	throw new Exception("Coordinate is outside drawing area!"); //$NON-NLS-1$
        if(yToConvert >= heightInPixels)
        	throw new Exception("Coordinate is outside drawing area!"); //$NON-NLS-1$
        
        
        // Now make the linear conversion to Cartesian real number coordinates
        // Linear equation of the type f(y)= ay + b, where:  b = y2; a= (x2-x1)/(width-1)
        double result = (-(y2-y1)/(heightInPixels-1)) * yToConvert + y2 ;
        
        return result;
    }

    
    /** Using this function we can access specific pixels from the drawingAreaCoords[] array.
     *  The return index specifies the x-coordinates of the pixel inside the Cartesian plane.
     *  If we would like to access the y-coordinates of the same pixel we need to increment
     *  the returned value.
     * 
     * @return - the location of the pixel inside the drawingAreaCoords[] array. 
     *           This index specifies the X-coordinates of the pixel inside the Cartesian plane.
     */
    private int getPixelIndex(int x, int y)
    {
    	return (y*widthInPixels+x)*2;
    }
    
    
    
    private double getCartWidth()
    {						
    	double X1 = getCartCoordOfUpLeftCorner_X();
    	double Y1 = getCartCoordOfUpLeftCorner_Y();
    	
    	double X2 = getCartCoordOfUpRighCorner_X();
    	double Y2 = getCartCoordOfUpRighCorner_Y();
    	
    	//Pythagorean theorem...
    	return Math.sqrt(Math.pow(X2-X1, 2)+Math.pow(Y2-Y1, 2));
    }
    
    private double getCartHeight()
    {
    	double X1 = getCartCoordOfUpLeftCorner_X();
    	double Y1 = getCartCoordOfUpLeftCorner_Y();
    	
    	double X2 = getCartCoordOfLowLeftCorner_X();
    	double Y2 = getCartCoordOfLowLeftCorner_Y();
    	
    	//Pythagorean theorem...
    	return Math.sqrt(Math.pow(X2-X1, 2)+Math.pow(Y2-Y1, 2));
    }
    
    
    
    
    /** Changes the size (in pixels) of the drawing area
     *  
     * @param pixelWidth - new width
     * @param pixelHeight - new height
     * @throws java.lang.Exception 
     */
    private void setSizeInPixels(int pixelWidth, int pixelHeight) throws Exception
    {
        if(pixelWidth <= 0)
            throw new Exception("pixelWidth cannot be smaller than 0"); //$NON-NLS-1$
        if(pixelHeight <= 0)
            throw new Exception("pixelHeight cannot be smaller than 0"); //$NON-NLS-1$
            
        this.widthInPixels = pixelWidth;
        this.heightInPixels = pixelHeight;
    }
    
    
    
    /** Sets the coordinates of the lower left (x1,y2) and upper right (x2,y2) corners
     *  which define the location of the drawing area in the 2D Cartesian plane.
     * 
     * @param x1 - X-coordinate of the LOWER LEFT corner of the Drawing area inside the 2D plane. 
     * @param y1 - Y-coordinate of the LOWER LEFT corner of the Drawing area inside the 2D plane. 
     * @param x2 - X-coordinate of the UPPER RIGHT corner of the Drawing area inside the 2D plane. 
     * @param y2 - Y-coordinate of the UPPER RIGHT corner of the Drawing area inside the 2D plane. 
     * @throws Exception 
     */
    private void setInitialCartCoords(double x1, double y1, double x2, double y2) throws Exception
    {
    	
    	if((x2-x1) <= 0f)
    		throw new Exception("Invalid values for X coordinates!"); //$NON-NLS-1$
        if((y2-y1) <= 0f)
        	throw new Exception("Invalid values for Y coordinates!"); //$NON-NLS-1$     
         
    	this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    
    public double getCartCoordOfLowLeftCorner_X()
    {
    	return cartesianCoords[getPixelIndex(0,heightInPixels-1)];
    }
    public double getCartCoordOfLowLeftCorner_Y()
    {
    	return cartesianCoords[getPixelIndex(0,heightInPixels-1)+1];	
    }
    private double getCartCoordOfLowRightCorner_X()
    {
    	return cartesianCoords[getPixelIndex(widthInPixels-1,heightInPixels-1)];
    }
    private double getCartCoordOfLowRightCorner_Y()
    {
    	return cartesianCoords[getPixelIndex(widthInPixels-1,heightInPixels-1)+1];
    }
    private double getCartCoordOfUpLeftCorner_X()
    {
    	return cartesianCoords[getPixelIndex(0,0)];
    }
    private double getCartCoordOfUpLeftCorner_Y()
    {
    	return cartesianCoords[getPixelIndex(0,0)+1];
    }
    public double getCartCoordOfUpRighCorner_X()
    {
    	return cartesianCoords[getPixelIndex(widthInPixels-1,0)];
    }
    public double getCartCoordOfUpRighCorner_Y()
    {
    	return cartesianCoords[getPixelIndex(widthInPixels-1,0)+1];
    }
    private double getCartCoordOfCenter_X()
    {
    	return cartesianCoords[getPixelIndex(widthInPixels/2,heightInPixels/2)];
    }
    private double getCartCoordOfCenter_Y()
    {
    	return cartesianCoords[getPixelIndex(widthInPixels/2,heightInPixels/2)+1];
    }
  
    
    // TODO 
     /**Returns the path of the waveform inside the drawing area. 
     *  If the waveform is going outside the bounds of the drawing area the 
     *  point will be placed on the edge of the drawing area.
     * 
     * @param waveform - the waveform defined in real 2D Cartesian coordinates.
     * 
     * @return - Path of the waveform inside the drawing surface. The path is
     * in pixel coordinates so that (0,0) is the top left corner of the 
     * drawing area.
     */
//    public synchronized Path2D.Double getDrawingPath(int[][] dataPoints)
//    {
//        Path2D.Double drawingPath = new Path2D.Double();
//       
//        // If empty waveform...
//        if(dataPoints.length == 0)
//           return drawingPath;
//
//        
//        //int[][] dataPoints = waveform.getDataPoints();
//        
//        // Convert the first data point into Drawing coordinates and store it
//        drawingPath.moveTo(this.getWindowCoordOf_X(dataPoints[0][Waveform.X]), 
//                           this.getWindowCoordOf_Y(dataPoints[0][Waveform.Y]) );
//        
//        for(int[] dataP : dataPoints)
//        {
//            // Convert the rest data points into Drawing coordinates and store them
//            drawingPath.lineTo(this.getWindowCoordOf_X(dataP[Waveform.X]), 
//                               this.getWindowCoordOf_Y(dataP[Waveform.Y]) );
//        }
//        
//        return drawingPath;
//    } 
    
    /**
//   *  Transforms the geometry of the 2D coordinates of the drawing area
//   *  using the specified AffineTransform. The pivot point for the 
//   *  transformation is the lower left corner of the drawing area.
//   *  The geometry is transformed in place, which permanently changes the 
//   *  boundary defined by this object. 
//   * 
//   * @param at - the AffineTransform used to transform the area
//   * @throws java.lang.Exception
//   */
//  public void transform(AffineTransform at) throws Exception
//  {  
//      // We want the anchor point to be the lower left corner of the drawing
//      // area. That's why we need to translate the the drawing area so that 
//      // the lower left corner is lying at 2D coordinates (0,0)
//      double origX1, origY1;
//      origX1 = x1;
//      origY1 = y1;
//      x1 = 0; 
//      x2 -= origX1;
//      y1 = 0; 
//      y2 -= origY1; 
//      
//      // Load the coordinates into Point2D objects
//      Point2D pointX1Y1, pointX2Y2;
//      pointX1Y1 = new Point2D.Double(x1, y1);
//      pointX2Y2 = new Point2D.Double(x2, y2);
//      // Do the transformation
//      pointX1Y1 = at.transform(pointX1Y1, null); // lower left corner
//      pointX2Y2 = at.transform(pointX2Y2, null); // upper right corner
//      
//      
//      // Check if the transformation messed our coordinate system
//      if( (pointX1Y1.getX() >= pointX2Y2.getX())  ||
//          (pointX1Y1.getY() >= pointX2Y2.getY())     )
//      {
//          throw new Exception("The dimentions of the drawing area inside the 2D plane have been broken!"); //$NON-NLS-1$
//      }
//      
//      // Read the transformed coordinates
//      x1 = pointX1Y1.getX();
//      y1 = pointX1Y1.getY();
//         
//      x2  = pointX2Y2.getX();
//      y2  = pointX2Y2.getY();
//      
//      // And translate the drawing area to it's previous place
//      x1 += origX1; 
//      x2 += origX1; 
//      y1 += origY1;  
//      y2 += origY1; 
//  }
//  
 
}
