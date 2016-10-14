#extension GL_OES_EGL_image_external : require
precision mediump float;
#ifdef INPUT_VF
	  uniform samplerExternalOES s_viewfindertexture;
	  #else
	  uniform sampler2D s_viewfindertexture;
	  #endif
varying vec2 v_textureCoordinate;
varying vec2 v_texelOffset[9];

void main() {

  vec4 samplevec[9];

  for (int i=0; i<9; i++) samplevec[i] = texture2D(s_viewfindertexture, v_textureCoordinate + v_texelOffset[i]);
  vec4 h = samplevec[2] + (2.0 * samplevec[5]) + samplevec[8] - (samplevec[0] + (2.0 * samplevec[3]) + samplevec[6]);
  vec4 v = samplevec[0] + (2.0 * samplevec[1]) + samplevec[2] - (samplevec[6] + (2.0 * samplevec[7]) + samplevec[8]);
  vec3 tmp = sqrt((h.rgb * h.rgb) + (v.rgb * v.rgb));
  float bw = dot(tmp, vec3(0.299, 0.587, 0.114)); // BW conversion
  gl_FragColor = vec4(bw, bw, bw, 1.0);
 }
