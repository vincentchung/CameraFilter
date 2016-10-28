package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.Shader;
import com.Yamate.Camera.Util;

public class Duocolor extends Filter {

    public float mKeyColorR=0.0f;
    public float mKeyColorG=0.0f;
    public float mKeyColorB=0.0f;

    public Duocolor(int w, int h) {
        super(w, h);
        //pic_filter_id=FragmentShaders.FILTER_NONE;
        // TODO Auto-generated constructor stub
    }
    void init()
    {
        mFramgment_glsl="duocolor_fragment_shader.glsl";
        filter_shader=new Shader();
        filter_shader.init(mVertext_glsl,mFramgment_glsl);
        //int handle1=filter_shader.init_addtexName("s_capturingtexture");
        filter_shader.init_commitResource();
        mGLProgram=filter_shader.getGLProgram();

        Util.setShaderVariableF(mGLProgram, "u_keyColorR", mKeyColorR);
        Util.setShaderVariableF(mGLProgram, "u_keyColorG", mKeyColorG);
        Util.setShaderVariableF(mGLProgram, "u_keyColorB", mKeyColorB);
        Util.setShaderVariableF(mGLProgram, "u_threshold", 0.8f); // 0.345f);
        Util.setShaderVariableF(mGLProgram, "u_slope", 0.8f); // 0.5f);
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //Util.loadGLTextureFromResource(R.drawable.texture, PicoreOpenGLTestActivity.getAppContext(), true);
    }
    @Override
    public void onInit() {
        init();
    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

