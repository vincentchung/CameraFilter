package com.Yamate.Camera;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Shader {
       private int mGLProgram=0;

       private int mTriangleVerticesHandle;
       private int mTransformHandle;;
       private int mRunningTimeHandle;
       private boolean preload=true;
       private int mVertexShader=0;
       private int mPixelShader=0;
       private FloatBuffer mTextureVertices;
       private FloatBuffer mFullTextureVertices;
       private FloatBuffer mQuadVertices;

       private int mViewfinderTexHandle;
       private int mTexCoordHandle;
       private int mFrameTexCoordHandle;

       private boolean mViewfinderUse=true;

       int mViewHeight=0;
       int mViewWidth=0;

       private final float[] FULL_TEXTURE_VERTICES =
               { 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
           //{ 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };
    private float[] TEXTURE_VERTICES =
            { 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
          //{ 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };

            /*{ 1632.0f/2048.0f, 918.0f/1024.0f,
               0.0f, 918.0f/1024.0f,
               0.0f, 0.0f,
               1632.0f/2048.0f, 0.0f };
*/
       /*
        * 2       1
        * +---+---+
        * |   |   |
        * +---+---+
        * |   |   |
        * +---+---+
        * 3        4
        * */
      /*
       private final float[] FR_TEXTURE_VERTICES =
             { 1632.0f/2048.0f, 918.0f/1024.0f,
               0.0f, 918.0f/1024.0f,
               0.0f, 0.0f,
               1632.0f/2048.0f, 0.0f };
       */
       private final float[] QUAD_VERTICES =
           { 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f };

       private final static int FLOAT_SIZE_BYTES = 4;

       int[] mTexnamehandle={0,0,0};//max 3
       int[] mTexidhandle={0,0,0};//max 3
       int mTexcounter=0;
       int full_tex=0;

       public Shader()
       {}

       public void setViewfinderSize(int w,int h)
       {
           mViewHeight=h;
           mViewWidth=w;
       }

    public void init(String vertex_path,String Pixel_path)
    {
        //preload=pre;
        //mVertexShader=vs;
        //mPixelShader=ps;

        //mVFTextureVertices=null;
        mTextureVertices=null;
        mQuadVertices=null;
        mFullTextureVertices=null;

        mTextureVertices = ByteBuffer.allocateDirect(TEXTURE_VERTICES.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureVertices.put(TEXTURE_VERTICES).position(0);

        mFullTextureVertices = ByteBuffer.allocateDirect(FULL_TEXTURE_VERTICES.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mFullTextureVertices.put(FULL_TEXTURE_VERTICES).position(0);

        mQuadVertices = ByteBuffer.allocateDirect(QUAD_VERTICES.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mQuadVertices.put(QUAD_VERTICES).position(0);

        //for full res
        mViewfinderUse=preload;
        create_preload_program(vertex_path,Pixel_path);
    }

       public void init_addRourceToTex(int res_id,boolean rotate)
       {
           Util.PiCoreLog("init_loadresource");
           //GLES20.glActiveTexture(tex_id);
           Util.loadGLTextureFromResource(res_id, true,-1,rotate);
       }

       public void init_commitResource()
       {
           Util.checkGlError("load textures to GPU");
           Util.PiCoreLog("init_commitResource");
           GLES20.glUseProgram(mGLProgram);
           Util.checkGlError("initialization");

           for(int i=0;i<mTexcounter;i++)
           {
               Util.PiCoreLog("glUniform1i:"+mTexnamehandle[i]+","+mTexidhandle[i]);
               GLES20.glUniform1i(mTexnamehandle[i], mTexidhandle[i]);
           }
           GLES20.glUniform1i(mViewfinderTexHandle, 1);
           Util.checkGlError("added texture handler");
       }

       public int init_addtexName(String texname)
       {
           int tex=GLES20.glGetUniformLocation(mGLProgram, texname);
           mTexidhandle[mTexcounter]=Util.addTextureName(texname);
           mTexnamehandle[mTexcounter]=tex;

           Util.PiCoreLog("init_addtexName:"+texname+",id:"+tex+", "+mTexidhandle[mTexcounter]+",p:"+mGLProgram);
           if(tex!=-1)
           mTexcounter++;

           return tex;
       }

    //new one for reading rs file
    void create_preload_program(String vert,String frag)
    {
        Util.PiCoreLog(vert);
        Util.PiCoreLog(frag);
        //todo: changing the code here to create program index for the shader!!!
        //think: the name still keep using shader? or GLprogram?
        mGLProgram=Util.CreateGLprogram(vert,frag,mViewfinderUse);



        mViewfinderTexHandle = GLES20.glGetUniformLocation(mGLProgram, "s_viewfindertexture");
        Util.PiCoreLog("mViewfinderTexHandle:"+mViewfinderTexHandle);
        mTexCoordHandle = GLES20.glGetAttribLocation(mGLProgram, "a_inputTextureCoordinate");
        Util.PiCoreLog("mTexCoordHandle:"+mTexCoordHandle);
        mFrameTexCoordHandle = GLES20.glGetAttribLocation(mGLProgram, "a_frameTextureCoordinate");
        Util.PiCoreLog("mFrameTexCoordHandle:"+mFrameTexCoordHandle);
        mTriangleVerticesHandle = GLES20.glGetAttribLocation(mGLProgram, "a_vposition");
        mTransformHandle = GLES20.glGetUniformLocation(mGLProgram, "u_xformMat");
        mRunningTimeHandle = GLES20.glGetUniformLocation(mGLProgram, "u_runningTime");
        Util.checkGlError("glGetUniformLocations");

/*
           if(mViewfinderUse)
               GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mVFTextureVertices);
           else
               GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mFRTextureVertices);
 */

        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureVertices);
        //GLES20.glVertexAttribPointer(mFrameTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mFullTextureVertices);
        GLES20.glVertexAttribPointer(mTriangleVerticesHandle, 2, GLES20.GL_FLOAT, false, 0, mQuadVertices);
        Util.checkGlError("glVertexAttribPointer");

    }

/*
*
* moving the texture view in GL block
*
* block size:  bW,bH
* view size:   vW,vH
* overlap:     op
*
* block
*
* (0,1)B           (1,1)A
* +----------------+
* |      overlap   |
* +    +------+    +
* |    | view |    |
* |    |      |    |
* +    +------+    +
* |   overlap      |
* +----------------+
* (0,0)C           (1.0)D
*
* {(A)        ,(B)    ,(C),    (D)}
* {vW/bW,vH/bH,0,vH/bH,0,0,vW/bW,0}
* example:
*
*
* overlap is zero
* block size and view size are the same
* { 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };
*
* overlap is 0 but the view size and block size are different
* +---------+
* | block   |
* +------+  +
* | view |  |
* +------+--+
* block:2048x2048, view:1632x918
* { 1632.0f/2048.0f, 918.0f/1024.0f,
               0.0f, 918.0f/1024.0f,
               0.0f, 0.0f,
               1632.0f/2048.0f, 0.0f };
*
* calculate the vertice by overlap
* */
       public void calculate_vertice(int blockindex,float RS_width,float RS_height,float viewsizeW,float viewsizeH,float overlap)
       {
           Util.PiCoreLog("bW:"+RS_width+",bH:"+RS_height+"vW:"+viewsizeW+",vH:"+viewsizeH);
           switch(blockindex)
           {
           case 1:
               //1
               TEXTURE_VERTICES[0]=(float)viewsizeW/(float)RS_width;
               TEXTURE_VERTICES[1]=(float)viewsizeH/(float)RS_height;
               //2
               TEXTURE_VERTICES[2]=0.0f;
               TEXTURE_VERTICES[3]=(float)viewsizeH/(float)RS_height;
               //4
               TEXTURE_VERTICES[4]=0.0f;
               TEXTURE_VERTICES[5]=0.0f;
               //3
               TEXTURE_VERTICES[6]=(float)viewsizeW/(float)RS_width;
               TEXTURE_VERTICES[7]=0.0f;

           break;
           case 2:
               //block 2 moving to right 1 overlap
                       //1
               TEXTURE_VERTICES[0]=(float)(viewsizeW+overlap)/RS_width;
               TEXTURE_VERTICES[1]=(float)viewsizeH/(float)RS_height;
                       //2
               TEXTURE_VERTICES[2]=(float)overlap/(float)RS_width;
               TEXTURE_VERTICES[3]=(float)viewsizeH/(float)RS_height;
                       //3
               TEXTURE_VERTICES[4]=(float)overlap/(float)RS_width;
               TEXTURE_VERTICES[5]=0.0f;
                       //4
               TEXTURE_VERTICES[6]=(float)(viewsizeW+overlap)/(float)RS_width;
               TEXTURE_VERTICES[7]=0.0f;

           break;
           case 3:
               //block 3
                       //1
               TEXTURE_VERTICES[0]=(float)viewsizeW/(float)RS_width;
               TEXTURE_VERTICES[1]=((float)(viewsizeH+overlap))/(float)RS_height;
                       //2
               TEXTURE_VERTICES[2]=0.0f;
               TEXTURE_VERTICES[3]=(float)(viewsizeH+overlap)/(float)RS_height;
                       //3
               TEXTURE_VERTICES[4]=0.0f;
               TEXTURE_VERTICES[5]=(float)overlap/(float)RS_height;
                       //4
               TEXTURE_VERTICES[6]=(float)viewsizeW/(float)RS_width;
               TEXTURE_VERTICES[7]=(float)overlap/(float)RS_height;
           break;
           case 4:
               //block 4
                       //1
               TEXTURE_VERTICES[0]=(float)(viewsizeW+overlap)/(float)RS_width;
               TEXTURE_VERTICES[1]=((float)viewsizeH+(float)overlap)/(float)RS_height;
                       //2
               TEXTURE_VERTICES[2]=(float)overlap/(float)RS_width;
               TEXTURE_VERTICES[3]=(float)(viewsizeH+overlap)/(float)RS_height;
                       //4
                       TEXTURE_VERTICES[4]=(float)overlap/(float)RS_width;
                       TEXTURE_VERTICES[5]=(float)overlap/(float)RS_height;
                       //3
                       TEXTURE_VERTICES[6]=(float)(viewsizeW+overlap)/(float)RS_width;
                       TEXTURE_VERTICES[7]=(float)overlap/(float)RS_height;
           break;
           }

           //update the mTextureVertices by new vertices
           mTextureVertices.put(TEXTURE_VERTICES).position(0);
           //
           String temp="index:"+blockindex+"TV:";
           for(int i=0;i<8;i++)
           {
               temp+=TEXTURE_VERTICES[i]+",";
           }
           Util.PiCoreLog(temp);
       }


       public int getGLProgram()
       {
           return mGLProgram;
       }

       public int getViewfinderHandle()
       {
           return mViewfinderTexHandle;
       }

       public int getRunningTimeHandle()
       {
           return mRunningTimeHandle;
       }

       public int getTexCoordHandle()
       {
           return mTexCoordHandle;
       }

       public int getFrameTexCoordHandle()
       {
           return mFrameTexCoordHandle;
       }

       public int getTriangleVHandle()
       {
           return mTriangleVerticesHandle;
       }

       public int getTransformHandle()
       {
           return mTransformHandle;
       }

       public FloatBuffer getTextureVertices()
       {
           return mTextureVertices;
           /*
           if(mViewfinderUse)
              return mVFTextureVertices;
           else
               return mFRTextureVertices;
               */
       }
       /*
       public void setTextureVertices(FloatBuffer TV)
       {
           if(mViewfinderUse)
               mVFTextureVertices.put(TV.array()).position(0);
           else
               mFRTextureVertices.put(TV.array()).position(0);
       }
       */

       public FloatBuffer getQuadVertices()
       {
           return mQuadVertices;
       }

       public void setVF(boolean t)
       {
           mViewfinderUse=t;

       }
       public boolean getVF()
       {
           return mViewfinderUse;
       }
   }
   