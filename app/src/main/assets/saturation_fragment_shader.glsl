#extension GL_OES_EGL_image_external : require
precision mediump float;
#ifdef INPUT_VF
uniform samplerExternalOES s_viewfindertexture;
#else
uniform sampler2D s_viewfindertexture;
#endif
varying vec2 v_textureCoordinate;
void main() {
  mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);
  		        lowp float saturation = 2.0;
  		        lowp vec4 textureColor = texture2D(s_viewfindertexture, v_textureCoordinate);
  		        lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
  		        lowp vec3 greyScaleColor = vec3(luminance);

  		  	    gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);
}