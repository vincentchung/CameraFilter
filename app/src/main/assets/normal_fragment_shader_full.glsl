#extension GL_OES_EGL_image_external : require
precision mediump float;
#ifdef INPUT_VF
uniform samplerExternalOES s_viewfindertexture;
#else
uniform sampler2D s_viewfindertexture;
#endif
varying vec2 v_frameCoordinate;
void main() {
  float a = 0.0;
  float b = 0.0;
  gl_FragColor = texture2D(s_viewfindertexture, v_frameCoordinate);
}