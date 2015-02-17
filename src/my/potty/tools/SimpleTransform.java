package my.potty.tools;

import android.graphics.Matrix;

public class SimpleTransform
{
	public float mTheta = 0;
    public float mScale = 1;
    public float mTranslateX = 0;
    public float mTranslateY = 0;
    
    private Matrix matrix = new Matrix();
    
    
    /**
     *  Resets the transformation
     */
    public void reset()
    {
    	mTheta = 0;
        mScale = 1;
        mTranslateX = 0;
        mTranslateY = 0;
    }
    
    
    /** Concatenates new simple transformation
     * 
     * @param trans
     */
    public void post(SimpleTransform trans)
    {
    	mTheta += trans.mTheta;
        mScale *= trans.mScale;
        mTranslateX += trans.mTranslateX;
        mTranslateY += trans.mTranslateY;
    }
    
    
    
    public Matrix getMatrixCenterAnchored(int width, int height)
    {
    	matrix.reset();
    	matrix.setScale(mScale, mScale, width/2f, height/2f);
    	matrix.postRotate(mTheta, width/2f, height/2f);
    	matrix.postTranslate(mTranslateX, mTranslateY);
    	return matrix;
    }
}