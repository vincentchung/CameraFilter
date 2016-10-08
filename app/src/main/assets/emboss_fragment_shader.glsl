#extension GL_OES_EGL_image_external : require
precision highp float;
#ifdef INPUT_VF
uniform samplerExternalOES s_viewfindertexture;
#else
uniform sampler2D s_viewfindertexture;
#endif

varying vec2 v_textureCoordinate;
varying vec2 v_texelOffset[9];
uniform float u_tintR;
uniform float u_tintG;
uniform float u_tintB;

void main() {
   float kerEmboss[9];

    kerEmboss[0] = 2.;
    kerEmboss[1] = 0.;
    kerEmboss[2] = 0.;
    kerEmboss[3] = 0.;
    kerEmboss[4] = -1.;
    kerEmboss[5] = 0.;
    kerEmboss[6] = 0.;
    kerEmboss[7] = 0.;
    kerEmboss[8] = -1.;

   float matr[9];
   float matg[9];
   float matb[9];
	 int i = 0;	 for (i=0; i<9; i++) { matr[i] = texture2D(s_viewfindertexture, v_textureCoordinate+v_texelOffset[i])[0]; }
	 for (i=0; i<9; i++) { matg[i] = texture2D(s_viewfindertexture, v_textureCoordinate+v_texelOffset[i])[1]; }
	 for (i=0; i<9; i++) { matb[i] = texture2D(s_viewfindertexture, v_textureCoordinate+v_texelOffset[i])[2]; }

   float mata[9];
  for (i=0; i<9; i++)  mata[i] = (matr[i] + matg[i] + matb[i]) / 3.0;

	float res = 0.0;
 for (i=0; i<9; i++) res += kerEmboss[i] * mata[i];

	float resr = clamp(res + 1./2. + u_tintR, 0.0, 1.0);
	float resg = clamp(res + 1./2. + u_tintG, 0.0, 1.0);
	float resb = clamp(res + 1./2. + u_tintB, 0.0, 1.0);

 gl_FragColor = vec4(resr, resg, resb, 1.0);

}
