package com.Yamate.Camera.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public abstract class Filter {
	int mVertexShader=0;
    int mPixelShader=0;
    int Height=0;
    int Width=0;
    int mPictureHeight=0;
    int mPictureWidth=0;
    int mGLProgram=0;
    public Shader filter_shader=null;//for viewfinder shader

    int mFRVertexShader=0;
    int mFRGLProgram=0;
    public Shader filter_FR_shader=null;//for full res shader
    boolean frame_load=false;
    String VERTEX_SHADER=null;
    String FRAGMENT_SHADER=null;
    final static boolean GPU_FR_SUPPORT=true;
    int overlap=0;
    float lVFTexelHeight = 1.0f; // screen resolution, not viewfinder resolution!
    float lVFTexelWidth = 1.0f;
    int pic_filter_id=-1;
    int mSupport_blocks=4;
    int mPixels=2;
    //vertext_shader file path
    //fragment_shader file path
    String mVertext_glsl="simple_vertex_shader.glsl";
    String mFramgment_glsl="normal_fragment_shader.glsl";
    String mFramgment_FR_glsl="normal_fragment_shader_full.glsl";

	public abstract void onInit();
    public abstract void onFRInit();
	public abstract void onSelected();

	public int getBlocknumber()
	{
		return mSupport_blocks;
	}

	public void onRenderBlockDone(ByteBuffer bdata,int index)
	{
		Util.RenderFullMapRawBuffer(bdata,mPictureWidth,mPictureHeight,index,mPictureWidth/2,mPictureHeight/2);
	}

	public boolean getGPUsupport()
	{
		return GPU_FR_SUPPORT;
	}

	public void setPictureSize(int w,int h)
	{
		mPictureWidth=w;
		mPictureHeight=h;
	}

	public int getRenderPixels()
	{
		return mPixels;
	}

	public void processEffect(byte[] data,int width,int height)
	{

	}

	public void onDrawing()
	{
        if(filter_shader==null)
        {
        	onInit();
        	GLES20.glFlush();
            mGLProgram=filter_shader.getGLProgram();
    		Util.setShaderVariableF(mGLProgram, "u_texelHeight", lVFTexelHeight);
            Util.setShaderVariableF(mGLProgram, "u_texelWidth", lVFTexelWidth);
        }
        onDraw();
	}

	public void onDraw()
	{

	}
	public void init_FR()
	{
		Util.PiCoreLog("init_FR");
		{
			filter_FR_shader=new Shader();
			filter_FR_shader.setVF(false);
            filter_FR_shader.init(mVertext_glsl,mFramgment_glsl);
			filter_FR_shader.init_commitResource();

			mFRGLProgram=filter_FR_shader.getGLProgram();
			filter_FR_shader.setVF(false);
			Util.setShaderVariableF(mFRGLProgram, "u_texelHeight", lVFTexelHeight);
            Util.setShaderVariableF(mFRGLProgram, "u_texelWidth", lVFTexelWidth);
            onFRInit();
		}
	}

	public void unSelected()
	{

	}

	public Filter(int w, int h)
	{
		Height=h;
		Width=w;
		
		//load_all_gl();
		
		//init the capturing textture in all filter
/*
		onInit();
		float lTexelHeight = 1.0f/Height; // screen resolution, not viewfinder resolution!
        float lTexelWidth = 1.0f/Width;
        if(filter_shader!=null)
        {
        	mGLProgram=filter_shader.getGLProgram();
            if(mGLProgram!=0)
            {
            	Util.setShaderVariableF(mGLProgram, "u_texelHeight", lTexelHeight);
                Util.checkGlError("u_texelWidth and u_texelHeight");
                Util.setShaderVariableF(mGLProgram, "u_texelWidth", lTexelWidth);
                Util.checkGlError("u_texelWidth and u_texelHeight");
            }
            GLES20.glFlush();
        }
  */     
        
	}
	
	public void onTakePicture(int w,int h)
	{
		float lTexelHeight = 1.0f/h; // screen resolution, not viewfinder resolution!
        float lTexelWidth = 1.0f/w;
        setPictureSize(w,h);

		if(filter_FR_shader==null)
		{
			init_FR();
	        if(filter_FR_shader!=null)
	        {
	        	mFRGLProgram=filter_FR_shader.getGLProgram();
	            if(mFRGLProgram!=0)
	            {
	            	Util.setShaderVariableF(mFRGLProgram, "u_texelHeight", lTexelHeight);
	                Util.checkGlError("u_texelWidth and u_texelHeight");
	                Util.setShaderVariableF(mFRGLProgram, "u_texelWidth", lTexelWidth);
	                Util.checkGlError("u_texelWidth and u_texelHeight");
	            }
	            GLES20.glFlush();
	        }
		}
	}

	public void setFiliterEnable() {
		setFiliterInit(Width,Height);
		//onSelected();
	}
	
	protected Bitmap processFRframe(byte[] data,int area,int width,int height)
	{
		Bitmap outBmp=decodeframe(data,area,width,height,overlap);
		
		update_vertice(area, (float)outBmp.getWidth(), (float)outBmp.getHeight(),(float) width/2,(float) height/2,(float)overlap);
		return outBmp;
	}
	
	//input the jpeg data 
	//output Bitmap for the index
	protected Bitmap decodeframe(byte[] data,int area,int width,int height,int overlap)
	{
		Bitmap inputBmp;
		Bitmap outBmp;
		inputBmp=Util.decodingRegimage(data,data.length, area, width, height,overlap);
		//transfer the size to pow of 2

		outBmp=Util.drawGLsizeBmp(inputBmp);
		inputBmp.recycle();
		
		return outBmp;
        //return inputBmp;
	}
	protected void update_vertice(int index,float bW,float bH,float vW,float vH,float overlap)
	{
		filter_FR_shader.calculate_vertice(index, bW, bH, vW, vH, overlap);
	}
	
	public void onFinalRender()
	{
		
	}
	//rendering block index
	public Bitmap onFRrender(int index,byte[] data,int width,int height)
	{
		setPictureSize(width,height);
		//cut the data to one block
		GLES20.glUseProgram(mFRGLProgram);
		if(filter_FR_shader!=null)
		{
			int x=0,y=0;
			//seting the block number
			//ex: 2=>2x2, 5=>5x5
			Util.setShaderVariableI(mFRGLProgram, "u_tilesTotal",2);
			switch(index)
			{
			case 1:
				x=1;
				y=1;
				break;
			case 2:
				x=2;
				y=1;
				break;
			case 3:
				x=1;
				y=2;
				break;
			case 4:
				x=2;
				y=2;
				break;	
			}
			Util.setShaderVariableI(mFRGLProgram, "u_tilesXcurrent",x);
			
			Util.setShaderVariableI(mFRGLProgram, "u_tilesYcurrent",y);
			GLES20.glFlush();
		}
		return processFRframe(data,index,width,height);
	}
	
	public void setFiliterInit(int w,int h) {
		Height=h;
		Width=w;
		lVFTexelHeight = 1.0f/Height; // screen resolution, not viewfinder resolution!
        lVFTexelWidth = 1.0f/Width;

	}
	
	//public boolean renderCompressResult(ByteBuffer out)
		public boolean renderCompressResult(ByteArrayOutputStream outputStream)
		{
			Util.RawToJpeg(Util.GetPictureBuffer(), outputStream, mPictureWidth, mPictureHeight);
			return true;
		}
}
