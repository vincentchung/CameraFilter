#extension GL_OES_EGL_image_external : require
#define KERNEL_SIZE 25
precision lowp float;
#ifdef INPUT_VF
uniform samplerExternalOES s_viewfindertexture;
#else
uniform sampler2D s_viewfindertexture;
#endif
varying vec2 v_textureCoordinate;

uniform highp float u_texelWidth;
uniform highp float u_texelHeight;
uniform vec2 u_texelOffset[25];

void main() {

  vec4 c_sample[KERNEL_SIZE];

  c_sample[0] = texture2D(s_viewfindertexture, v_textureCoordinate - 2.0*u_texelWidth - 2.0*u_texelHeight);
  c_sample[1] = texture2D(s_viewfindertexture, v_textureCoordinate - u_texelWidth - 2.0*u_texelHeight);
  c_sample[2] = texture2D(s_viewfindertexture, v_textureCoordinate - 2.0*u_texelHeight);
  c_sample[3] = texture2D(s_viewfindertexture, v_textureCoordinate + u_texelWidth - 2.0*u_texelHeight);
  c_sample[4] = texture2D(s_viewfindertexture, v_textureCoordinate + 2.0*u_texelWidth - 2.0*u_texelHeight);

  c_sample[5] = texture2D(s_viewfindertexture, v_textureCoordinate - 2.0*u_texelWidth - u_texelHeight);
  c_sample[6] = texture2D(s_viewfindertexture, v_textureCoordinate - u_texelWidth - u_texelHeight);
  c_sample[7] = texture2D(s_viewfindertexture, v_textureCoordinate - u_texelHeight);
  c_sample[8] = texture2D(s_viewfindertexture, v_textureCoordinate + u_texelWidth - u_texelHeight);
  c_sample[9] = texture2D(s_viewfindertexture, v_textureCoordinate + 2.0*u_texelWidth - u_texelHeight);

  c_sample[10] = texture2D(s_viewfindertexture, v_textureCoordinate - 2.0*u_texelWidth);
  c_sample[11] = texture2D(s_viewfindertexture, v_textureCoordinate - u_texelWidth);
  c_sample[12] = texture2D(s_viewfindertexture, v_textureCoordinate);
  c_sample[13] = texture2D(s_viewfindertexture, v_textureCoordinate + u_texelWidth);
  c_sample[14] = texture2D(s_viewfindertexture, v_textureCoordinate + 2.0*u_texelWidth);

  c_sample[15] = texture2D(s_viewfindertexture, v_textureCoordinate - 2.0*u_texelWidth + u_texelHeight);
  c_sample[16] = texture2D(s_viewfindertexture, v_textureCoordinate - u_texelWidth + u_texelHeight);
  c_sample[17] = texture2D(s_viewfindertexture, v_textureCoordinate + u_texelHeight);
  c_sample[18] = texture2D(s_viewfindertexture, v_textureCoordinate + u_texelWidth + u_texelHeight);
  c_sample[19] = texture2D(s_viewfindertexture, v_textureCoordinate + 2.0*u_texelWidth + u_texelHeight);

  c_sample[20] = texture2D(s_viewfindertexture, v_textureCoordinate - 2.0*u_texelWidth + 2.0*u_texelHeight);
  c_sample[21] = texture2D(s_viewfindertexture, v_textureCoordinate - u_texelWidth + 2.0*u_texelHeight);
  c_sample[22] = texture2D(s_viewfindertexture, v_textureCoordinate + 2.0*u_texelHeight);
  c_sample[23] = texture2D(s_viewfindertexture, v_textureCoordinate + u_texelWidth + 2.0*u_texelHeight);
  c_sample[24] = texture2D(s_viewfindertexture, v_textureCoordinate + 2.0*u_texelWidth + 2.0*u_texelHeight);


  int i;
  vec4 minvalue = vec4(1.0);
  for (i=0; i<KERNEL_SIZE; i++)
    minvalue=min(c_sample[i], minvalue);

  gl_FragColor = minvalue;

 }
