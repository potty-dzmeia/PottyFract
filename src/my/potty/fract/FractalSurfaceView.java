package my.potty.fract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


import my.potty.fract.FractalSettings.ColorMode;
import my.potty.gesturedetectors.MoveGestureDetector;
import my.potty.gesturedetectors.RotateGestureDetector;
import my.potty.tools.SimpleTransform;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint;
import android.graphics.PointF;
import android.location.SettingInjectorService;
import android.net.Uri;
import android.os.Environment;
import android.renderscript.RenderScript.Priority;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.KeyEvent;

public class FractalSurfaceView extends SurfaceView implements Runnable
{           
    private static final int RANGE_CUSTOM_FRACTAL_SLIDERS = 1000;
    protected static enum AppStatus{MOTION, 	// User is touching the screen
    								NO_MOTION}  // User is not touching the screen             
    
    private Context context;
    private Thread  drawingThread; 
    private boolean isThreadRunning = false;

    protected FractalSettings fractalSettings; // Defines which part of the fractal is visible on the screen
    protected FractalDrawer   fractalDrawer;   // Draws a fractal onto a bitmap using the current coordinates
    
    private Bitmap      offScreenBitmap; // Bitmap on which we are drawing the fractal and the orbit
    private Canvas      offScreenCanvas; 
    
    private Bitmap      onScreenBitmap; // Buffer Bitmap holding the final picture which is to be drawn
    private Canvas      onScreenCanvas;// This Buffer is needed in order to be able to save the visible image 
    
    protected Matrix	offscreenMatrix = new Matrix(); // Matrix used for the rotation of the Offscreen surface
    
    //private Bitmap      screenBitmap; // The image being displayed on the Surface 
    //private Canvas      screenCanvas; 
    
    protected AppStatus motionStatus = AppStatus.NO_MOTION;  // Describes the users action
      
    private final SurfaceHolder surfaceHolder;
    
	protected ScaleGestureDetector   scaleDetector;
	protected RotateGestureDetector  rotateDetector;
	protected MoveGestureDetector    moveDetector;

    protected SimpleTransform currentTransformation = new SimpleTransform(); 
    protected SimpleTransform fractalTransformation = new SimpleTransform(); 
    
    protected float mThetaBitmap = 0;
    protected float mScaleBitmap = 1;
    protected float mTranslateXBitmap = 0;
    protected float mTranslateYBitmap = 0;

    protected float mThetaFractal = 0;
    protected float mScaleFractal = 1;
    protected float mTranslateXFractal = 0;
    protected float mTranslateYFractal = 0;
    
    private Matrix  emptyMatrix = new Matrix(); // Matrix used for different drawings

  
    private  boolean   bIsProcessingIndicatorShown = false; // holds the last state of the processing indicator shown in the middle of the screen
    private  boolean   bUserIsTouchingTheScreen = false;
    private  boolean   bDiscardUserInput = false; // If we should not discard any user input
    
    /** 
     * @param context
     */
    public FractalSurfaceView(Context context) 
    {       
        super(context);
        
        this.context  = context;
        surfaceHolder = getHolder();
        
    	// Setup Gesture Detectors
        scaleDetector  = new ScaleGestureDetector(context, new ScaleListener());
        rotateDetector = new RotateGestureDetector(context, new RotateListener());
        moveDetector   = new MoveGestureDetector(context, new MoveListener());
    }
    

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh)
    {
    	System.out.println("View onSizeChanged()"); //$NON-NLS-1$
        if(w == 0 || h==0)
            return;
        
        fractalSettings = new FractalSettings(w,h); // Init the fractalSettings
        ((ActivityMain)context).updateControls(fractalSettings);
        
        fractalDrawer   = new FractalDrawer(fractalSettings); // Start the FractalDrawer
        
        offScreenBitmap   = Bitmap.createBitmap(fractalSettings.width,   
                								fractalSettings.height, 
                								Bitmap.Config.ARGB_8888);
        offScreenCanvas = new Canvas(offScreenBitmap); 
        
        
       
        
        onScreenBitmap   = Bitmap.createBitmap(fractalSettings.width,   
                								fractalSettings.height, 
                								Bitmap.Config.ARGB_8888);
        onScreenCanvas = new Canvas(onScreenBitmap);    
    }
    
   
    
    @Override
    /**
     *  Drawing of the SurfaceView is done here
     */
    public void run() 
    {
        while(isThreadRunning)
        {
        	doDrawing();
          
	        try 
	        {
	            Thread.sleep(40);  // Sleep the thread for some time before drawing again...
	        } catch (InterruptedException e) {e.printStackTrace();}
        } //while(running)
    } // run()
    
    
    

    /**
     * Takes care of drawing onto the SurfaceView. The function is called by the drawing thread.
     * 
     */
    private void doDrawing() 
    {	
    	if(fractalDrawer == null || offScreenCanvas == null || fractalSettings == null)
    		return;
    	
    	
    	switch(getAppStatus())
    	{
    	case MOTION:
    		bUserIsTouchingTheScreen = true;
    		
    		// User is dragging/scaling -  blit the offscreen surface using the current transformation
    		drawOffscreenBitmapOnDisplay(fractalTransformation.getMatrixCenterAnchored(fractalSettings.width, fractalSettings.height));
    		break;
    		
    		
    	case NO_MOTION:
    		
    		// If the user has just stopped touching the display...
    		if(bUserIsTouchingTheScreen == true)
    		{    			
    			bUserIsTouchingTheScreen = false;
    			// We will update the coordinates of the fractal 
    			// Start of slow processing...
    			setDiscardUserInput(true);
    			fractalSettings.scale(fractalTransformation.mScale);
        	    fractalSettings.rotate(fractalTransformation.mTheta);
        	    fractalSettings.translate(fractalTransformation.mTranslateX, fractalTransformation.mTranslateY);   
        	    // ..End of slow processing
        	    
        	   
        	    // Tell the activity to update its controls (slider, info texts, etc)
                ((ActivityMain)context).updateControls(fractalSettings);
        	    
        	    // Recalculate the fractal image using the new fractalSettings
        	    fractalDrawer.resetProcessing(); 
    		}
    		
    	
    		// We have data ready to be drawn...
    		if( fractalDrawer.drawFractal(offScreenCanvas, fractalSettings)>0 )
    		{   
    			fractalTransformation.reset();
    			setDiscardUserInput(false);
    			
    			// Draw the orbit onto the offscreen surface
    			OrbitDrawer.draw(fractalSettings, offScreenCanvas);
    			
    			drawOffscreenBitmapOnDisplay(emptyMatrix);
    		}
    		// No data yet - draw old
    		else
    		{
    			drawOffscreenBitmapOnDisplay(fractalTransformation.getMatrixCenterAnchored(fractalSettings.width, fractalSettings.height));
    		}
    		break;
    	}
    	
    	
    	vManageCalculationStatusBar();
    }


    /**
     *  Show or hide the status bar informing us about ongoing calculations.
     */
	private void vManageCalculationStatusBar() {
		if(bIsProcessingIndicatorShown != fractalDrawer.isCurrentlyProcessing())
    	{
    		bIsProcessingIndicatorShown = fractalDrawer.isCurrentlyProcessing();
    		((ActivityMain)context).setCalculationInProgress(bIsProcessingIndicatorShown);
    	}
	}


	/** Draws the offscreen bitmap onto the display
	 * @param mtrx  - Affine transformation to be used when drawing 
	 */
	private void drawOffscreenBitmapOnDisplay(Matrix mtrx) {
		if(surfaceHolder.getSurface().isValid())
    	{ 			
			// Final image is ready to be drawn - Let's buffer it in onScreenBitmap
			synchronized (onScreenBitmap) 
			{
				onScreenCanvas.drawColor(Color.BLACK);
				onScreenCanvas.drawBitmap(offScreenBitmap, mtrx, null);
			}
				
			// Now blit the image on the display
			Canvas canvas = surfaceHolder.lockCanvas(); 
			canvas.drawBitmap(onScreenBitmap, emptyMatrix, null); 
	        surfaceHolder.unlockCanvasAndPost(canvas);
    	}
	}


	/**
     * Called by the Activity when it resumes
     */
    public void onResume()
    {
           isThreadRunning = true;
           drawingThread = new Thread(this);
           drawingThread.start();
    }
         
    
    /**
     * Called by the Activity when paused
     */
    public void onPause()
    {
        boolean retry = true;
        isThreadRunning = false;

        while(retry)
        {
            try 
            {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }
     
    

    public boolean onTouchingEvent(MotionEvent event)
    {
    	// If we are drawing the orbits, we are only interested in the current touch coordinates
    	if(fractalSettings.isOrbitMode())
    	{
    		int touchX = Math.round(event.getRawX());
    		int touchY = Math.round(event.getRawY());
    		fractalSettings.setOrbitPoint(fractalSettings.getRealCoord_X(touchX, touchY),
    									  fractalSettings.getRealCoord_Y(touchX, touchY));
    		return true;
    	}     
    	
    	// Reset the values before reading the current state
    	currentTransformation.reset(); 
  	
        scaleDetector.onTouchEvent(event);  // stored in currentTransformation.mScale
        rotateDetector.onTouchEvent(event); // stored in currentTransformation.mTheta
        moveDetector.onTouchEvent(event);   // stored in currentTransformation.mTranslateX and mTranslateY
             
        if(isDiscardUserInput() == false)
    	{
        	fractalTransformation.post(currentTransformation);
        	updateMotionStatus(event); // Update the status of the application: i.e. MOTION or NO_MOTION
    	}
        

        return true;
    }
    
    

    /*
     *  Handler for the Menu buttons
     */
    public void onOptionsItemSelected(MenuItem item)
    {
        switch(MenuItems.values()[item.getItemId()])
        {
        case RESET:
            // Reset to original values
            fractalSettings.reset(this.getWidth(), this.getHeight());
            resetProcessing();
            // Tell the activity to update its controls (slider, info texts, etc)
            ((ActivityMain)context).updateControls(fractalSettings);
            break;
            
        case ANTIALIASING:
            // When the x8 Anti Aliasing button in the menu is pressed the fractal is redrawn using anti aliasing.
            fractalDrawer.resetProcessingWithAntialiasing();
            break;
            
        case SAVEIMAGE:
            saveFractalImage();
            break;
            
        case MANDELBROTSET:
            // Reset to original values
        	fractalSettings.reset(this.getWidth(), this.getHeight());
            fractalDrawer.resetProcessing();
            // Tell the activity to update its controls (slider, info texts, etc)
            ((ActivityMain)context).updateControls(fractalSettings);
            break;
            
        case CUSTOMFRACTAL:
            startCustomFractalDialog();
            break;
            
        case COLOR:
            fractalSettings.setColorMode(ColorMode.COLOR);
            break;
            
        case COLOR_INVERTED:
            fractalSettings.setColorMode(ColorMode.COLOR_INVERTED);
            break;
            
        case BW:
            fractalSettings.setColorMode(ColorMode.BW);
            break;
            
        case SHAREIMAGE:
            // First save the image
            File savedImage = saveFractalImage();
            // Share the file
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.setType("image/png"); //$NON-NLS-1$
            msg.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+savedImage)); //$NON-NLS-1$
            ((ActivityMain)context).startActivity(Intent.createChooser(msg, "Share image")); //$NON-NLS-1$
            break;
            
        case HIDEORBIT:
        	fractalSettings.setOrbitVisibility(false);
        	// Tell the activity to update its controls (slider, info texts, etc)
        	((ActivityMain)context).updateControls(fractalSettings);
        	break;
                
        default:
            break;
        }     
        
    }

    /*
    private void startGoToLocationDialog()
    {
         // Open a dialog box where the person can enter his 
         // desired coordinates and magnification.
         //
        
        // Create the dialog box
        final Dialog inputDialog = new Dialog(context);
        inputDialog.setTitle("GOTO desired location..."); //$NON-NLS-1$
  
        // Create a GridView
        final LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        
        // Create and add the three EditBoxes where the user will enter the coordinates of his desired position
        final EditText editTextX1 = new EditText(context); // x-coordinate inside the Cartesian plane of the window lower left corner
        final EditText editTextY1 = new EditText(context); // y-coordinate inside the Cartesian plane of the window lower left corner
        final EditText editTextX2 = new EditText(context); // x-coordinate inside the Cartesian plane of the window upper right corner
        // There is no need of editText for the Y2 coordinate!
        // The Y2 coordinate is calculated using the aspect ration of the display.
        
        // Create the OK button
        final Button okButton = new Button(context);
        okButton.setText(" OK "); //$NON-NLS-1$
        // Set the listener for the OK button
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) 
            {
            	double x1,y1,x2;
            	
            	// Get the values
            	try {
            		x1 = Double.valueOf(editTextX1.getText().toString()).doubleValue();
            		y1 = Double.valueOf(editTextY1.getText().toString()).doubleValue();
            		x2 = Double.valueOf(editTextX2.getText().toString()).doubleValue();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
            	
            	fractalSettings.setNewLocation(x1, y1, x2);
            	resetProcessing();
            	
                // Close the Dialog
                inputDialog.dismiss();
            }
        });
        
        
        // Set keyboard to be showing numbers
        editTextX1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextY1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextX2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //editTextY2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Add the buttons to the Linear Layout View
        layout.addView(editTextX1);
        layout.addView(editTextY1);
        layout.addView(editTextX2);
        //layout.addView(editTextY2);
        layout.addView(okButton);
        
        // Label the two edit boxes
        editTextX1.setHint("Enter value for X1"); //$NON-NLS-1$
        editTextY1.setHint("Enter value for Y1"); //$NON-NLS-1$
        editTextX2.setHint("Enter value for X2"); //$NON-NLS-1$
        //editTextY2.setHint("Enter value for Y2"); //$NON-NLS-1$
        
        inputDialog.setContentView(layout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        
        // Make Dialog visible
        inputDialog.show();
    }
    */
    
    @SuppressWarnings("deprecation")
	private void startCustomFractalDialog()
    {
        /* 
         * Create a Dialog with to EditBoxes where the user will enter the 
         * the Real and Imaginary part for the constant C which is used into the
         * equation Z[n+1]=Z[n]^2+C.
         */
        
        // Create the dialog box
        final Dialog inputDialog = new Dialog(context);
        inputDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputDialog.getWindow().setGravity(Gravity.BOTTOM);
        
        // Layouts - 
        final LinearLayout layoutMain   = new LinearLayout(context);   
        final LinearLayout layoutTop    = new LinearLayout(context);
        final LinearLayout layoutBottom = new LinearLayout(context);
        layoutMain.setLayoutParams  (new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        layoutTop.setLayoutParams   (new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));             
        layoutBottom.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        
        // EditBoxes - 
        final EditText  editTextForRealPart      = new EditText(context);
        final EditText  editTextForImaginaryPart = new EditText(context);
          //set the values
        DecimalFormat df = new DecimalFormat("###.######");         //$NON-NLS-1$
        editTextForRealPart.setText(df.format(fractalSettings.getImaginaryConstantRe()));
        editTextForImaginaryPart.setText(df.format(fractalSettings.getImaginaryConstantIm()));
          //styles
        editTextForRealPart.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT, 1));
        editTextForRealPart.setSingleLine();
        editTextForRealPart.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);         
        editTextForImaginaryPart.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT, 1));
        editTextForImaginaryPart.setSingleLine();
        editTextForImaginaryPart.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        
        // Sliders - 
        final SeekBar           sliderBarForRealPart     = new SeekBar(context); 
        final SeekBar           sliderBarForImaginaryPart= new SeekBar(context);      
        sliderBarForRealPart.setPadding(10, 0, 10, 0);
        sliderBarForRealPart.setMax(RANGE_CUSTOM_FRACTAL_SLIDERS);  
        sliderBarForRealPart.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,1));              
        sliderBarForImaginaryPart.setPadding(10, 0, 10, 0);
        sliderBarForImaginaryPart.setMax(RANGE_CUSTOM_FRACTAL_SLIDERS); 
        sliderBarForImaginaryPart.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,1));             
            // sliders initial values
        float fTemp;
        fTemp = ((float)fractalSettings.getImaginaryConstantRe()+2)*(RANGE_CUSTOM_FRACTAL_SLIDERS/4);
        sliderBarForRealPart.setProgress(Math.round(fTemp));
        fTemp = ((float)fractalSettings.getImaginaryConstantIm()+2)*(RANGE_CUSTOM_FRACTAL_SLIDERS/4);
        sliderBarForImaginaryPart.setProgress(Math.round(fTemp));
      
        // Texts - initial values
        final TextView textRe =  new TextView(context);
        final TextView textIm =  new TextView(context);                   
        textRe.setText("         Real part         "); //$NON-NLS-1$
        textIm.setText("       Imaginary part       "); //$NON-NLS-1$
        
        
        
        // Set to custom fractal and update the display
        fractalSettings.setFractalType(FractalCalculator.FractalTypes.JULIA);
        // Restart the fractal processing
        resetProcessing();       
     
        // Listener for [Re] editbox
        editTextForRealPart.setOnEditorActionListener (new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
            {
                Double dTemp;
                try {
                    dTemp = new Double(v.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
                
                // Set the desired number
                fractalSettings.setFractalType(FractalCalculator.FractalTypes.JULIA);
                fractalSettings.setImaginaryConstantRe(dTemp.doubleValue());
                resetProcessing();
                
                // Update the slider
                float fTemp2;
                fTemp2 = ((float)fractalSettings.getImaginaryConstantRe()+2)*(RANGE_CUSTOM_FRACTAL_SLIDERS/4);
                sliderBarForRealPart.setProgress(Math.round(fTemp2));
                
                return false;
            }
         });// EditText listener
        
        // Listener for [Im] editbox
        editTextForImaginaryPart.setOnEditorActionListener (new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
            {
                Double dTemp;
                try {
                    dTemp = new Double(v.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
                
                // Set the desired number
                fractalSettings.setFractalType(FractalCalculator.FractalTypes.JULIA);
                fractalSettings.setImaginaryConstantIm(dTemp.doubleValue());
                resetProcessing();
                
                // Update the slider position
                float fTemp1;
                fTemp1 = ((float)fractalSettings.getImaginaryConstantIm()+2)*(RANGE_CUSTOM_FRACTAL_SLIDERS/4);
                sliderBarForImaginaryPart.setProgress(Math.round(fTemp1));
                
                return false;
            }
         });// EditText listener
        
        // Slider for REAL part - range is from [-2, 2] 
        sliderBarForRealPart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1)
            {
                DecimalFormat df1 = new DecimalFormat("###.###"); //$NON-NLS-1$
                
                try
                {
                    float RealPart  = (4*sliderBarForRealPart.getProgress())/(float)RANGE_CUSTOM_FRACTAL_SLIDERS - 2; // 500 is considered 0
                    RealPart = Float.parseFloat(df1.format(RealPart)); // round the value
                    
                    editTextForRealPart.setText(Float.toString(RealPart));
                    
                    fractalSettings.setImaginaryConstantRe(RealPart);
                    resetProcessing();
                }catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            @Override
            public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser)
            {
                DecimalFormat df2 = new DecimalFormat("###.###"); //$NON-NLS-1$
                
                try
                {
                    fractalSettings.setFractalType(FractalCalculator.FractalTypes.JULIA);
                    float RealPart  = (4*sliderBarForRealPart.getProgress())/(float)RANGE_CUSTOM_FRACTAL_SLIDERS - 2; // 500 is considered 0
                    RealPart = Float.parseFloat(df2.format(RealPart));
                    
                    editTextForRealPart.setText(Float.toString(RealPart));
                    
                    fractalSettings.setImaginaryConstantRe(RealPart);
                    fractalDrawer.resetProcessing();
                }catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) 
            {}
         });  
                    
        // Slider for IMAGINARY part - range is from [-2, 2] 
        sliderBarForImaginaryPart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1)
            {
                DecimalFormat df3 = new DecimalFormat("###.###"); //$NON-NLS-1$
                try
                {
                    fractalSettings.setFractalType(FractalCalculator.FractalTypes.JULIA);
                    float Im = (4*sliderBarForImaginaryPart.getProgress())/(float)RANGE_CUSTOM_FRACTAL_SLIDERS -2; // 500 is considered 0
                    Im = Float.parseFloat(df3.format(Im));
                    
                    editTextForImaginaryPart.setText(Float.toString(Im));
                    
                    fractalSettings.setImaginaryConstantIm(Im);
                    resetProcessing();
                }catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            @Override
            public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser)
            {
                DecimalFormat df3 = new DecimalFormat("##.###"); //$NON-NLS-1$
                try {
                    fractalSettings.setFractalType(FractalCalculator.FractalTypes.JULIA);
                    float Im = (4*sliderBarForImaginaryPart.getProgress())/(float)RANGE_CUSTOM_FRACTAL_SLIDERS -2; // 500 is considered 0
                    Im = Float.parseFloat(df3.format(Im));
                
                    editTextForImaginaryPart.setText(Float.toString(Im));
                    
                    fractalSettings.setImaginaryConstantIm(Im);
                    fractalDrawer.resetProcessing();
                    
                }catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) 
            {}
         });  
        
        // Add the buttons to the bottom view
        layoutBottom.addView(editTextForRealPart);
        layoutBottom.addView(textRe);
        layoutBottom.addView(editTextForImaginaryPart);
        layoutBottom.addView(textIm);
        // Add the sliders to the top view
        layoutTop.addView(sliderBarForRealPart);
        layoutTop.addView(sliderBarForImaginaryPart);
        
        // Add the three views
        layoutMain.setOrientation(LinearLayout.VERTICAL);
        layoutMain.addView(layoutTop, 0);
        layoutMain.addView(layoutBottom, 1);
      
        // Add the mainLayout
        inputDialog.setContentView(layoutMain, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
             
        
        // Make Dialog visible
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(inputDialog.getWindow().getAttributes());
        lp.width = LayoutParams.FILL_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        inputDialog.show();
        inputDialog.getWindow().setAttributes(lp);
    }
    
    
    public void toOrbitMode(boolean b)
    {
        fractalSettings.setOrbitMode(b);
    }

    public void setIterationsLimmit(int iterLim)
    {
        fractalSettings.setIterationsLimmit(iterLim);
        resetProcessing();
    }

    public void setColorPeriodicity(int clrPer)
    {
        try {
			fractalSettings.setColorPeriodicity(clrPer);
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
    
    
    private File saveFractalImage()
    {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = Environment.getExternalStorageDirectory();
        // Create the directory where the pictures will be added
        File homeDir = new File(path, "PottyFract"); //$NON-NLS-1$
      
        homeDir.mkdirs();           
       
        // Create the file appending to it's name the current date and time
        Calendar calendar = new GregorianCalendar();     
        File     file     = new File(homeDir,  "FractalImage"      +  //$NON-NLS-1$
                                     calendar.get(Calendar.DATE)   + 
                                     calendar.get(Calendar.MONTH-1)+
                                     calendar.get(Calendar.YEAR)   + "-" + //$NON-NLS-1$
                                     calendar.get(Calendar.HOUR)   + 
                                     calendar.get(Calendar.MINUTE) +
                                     calendar.get(Calendar.SECOND) + ".png"); //$NON-NLS-1$

        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.   
            OutputStream os = new FileOutputStream(file);   
            synchronized (onScreenBitmap) 
            {
            	onScreenBitmap.compress(CompressFormat.PNG, 100, os);
			}
            
            os.close();
        } catch (FileNotFoundException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error FileNotFoundException " + file, e); //$NON-NLS-1$ //$NON-NLS-2$
        }catch(SecurityException e)
        {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "SecurityException" + file, e); //$NON-NLS-1$ //$NON-NLS-2$
        }catch(IOException e)
        {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "IOException" + file, e); //$NON-NLS-1$ //$NON-NLS-2$
            
        }
        
        return file;
    }
    
    
    protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) 
        {
        	currentTransformation.mScale = detector.getScaleFactor();   
            return true;
        }
    }

    protected class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) 
        {	
        	currentTransformation.mTheta = -detector.getRotationDegreesDelta();
            return true;
        }
    }   

    protected class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector)
        {
            PointF delta = detector.getFocusDelta();       
            currentTransformation.mTranslateX = delta.x;
            currentTransformation.mTranslateY = delta.y;
            return true;
        }
    }   
   
    
    
    protected void updateMotionStatus(MotionEvent event)
    {  	
    	switch(event.getAction())
    	{
    	case MotionEvent.ACTION_DOWN:
    	case MotionEvent.ACTION_MOVE:
    		
    		setAppStatus(AppStatus.MOTION);   
    		break;
    		
    	case MotionEvent.ACTION_UP:
    		setAppStatus(AppStatus.NO_MOTION);
    		break;
    	}	
    }
    
    
    
    private synchronized boolean isDiscardUserInput()
    {
    	return bDiscardUserInput;
    }
    private synchronized void setDiscardUserInput(boolean bInput)
    {
    	bDiscardUserInput = bInput;
    }
    
    private synchronized AppStatus getAppStatus()
    {
    	return motionStatus;
    }
    
    private synchronized void setAppStatus(AppStatus newStatus)
    {
    	motionStatus = newStatus; // Store the new status
    	
    	System.err.println("Motion status = " + newStatus.toString());
    }
    
    void resetProcessing()
    {
    	fractalDrawer.resetProcessing();
    }
    

    
}// MainView class


