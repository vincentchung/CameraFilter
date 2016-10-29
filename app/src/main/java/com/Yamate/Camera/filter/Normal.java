package com.Yamate.Camera.filter;

import android.opengl.GLES20;

import com.Yamate.Camera.filter.Filter;
import com.Yamate.Camera.Shader;

import com.Yamate.Camera.Util;

public class Normal extends Filter {

    public Normal(int w, int h) {
        super(w, h);
        //pic_filter_id=FragmentShaders.FILTER_NONE;
        // TODO Auto-generated constructor stub
    }
    public void init()
    {
        filter_shader=new Shader();
        filter_shader.init(mVertext_glsl,mFramgment_glsl);
        //int handle1=filter_shader.init_addtexName("s_capturingtexture");
        filter_shader.init_commitResource();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //Util.loadGLTextureFromResource(R.drawable.texture, PicoreOpenGLTestActivity.getAppContext(), true);
    }
    @Override
    public void onInit() {


/*
        VERTEX_SHADER=VertexShaders.SIMPLE_VERTEX_SHADER;
        FRAGMENT_SHADER=FragmentShaders.SIMPLE_FRAGMENT_SHADER;
        mVertexShader=VertexShaders.mSimpleVertexShader;
        mFRVertexShader=VertexShaders.mFRSimpleVertexShader;
        //mVertexShader = Util.loadShader(GLES20.GL_VERTEX_SHADER, FragmentShaders.VF_DEFINE+VertexShaders.SIMPLE_VERTEX_SHADER);

        mPixelShader = Util.loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaders.VF_DEFINE+FragmentShaders.SIMPLE_FRAGMENT_SHADER);
        */
        init();
    }

    @Override
    public void onFRInit() {

    }

    @Override
    public void onSelected() {
        // TODO Auto-generated method stub

    }

}

