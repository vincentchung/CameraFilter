#extension GL_OES_EGL_image_external : require
precision mediump float;
#ifdef INPUT_VF
	  uniform samplerExternalOES s_viewfindertexture;
	  #else
	  uniform sampler2D s_viewfindertexture;
	  #endif
varying vec2 v_textureCoordinate;
uniform float u_keyColorR;
uniform float u_keyColorG;
uniform float u_keyColorB;
uniform float u_threshold;
uniform float u_slope;

#define DISTANCE_METHOD 1

void main(void) {
  float alpha;
  vec3 keyColor = vec3(u_keyColorR, u_keyColorG, u_keyColorB);
  vec3 inputColor = texture2D(s_viewfindertexture, v_textureCoordinate).rgb;
  float bwColor = dot(inputColor, vec3(0.299, 0.587, 0.114));

	#if DISTANCE_METHOD == 1
  float d = abs(distance(keyColor, inputColor));
#elif DISTANCE_METHOD == 0
  float d = abs(length(abs(keyColor.rgb - inputColor.rgb)));
#endif

  float edge = u_threshold * (1.0 - u_slope);
  alpha = smoothstep(edge, u_threshold, d);

  gl_FragColor = vec4(mix(inputColor, vec3(bwColor, bwColor, bwColor), alpha), 1.0);
}
