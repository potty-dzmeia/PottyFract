package my.potty.fract;


import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;




/** Main Activity of the application. Holds the View with the UI interface.
 * 
 * @author Pottry
 *
 */
public class ActivityMain extends Activity
{
	protected FractalSurfaceView fractalSurfaceView; // The Main View
	
    protected Button   buttonOrbitMode;
    protected TextView textviewImgQuality;
    protected SeekBar  seekbarIterLimit;
    protected EditText edittextIterLimit;
    protected TextView textviewInfoText;
    protected TextView textViewClrCycle;
    protected SeekBar  seekbarClrPer;
    protected TextView textviewCycleCnt;
    
    protected ProgressBar processingIndicator;
    
    private static final int COLOR_MENU_GROUP = 144; // Identifier for the different color options in the menu
   
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {  
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        System.out.println("On Create -----------------------------");
      
        // Setup the SurfaceView
        fractalSurfaceView = new FractalSurfaceView(this);
        ((LinearLayout) findViewById(R.id.FractalHolderSurface)).addView(fractalSurfaceView);
 

        // Get references to widgets
        buttonOrbitMode   = ((Button)findViewById(R.id.toOrbitModeBtn));
        textviewImgQuality= ((TextView)findViewById(R.id.textViewImgQuality));
        seekbarIterLimit  = ((SeekBar)findViewById(R.id.seekBarImgQuality));
        edittextIterLimit = ((EditText)findViewById(R.id.editTextPictureQuality));
        textviewInfoText  = ((TextView)findViewById(R.id.textViewInf));
        textViewClrCycle  = ((TextView)findViewById(R.id.textViewClrCycle));
        seekbarClrPer     = ((SeekBar)findViewById(R.id.seekBarClrCycle));
        textviewCycleCnt  = ((TextView)findViewById(R.id.textViewCyclesCount));
        processingIndicator = ((ProgressBar)findViewById(R.id.progressBar1));
      
     
        
        // To OrbitMode button     
        buttonOrbitMode.setOnClickListener(new View.OnClickListener() {
           
            @Override
            public void onClick(View v) 
            {
                if(buttonOrbitMode.getText().toString().compareTo("To BrowseMode") == 0) //$NON-NLS-1$
                {
                    buttonOrbitMode.setText("To OrbitMode"); //$NON-NLS-1$
                    fractalSurfaceView.toOrbitMode(false);
                }
                else
                {
                    buttonOrbitMode.setText("To BrowseMode"); //$NON-NLS-1$
                    fractalSurfaceView.toOrbitMode(true);
                }
            }
        });
        
        // Max.Iteration seekbar  
        seekbarIterLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1)
            {
                fractalSurfaceView.setIterationsLimmit(seekBar1.getProgress());
                // Set the text into the box
                edittextIterLimit.setText(Integer.toString(seekBar1.getProgress()));
            }
            @Override
            public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser)
            {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) 
            {}
         });  
        
        
        // Max.Iteration editText      
        edittextIterLimit.setOnEditorActionListener (new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
            {
                Integer iTemp;
                try {
                    iTemp =  Integer.valueOf(v.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
                
                // Update the slider value
                seekbarIterLimit.setProgress(iTemp.intValue());
                fractalSurfaceView.setIterationsLimmit(iTemp.intValue());
                
                return true;
            }
         });// EditText listener
        
        // Clr.Per seekbar
        seekbarClrPer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1)
            {
                int clrPer = 1 + seekBar1.getProgress(); // add 1 because the first value is 0
                // Set the text on the right side of the slider to represent the value of the slider
                textviewCycleCnt.setText(Integer.toString(clrPer));
                fractalSurfaceView.setColorPeriodicity(clrPer);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser)
            {
            	if(!fromUser)
            		return; 
            	
                int clrPer = 1 + seekBar1.getProgress();
                // Set the text on the right side of the slider to represent the value of the slider
                textviewCycleCnt.setText(Integer.toString(clrPer)); 
                fractalSurfaceView.setColorPeriodicity(clrPer);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) 
            {}
         });  
        
    }
    
  





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, MenuItems.RESET.ordinal(),         0, "RESET"); //$NON-NLS-1$
        menu.add(0, MenuItems.TOGGLEBUTTONS.ordinal(), 0, "Hide Buttons"); //$NON-NLS-1$
        SubMenu colorOptionMenu = menu.addSubMenu(0, MenuItems.COLOROPTIONS.ordinal(), 0, "Color Options");
         colorOptionMenu.add(COLOR_MENU_GROUP, MenuItems.COLOR.ordinal(),           0, "Color");
         colorOptionMenu.add(COLOR_MENU_GROUP, MenuItems.COLOR_INVERTED.ordinal(),  0, "Color Inverted");
         colorOptionMenu.add(COLOR_MENU_GROUP, MenuItems.BW.ordinal(),  0, "B&W");
        SubMenu fractalTypeMenu = menu.addSubMenu(0,MenuItems.FRACTALTYPE.ordinal(),0, "Fractal Type"); // Fractal type with sub menu options
         fractalTypeMenu.add(0, MenuItems.MANDELBROTSET.ordinal(),  0, "Z[n+1]=Z[n]^2+C, where C=Z[0]");
         fractalTypeMenu.add(0, MenuItems.CUSTOMFRACTAL.ordinal(),  0, "Z[n+1]=Z[n]^2+C, where you choose the C");
        menu.add(0, MenuItems.ANTIALIASING.ordinal(),   0, "x8 AntiAliasing");
        menu.add(0, MenuItems.SAVEIMAGE.ordinal(),      0, "Save Image");
        menu.add(0, MenuItems.HIDEORBIT.ordinal(),    0, "Hide Orbit");
        menu.add(0, MenuItems.SHAREIMAGE.ordinal(),     0, "Share Image");
        menu.add(0, MenuItems.HELP.ordinal(),           0, "HELP!!!");  
        
        return true;
    }


    @SuppressWarnings("deprecation")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch(MenuItems.values()[item.getItemId()])
        {
        case TOGGLEBUTTONS:
            // Hide buttons
            if(item.getTitle().toString().compareTo("Hide Buttons") == 0) //$NON-NLS-1$
            {               
                hideControls();
                item.setTitle("Show Buttons"); //$NON-NLS-1$
            }
            // Show buttons
            else
            {  
                showControls();
                item.setTitle("Hide Buttons"); //$NON-NLS-1$
            }
            break;
            
            
        case HELP:
            // Create the TextView for the help text
            TextView helpText = new TextView(this);
            Resources res = getResources();
            helpText.setText(Html.fromHtml(res.getString(R.string.HelpText)));  //$NON-NLS-1$      
            // Create the Linear View
            final LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);
            // Add the to widgets to the LineraLAyout
            layout.addView(helpText);

            // Create the scroll view
            final ScrollView scrollView= new ScrollView(this);
            scrollView.addView(layout);
            // Create the Help dialog box
            final Dialog helpDialog = new Dialog(this);
            helpDialog.setTitle("About PottyFract"); //$NON-NLS-1$
            helpDialog.addContentView(scrollView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
            helpDialog.show();
            break;
            
        
        default:
                // The rest are handled by the View
                fractalSurfaceView.onOptionsItemSelected(item);
                break;
        }
        
        
        return true;
    }
    
    
    /**
     *  Updates the widgets on the screen using the supplied settings
     *  
     * @param fractalsettings - object describing the state of the widgets
     */
    public void updateControls(final FractalSettings fractalSettings)
    {
    	this.runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				 // Update the controls
		        seekbarIterLimit.setProgress(fractalSettings.getIterationsLimmit());
		        edittextIterLimit.setText(Integer.toString(fractalSettings.getIterationsLimmit()));
		        seekbarClrPer.setProgress(fractalSettings.getColorPeriodicity()-1);
		        textviewCycleCnt.setText(Integer.toString(fractalSettings.getColorPeriodicity()));
		        
		        // Draw the coordinates of the fractal being viewed
		        textviewInfoText.setText("x1="+fractalSettings.getCartX1()+"; y1="+fractalSettings.getCartY1()+";\n");   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		        textviewInfoText.append ("x2="+fractalSettings.getCartX2()+"; y2="+fractalSettings.getCartY2());     //$NON-NLS-1$ //$NON-NLS-2$

		        // Update the Mode button in the upper left corner
		        if(fractalSettings.isOrbitMode() == false)
		        {
		        	buttonOrbitMode.setText("To OrbitMode");	
		        }
		        else
		        {
		        	buttonOrbitMode.setText("To BrowseMode");
		        }
			}
		});
       
    }
    
    
    
    public void setCalculationInProgress(final boolean IsVisible)
    {
		this.runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				processingIndicator.setVisibility(IsVisible==true?View.VISIBLE:View.INVISIBLE);	
			}
		});
    }
    
    
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        System.out.println("On Resume -----------------------------");
        fractalSurfaceView.onResume();
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        System.out.println("On Pause -----------------------------");
        fractalSurfaceView.onPause();
    } 
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        System.out.println("On Destroy -----------------------------");
        //TODO add saving of the current fractal location
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return fractalSurfaceView.onTouchingEvent(event); // indicate event was handled
    }
    


    private void hideControls()
    {
        buttonOrbitMode.setVisibility(View.GONE);
        textviewImgQuality.setVisibility(View.GONE);
        seekbarIterLimit.setVisibility(View.GONE);
        edittextIterLimit.setVisibility(View.GONE);
        textviewInfoText.setVisibility(View.GONE);
        textViewClrCycle.setVisibility(View.GONE);
        seekbarClrPer.setVisibility(View.GONE);
        textviewCycleCnt.setVisibility(View.GONE);
    }
    
    private void showControls()
    {
        buttonOrbitMode.setVisibility(View.VISIBLE);
        textviewImgQuality.setVisibility(View.VISIBLE);
        seekbarIterLimit.setVisibility(View.VISIBLE);
        edittextIterLimit.setVisibility(View.VISIBLE);
        textviewInfoText.setVisibility(View.VISIBLE);
        textViewClrCycle.setVisibility(View.VISIBLE);
        seekbarClrPer.setVisibility(View.VISIBLE);
        textviewCycleCnt.setVisibility(View.VISIBLE);
        
    }
}// MainActivity
    