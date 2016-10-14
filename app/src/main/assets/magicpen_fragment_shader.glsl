#extension GL_OES_EGL_image_external : require
	  precision mediump float;
	  #ifdef INPUT_VF
	  uniform samplerExternalOES s_viewfindertexture;
	  #else
	  uniform sampler2D s_viewfindertexture;
	  #endif
	  varying vec2 v_textureCoordinate;
	  void main() {

	   vec2 tc_offset[9];
	   vec4 sample4[9];
	   tc_offset[0] = vec2(-0.0028125, 0.0028125);
	   tc_offset[1] = vec2(0.00, 0.0028125);
	   tc_offset[2] = vec2(0.0028125, 0.0028125);
	   tc_offset[3] = vec2(-0.0028125, 0.00 );
	   tc_offset[4] = vec2(0.0, 0.0);
	   tc_offset[5] = vec2(0.0028125, 0.0028125);
	   tc_offset[6] = vec2(-0.0028125, -0.0028125);
	   tc_offset[7] = vec2(0.00, -0.0028125);
	   tc_offset[8] = vec2(0.0028125, -0.0028125);

	   for (int i = 0; i < 9; i++) sample4[i] = texture2D(s_viewfindertexture, v_textureCoordinate + tc_offset[i]);

	   vec4 horizEdge = sample4[2] + (2.0*sample4[5]) + sample4[8] - (sample4[0] + (2.0*sample4[3]) + sample4[6]);
	   vec4 vertEdge = sample4[0] + (2.0*sample4[1]) + sample4[2] - (sample4[6] + (2.0*sample4[7]) + sample4[8]);

	   vec3 tmp = sqrt((horizEdge.rgb * horizEdge.rgb) + (vertEdge.rgb * vertEdge.rgb));

	   gl_FragColor.rgb = tmp;
	   gl_FragColor.a = 1.0;
	   }