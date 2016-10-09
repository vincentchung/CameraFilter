#extension GL_OES_EGL_image_external : require
precision mediump float;
#ifdef INPUT_VF
uniform samplerExternalOES s_viewfindertexture;
#else
uniform sampler2D s_viewfindertexture;
#endif
varying vec2 v_textureCoordinate;
void main() {
  vec3 rgb = texture2D(s_viewfindertexture, v_textureCoordinate).rgb;
  
  float newr = clamp(rgb.r -0.64 + 0.44, 0.0, 1.0);
  float newg = clamp(rgb.g -0.172 + 0.44, 0.0, 1.0);
  float newb = clamp(rgb.b -0.164 + 0.44, 0.0, 1.0);
  gl_FragColor = vec4(newr, newg, newb, 1.0);
}