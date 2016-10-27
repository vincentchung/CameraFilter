package com.Yamate.Camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by vincent on 2016/9/27.
 */

public class Util {
    static final private String TAG = "YCAMLog";
    static AtomicBoolean mCameraEnabled = new AtomicBoolean();
    static AtomicBoolean mUploading = new AtomicBoolean();
    static AtomicInteger mCameraFrameCount = new AtomicInteger();
    static AtomicInteger mReportedFrameCount = new AtomicInteger();
    static AtomicBoolean mInitCompleted = new AtomicBoolean();
    static AtomicBoolean mCapturing = new AtomicBoolean();

//managing GPU texture
    static class Res_buffer
    {
        ByteBuffer preloadimageBuffer=null;
        int width=0;
        int height=0;
        int resourceID=0;
        int tex_id=0;
    }

    static final int mResourceSize=10;
    static Res_buffer[] preloadResource = new Res_buffer[mResourceSize];
    static boolean pre_flag=false;
    //8 textures is limit
//GL_TEXTURE0 for viewfinder
//GL_TEXTURE1 for full res
    static final int TextureArray[]={GLES20.GL_TEXTURE2,GLES20.GL_TEXTURE3,GLES20.GL_TEXTURE4,GLES20.GL_TEXTURE5,GLES20.GL_TEXTURE6,GLES20.GL_TEXTURE7};
    static String[] mTextname= new String[mResourceSize];
    static int mTextcounter=0;

    //system req
    static Context mContext=null;

    //processing image buffer
    //Todo: working for captured image
    private static final Bitmap.Config bmp_decoding_config= Bitmap.Config.RGB_565;
    private static final int mFullWidthLimit=3264;
    private static final int mFullHeightLimit=2448;//1836;
    private static int mImageWidth=0;
    private static int mImageHeight=0;
    private static byte[] mFullres_array=new byte[mFullWidthLimit*mFullHeightLimit*2];

    private static final String STORE_DIR = "/mnt/sdcard/DCIM/Yamate";

    public static void PiCoreLog(String txt) {
        int threadID = android.os.Process.myTid();
        Log.v(TAG + " " + android.os.Process.myTid(), txt);
    }

    public static void setContext(Context context)
    {
        mContext=context;
    }


    public static String getStringFromFileInAssets(Context ctx, String filename) throws IOException {
        return getStringFromFileInAssets(ctx, filename, true);
    }

    public static String getStringFromFileInAssets(Context ctx, String filename, boolean useNewline) throws IOException
    {
        InputStream is = ctx.getAssets().open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
        {
            builder.append(line + (useNewline ? "\n" : ""));
        }
        is.close();
        return builder.toString();
    }


    /*
     * using buffer_index to assign any prepare buffer, -1 is mean assign buffer automatically
     *
     *
     * */

    private static int getNextHighestPO2( int n ) {
        n -= 1;
        n = n | (n >> 1);
        n = n | (n >> 2);
        n = n | (n >> 4);
        n = n | (n >> 8);
        n = n | (n >> 16);
        n = n | (n >> 32);
        return n + 1;

    }

    public static void resizeBmp(Bitmap inBmp,Bitmap outBmp)
    {
        BitmapDrawable image = new BitmapDrawable(inBmp);
        boolean scaleToPO2=false;

        int originalWidth = inBmp.getWidth();
        int originalHeight = inBmp.getHeight();

        int powWidth = getNextHighestPO2( originalWidth );
        int powHeight = getNextHighestPO2( originalHeight );

        image.setBounds( 0, 0, outBmp.getWidth(), outBmp.getHeight() );


        Canvas canvas = new Canvas( outBmp );
        outBmp.eraseColor(0);

        image.draw( canvas ); // draw the image onto our bitmap
    }
    /*
     * using buffer_index to assign any prepare buffer, -1 is mean assign buffer automatically
     *
     *
     * */
    public static Bitmap drawGLsizeBmp(Bitmap bmp)
    {
        BitmapDrawable image = new BitmapDrawable(bmp);
        Bitmap bitmap = null;
        boolean scaleToPO2=false;

        int originalWidth = bmp.getWidth();
        int originalHeight = bmp.getHeight();

        int powWidth = getNextHighestPO2( originalWidth );
        int powHeight = getNextHighestPO2( originalHeight );

        if ( scaleToPO2 ) {
            Util.PiCoreLog("Setting texture bounds to " + powWidth +"x"+ powHeight);
            image.setBounds( 0, 0, powWidth, powHeight );
        } else {
            Util.PiCoreLog("Setting texture bounds to " + originalWidth +"x"+ originalHeight);
            image.setBounds( 0, 0, originalWidth, originalHeight );
        }

        // Create an empty, mutable bitmap
        bitmap = Bitmap.createBitmap( powWidth, powHeight, bmp.getConfig() );


        Canvas canvas = new Canvas( bitmap );
        bitmap.eraseColor(0);

        image.draw( canvas ); // draw the image onto our bitmap
        return bitmap;
    }

    public static Bitmap loadResourceToBuffer(int resourceID, boolean scaleToPO2,boolean rotate)
    {
        Bitmap bitmap = null;
        bitmap=loadResourceTobmp(resourceID,mContext,scaleToPO2,rotate);
        return bitmap;
    }

    private static Bitmap loadResourceTobmp(int resourceID, Context context, boolean scaleToPO2,boolean rotate)
    {
        // pull in the resource
        Bitmap bitmap = null;
        Resources resources = context.getResources();

        Drawable image = resources.getDrawable( resourceID );
        float density = resources.getDisplayMetrics().density;

        int originalWidth = (int)(image.getIntrinsicWidth() / density);
        int originalHeight = (int)(image.getIntrinsicHeight() / density);

        int powWidth = getNextHighestPO2( originalWidth );
        int powHeight = getNextHighestPO2( originalHeight );

        if ( scaleToPO2 ) {
            Util.PiCoreLog("Setting texture bounds to " + powWidth +"x"+ powHeight);
            image.setBounds( 0, 0, powWidth, powHeight );
        } else {
            Util.PiCoreLog("Setting texture bounds to " + originalWidth +"x"+ originalHeight);
            image.setBounds( 0, 0, originalWidth, originalHeight );
        }

        // Create an empty, mutable bitmap

        bitmap = Bitmap.createBitmap( powWidth, powHeight, Bitmap.Config.ARGB_8888 );

        //bitmap = Bitmap.createBitmap( 960, 720, Bitmap.Config.ARGB_8888 );
        // get a canvas to paint over the bitmap
        Canvas canvas = new Canvas( bitmap );
        bitmap.eraseColor(0);

        image.draw( canvas ); // draw the image onto our bitmap

        //rotate bmp if we need

        // Setting post rotate to 90
        int ror=0;

        if(rotate)
            ror^=1;
        if((powWidth<powHeight))
            ror^=1;

        if(ror==1)
        {
            Matrix mtx = new Matrix();
            mtx.postRotate(270);

            // Rotating Bitmap
            Bitmap rotatedBMP = Bitmap.createBitmap(bitmap, 0, 0, powWidth, powHeight, mtx, true);
            return rotatedBMP;
        }

        //BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);

        return bitmap;
    }
    public static int loadGLTextureFromResource(int resourceID, Context context, boolean scaleToPO2,boolean rotate)
    {
        Bitmap bitmap = null;
        int pixelsize=4;
        bitmap=loadResourceTobmp(resourceID,context,scaleToPO2,rotate);
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bitmap.getHeight() * bitmap.getWidth() * 4);
        imageBuffer.order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(imageBuffer);

        if(bitmap.getConfig()==Bitmap.Config.ARGB_8888)
            pixelsize=4;
        else
            pixelsize=2;

        int textureId = Util.loadTextureFromBuffer( bitmap.getWidth(), bitmap.getHeight(),imageBuffer,true,-1,pixelsize);

        if(bitmap!=null)
            bitmap.recycle(); // texture has been loaded in GPU texture memory, so we can free the bitmap resource

        return 0;
    }


    //TRUE:  GL10.GL_REPEAT
    //FALSE: GL10.GL_CLAMP_TO_EDGE
    //pixel_szie:2->RBA565,4->RGBA8888
    public static int loadTextureFromBuffer(int width,int height,ByteBuffer imageBuffer,boolean gl_repeat,int tex,int pixel_szie)
    {
        int[] textures = new int[1];

        if(tex==-1)
        {
            GLES20.glGenTextures(1, textures, 0);
        }else
        {
            textures[0]=tex;
        }

        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        if(gl_repeat)
        {
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        }else
        {
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }

        if(pixel_szie==4)
            GLES20.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, imageBuffer);
        else if(pixel_szie==2)
            GLES20.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, width, height, 0, GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5, imageBuffer);

        PiCoreLog("Bitmap texture loaded to GPU, size "+ width + "x" + height + ", name " + textures[0]);
        checkGlError("load textures to GPU");
        return textures[0];
    }

    public static int loadGLTextureFromResource(int resourceID, boolean scaleToPO2, int buffer_index,boolean rotate)
    {
        mUploading.set(false);
        // pull in the resource
        Bitmap bitmap = null;
        int bIndex=buffer_index;

        //int textureId = loadTextureFromBitmap(bitmap);

        if(!pre_flag)
        {
            for(int i=0;i<mResourceSize;i++)
            {
                preloadResource[i]=new Res_buffer();
            }
            pre_flag=true;
        }

        if(bIndex==-1)
        {
            //looking for used buuffer
            int c=-1;//candidate index

            for(int i=0;i<mResourceSize;i++)
            {
                if(preloadResource[i].resourceID ==resourceID)
                {
                    //loaded
                    bIndex=i;
                    PiCoreLog("resource buffer:"+bIndex+" reused");
                    break;
                }else
                {
                    if(c==-1)
                    {
                        if(preloadResource[i].resourceID==0)
                        {
                            c=i;
                        }
                    }
                }
            }

            if(bIndex==-1)
                bIndex=c;
        }

        //reuse buffer no need to alloate
        if(preloadResource[bIndex].preloadimageBuffer==null)
        {
            bitmap=loadResourceTobmp(resourceID,mContext,scaleToPO2,rotate);
            ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bitmap.getHeight() * bitmap.getWidth() * 4);
            imageBuffer.order(ByteOrder.nativeOrder());
            bitmap.copyPixelsToBuffer(imageBuffer);

            imageBuffer.position(0);
            preloadResource[bIndex].preloadimageBuffer=imageBuffer;
            PiCoreLog("resource buffer:"+bIndex+" loaded");
            preloadResource[bIndex].height=bitmap.getHeight();
            preloadResource[bIndex].width=bitmap.getWidth();
            preloadResource[bIndex].tex_id=TextureArray[bIndex];
            GLES20.glActiveTexture(preloadResource[bIndex].tex_id);
        }else
        {
            if(buffer_index!=-1)
            {
                //replace the same buffer pointer
                bitmap=loadResourceTobmp(resourceID,mContext,scaleToPO2,rotate);
                ByteBuffer imageBuffer = preloadResource[bIndex].preloadimageBuffer;
                imageBuffer.order(ByteOrder.nativeOrder());
                bitmap.copyPixelsToBuffer(imageBuffer);

                imageBuffer.position(0);
                //preloadResource[bIndex].preloadimageBuffer=imageBuffer;
                PiCoreLog("resource buffer:"+bIndex+" replaced");
                preloadResource[bIndex].height=bitmap.getHeight();
                preloadResource[bIndex].width=bitmap.getWidth();
                preloadResource[bIndex].tex_id=TextureArray[bIndex];
                GLES20.glActiveTexture(preloadResource[bIndex].tex_id);
            }
        }

        preloadResource[bIndex].resourceID=resourceID;


        int textureId = Util.loadTextureFromBuffer( preloadResource[bIndex].width, preloadResource[bIndex].height,preloadResource[bIndex].preloadimageBuffer,true,-1,4);

        PiCoreLog("res"+preloadResource[bIndex].width+","+preloadResource[bIndex].height+","+preloadResource[bIndex].tex_id);

        if(bitmap!=null)
            bitmap.recycle(); // texture has been loaded in GPU texture memory, so we can free the bitmap resource
        mUploading.set(true);
        return textureId;
    }

    public static int addTextureName(String texname)
    {
        int hit=-1;
        for(int i=0;i<mTextcounter;i++)
        {
            if(mTextname[i]==texname)
            {
                hit=(i+1);
                break;
            }
        }
        if(hit==-1)
        {
            mTextname[mTextcounter]=texname;
            mTextcounter++;
            hit=mTextcounter;
        }

        return (hit+1);
    }

    public static void RenderFullMapRawBuffer(ByteBuffer bdata,int fullImageW,int fullImageH, int index, int dataw, int datah)
    {
        ByteBuffer bmap = ByteBuffer.wrap(mFullres_array);
        int read_line=fullImageW;
        int mapping_offset=0;
        bmap.position(0);
        int data_overlap_offect=0;
        int source_w=dataw;
        int source_h=datah;

        switch(index)
        {
            case 1:
                mapping_offset=0;
                //data_overlap_offect=0;
                break;
            case 2:
                mapping_offset=(fullImageW/2)*2;
                //data_overlap_offect=(PxOverlap)*2;
                break;
            case 3:
                mapping_offset=(fullImageW)*(fullImageH/2)*2;
                //data_overlap_offect=(source_w*PxOverlap)*2;
                break;
            case 4:
                mapping_offset=((fullImageW)*(fullImageH/2)+(fullImageW/2))*2;
                //data_overlap_offect=(source_w*PxOverlap+PxOverlap)*2;
                break;
        }
        data_overlap_offect=0;
        bdata.position(0);
        Util.PiCoreLog("read offset:"+mapping_offset+",index:"+index);
        //copy the buffer line by line
        for(int i=0;i<(fullImageH/2);i++)
        {
            System.arraycopy(bdata.array(), data_overlap_offect+(source_w*i)*2, mFullres_array,  mapping_offset+(fullImageW*i)*2, read_line);
        }
    }


    //openGL until function
    public static void checkGPUCapabilities(GL10 gl) {

        PiCoreLog("GPU capabilities:");
        int[] retint = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, retint, 0);
        PiCoreLog("Maximum size of the texture: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE, retint, 0);
        PiCoreLog("Maximum size of the renderbuffer: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, retint, 0);
        PiCoreLog("Maximum number of vertex attributes: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS, retint, 0);
        PiCoreLog("Maximum number of uniform vertex vectors: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS, retint, 0);
        PiCoreLog("Maximum number of uniform fragment vectors: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_VARYING_VECTORS, retint, 0);
        PiCoreLog("Maximum number of varying vectors: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, retint, 0);
        PiCoreLog("Maximum number of texture units usable in a vertex shader: " + retint[0]);

        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, retint, 0);
        PiCoreLog("Maximum number of texture units usable in a fragment shader: " + retint[0]);

        String retstr = GLES20.glGetString(GL10.GL_EXTENSIONS);
        PiCoreLog("Supported extensions: " + retstr);
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            PiCoreLog(op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public static void setShaderVariableI(int glProgram, String variableName, int value) {
        PiCoreLog("Setting shader variable (1I) " + variableName + " to " + value);
        int handle = GLES20.glGetUniformLocation(glProgram, variableName);
        Util.checkGlError("setShaderVariableI getUniformLocation");
        GLES20.glUniform1i(handle, value);
        Util.checkGlError("setShaderVariableI glUniform1i");
    }

    public static void setShaderVariableF(int glProgram, String variableName, float value) {
        PiCoreLog("Setting shader variable (1F) " + variableName + " to " + value);
        int handle = GLES20.glGetUniformLocation(glProgram, variableName);
        Util.checkGlError("setShaderVariableF getUniformLocation");
        GLES20.glUniform1f(handle, value);
        Util.checkGlError("setShaderVariableF glUniform1f");
    }

    public static void setShaderVariable1FV(int glProgram, String variableName, int len, float[] values) {
        PiCoreLog("Setting shader variable (2FV) " + variableName + " to " + values.toString());
        int i;
        for (i=0; i<len; i++) Log.v("PiCore", "Table value "+i+":"+values[i]);
        int handle = GLES20.glGetUniformLocation(glProgram, variableName);
        Util.checkGlError("setShaderVariableF getUniformLocation");
        GLES20.glUniform1fv(handle, len, values, 0);
        Util.checkGlError("setShaderVariableF glUniform1fv");
    }

    public static void setShaderVariable2FV(int glProgram, String variableName, int len, float[] values) {
        PiCoreLog("Setting shader variable (2FV) " + variableName + " to " + values.toString());
        int i;
        for (i=0; i<len; i++) Log.v("PiCore", "Table value "+i+":"+values[i]);
        int handle = GLES20.glGetUniformLocation(glProgram, variableName);
        Util.checkGlError("setShaderVariableF getUniformLocation");
        GLES20.glUniform2fv(handle, len/2, values, 0);
        Util.checkGlError("setShaderVariableF glUniform2fv");
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static int CreateGLprogram(String vertexSource, String fragmentSource,Boolean VF)
    {
        String vert="";
        String frag="";

        try {
            if(VF)
            {
                frag="#define INPUT_VF 1\n"+getStringFromFileInAssets(mContext,fragmentSource);
            }else
            {
                frag="#define TILE_MODE 1\n"+getStringFromFileInAssets(mContext,fragmentSource);
            }


            vert=getStringFromFileInAssets(mContext,vertexSource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vert);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, frag);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            return 0;
        }

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, pixelShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    public static void RawToJpeg(ByteBuffer src, ByteArrayOutputStream outputStream, int w, int h)
    {


        Bitmap bmp1=Bitmap.createBitmap(w, h, bmp_decoding_config);
        bmp1.copyPixelsFromBuffer(src);

        //ByteArrayOutputStream outputStream = null;
        //outputStream = new ByteArrayOutputStream();

        bmp1.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        bmp1.recycle();

        //if(out==null)
        //	out=ByteBuffer.allocate(outputStream.size());

        //System.arraycopy(outputStream.toByteArray(),0,out.array(),0,outputStream.size());
	/*
	try {
		outputStream.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/
    }

    private static String generateFileName() {
        String fileName = "ycamera_" + System.currentTimeMillis() + ".jpg";
        return fileName;
    }

    public static String ImageToFile(ByteArrayOutputStream baos)
    {
        String filename;

        filename=generateFileName();

        File dir = new File(STORE_DIR);
        OutputStream outputStream = null;
        File file = null;
        if (!dir.exists())
            dir.mkdirs();
        file = new File(dir, filename);

        FileOutputStream fos=null;
        try {
            fos = new FileOutputStream (file);

            baos.writeTo(fos);
        } catch(IOException ioe) {
            // Handle exception here
            ioe.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file.getAbsolutePath();
    }

    public static String RawToJpeg(byte[] data,int w,int h)
    {
        String filename;
        ByteBuffer bmap = ByteBuffer.wrap(data);
        Bitmap bmp1=Bitmap.createBitmap(w, h, bmp_decoding_config);
        Matrix matrix = new Matrix();
        //matrix.postRotate(180);
        matrix.preScale(1.0f, -1.0f);
        bmp1.copyPixelsFromBuffer(bmap);
        bmp1=Bitmap.createBitmap(bmp1,0,0,bmp1.getWidth(),bmp1.getHeight(),matrix,false);

        filename=generateFileName();

        File dir = new File(STORE_DIR);
        OutputStream outputStream = null;
        File file = null;
        if (!dir.exists())
            dir.mkdirs();
        file = new File(dir, filename);
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bmp1.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        bmp1.recycle();

        return file.getAbsolutePath();
        //MediaScannerNotifier m=new MediaScannerNotifier(mContext,file.getAbsolutePath(),"image/jpg");
        //m.onMediaScannerConnected();

    }

    private static void CopyExif(ExifInterface source,ExifInterface target) {
            target.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, source.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
            target.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, source.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            target.setAttribute(ExifInterface.TAG_DATETIME, source.getAttribute(ExifInterface.TAG_DATETIME));
            target.setAttribute(ExifInterface.TAG_MAKE, source.getAttribute(ExifInterface.TAG_MAKE));
            target.setAttribute(ExifInterface.TAG_MODEL, source.getAttribute(ExifInterface.TAG_MODEL));
            target.setAttribute(ExifInterface.TAG_ORIENTATION, source.getAttribute(ExifInterface.TAG_ORIENTATION));
            target.setAttribute(ExifInterface.TAG_WHITE_BALANCE, source.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
            target.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, source.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
            target.setAttribute(ExifInterface.TAG_FLASH, source.getAttribute(ExifInterface.TAG_FLASH));
            target.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, source.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));
            target.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, source.getAttribute(ExifInterface.TAG_GPS_DATESTAMP));
            target.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, source.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
            target.setAttribute(ExifInterface.TAG_GPS_LATITUDE, source.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            target.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,source.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
            target.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, source.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            target.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,source.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
    }
    public static String RawToJpeg(byte[] data, int w, int h, ExifInterface exittag)
    {
        String filename;
        ByteBuffer bmap = ByteBuffer.wrap(data);
        Bitmap bmp1=Bitmap.createBitmap(w, h, bmp_decoding_config);
        filename=generateFileName();

        File dir = new File(STORE_DIR);
        OutputStream outputStream = null;
        File file = null;
        if (!dir.exists())
            dir.mkdirs();
        file = new File(dir, filename);
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bmp1.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        bmp1.recycle();

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //adding exif data
        try {
            ExifInterface addexif =new ExifInterface(file.getAbsolutePath());
            CopyExif(exittag,addexif);
            addexif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
        //MediaScannerNotifier m=new MediaScannerNotifier(mContext,file.getAbsolutePath(),"image/jpg");
        //m.onMediaScannerConnected();

    }

    public static Bitmap decodingRegimage(byte[] data,int length,int area,int width,int height,int overlap)
    {
        Bitmap bmp1=null;
        BitmapFactory.Options BO=new BitmapFactory.Options();
        Rect decode_rect=new Rect();

        switch(area)
        {
            case 1:
                decode_rect.left=0;
                decode_rect.top=0;
                decode_rect.right=width/2;
                decode_rect.bottom=height/2;
                //adding overlap
                decode_rect.right+=overlap;
                decode_rect.bottom+=overlap;
                break;
            case 2:
                decode_rect.left=width/2;
                decode_rect.top=0;
                decode_rect.right=width;
                decode_rect.bottom=height/2;
                //adding overlap
                decode_rect.left-=overlap;
                decode_rect.bottom+=overlap;
                break;
            case 3:
                decode_rect.left=0;
                decode_rect.top=height/2;
                decode_rect.right=width/2;
                decode_rect.bottom=height;
                //adding overlap
                decode_rect.top-=overlap;
                decode_rect.right+=overlap;
                break;
            case 4:
                decode_rect.left=width/2;
                decode_rect.top=height/2;
                decode_rect.right=width;
                decode_rect.bottom=height;
                //adding overlap
                decode_rect.top-=overlap;
                decode_rect.left-=overlap;
                break;
        }
        //BO.inPreferredConfig=bmp1.getConfig();
        //BO.inPreferredConfig =Bitmap.Config.ARGB_8888;
        BO.inPreferredConfig =bmp_decoding_config;

        Util.PiCoreLog("decode rect left:"+decode_rect.left+",top:"+decode_rect.top+",r:"+decode_rect.right+",b:"+decode_rect.bottom);
        try {
            BitmapRegionDecoder bmpr =BitmapRegionDecoder.newInstance(data, 0, length, true);
            bmp1=bmpr.decodeRegion(decode_rect, BO);
            bmpr.recycle();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return bmp1;
    }

    public static int uploadFRTextureFromBuffer(int width,int height,ByteBuffer imageBuffer,int FRtex)
    {
        imageBuffer.position(0);
        PiCoreLog("uploadFRTextureFromBuffer w:"+width+",h:"+height);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //using RGB565
        int tex=Util.loadTextureFromBuffer( width, height,imageBuffer,false,FRtex,2);
        return tex;
    }

    public static ByteBuffer GetPictureBuffer()
    {
        ByteBuffer temp =ByteBuffer.wrap(mFullres_array);
        return temp;
    }

    public static int getCameraFrameCount()
    {
        return mCameraFrameCount.get();
    }

    public static int addCameraFrameCount()
    {
        return mCameraFrameCount.incrementAndGet();
    }

    public static void setCameraEnabled(boolean enabled) {
        mCameraEnabled.set(enabled);
    }

    public static boolean getCameraEnabled() {
        return mCameraEnabled.get();
    }
    public static void resetReportedFrameCounter() {
        mReportedFrameCount.set(0);
    }

    public static int getReportedFrameCounter() {
        return mReportedFrameCount.get();
    }

    public static int addReportedFrameCount()
    {
        return mReportedFrameCount.incrementAndGet();
    }

    public static void setInitCompleted(boolean enabled) {
        mInitCompleted.set(enabled);
    }

    public static boolean getInitCompleted() {
        return mInitCompleted.get();
    }

    public static void setCapturing(boolean enabled) {
        mCapturing.set(enabled);
    }

    public static boolean getCapturing() {
        return mCapturing.get();
    }
}
