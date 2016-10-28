package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

public class NightVision extends Filter {
    int mNoise_id=0;
    public NightVision(int w, int h,Object arg) {
        super(w, h);
        int res_id=(Integer)arg;
        mNoise_id=res_id;
    }
    void init()
    {
        mFramgment_glsl="nightvision_fragment_shader.glsl";
        filter_shader=new Shader();
        filter_shader.init(mVertext_glsl,mFramgment_glsl);
        int handle3=filter_shader.init_addtexName("s_noisetexture");
        filter_shader.init_commitResource();
    }
    @Override
    public void onInit() {

        init();
    }

    @Override
    public void onFRInit() {

    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

    public void init_FR() {
        Util.PiCoreLog("init_FR");
        ////if(VERTEX_SHADER!=null)
        {
            filter_FR_shader=new Shader();
            filter_FR_shader.init(mVertext_glsl,mFramgment_glsl);
            int handle3=filter_FR_shader.init_addtexName("s_noisetexture");
            filter_FR_shader.init_commitResource();
            mFRGLProgram=filter_FR_shader.getGLProgram();
            filter_FR_shader.setVF(false);
        }
    }
    @Override
    public void onDraw()
    {
        if(!frame_load)
        {
            Util.loadGLTextureFromResource(mNoise_id,  true,1,false);
            frame_load=true;
        }

    }
}

