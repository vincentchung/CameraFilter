attribute vec4 a_vposition;
attribute vec2 a_inputTextureCoordinate;


uniform highp float u_texelWidth;
uniform highp float u_texelHeight;

varying vec2 v_textureCoordinate;
varying vec2 v_texelOffset[15];
uniform mat4 u_xformMat;

void main()
{
gl_Position = a_vposition;


vec2 widthStep = vec2(u_texelWidth, 0.0);
vec2 heightStep = vec2(0.0, u_texelHeight);
vec2 widthHeightStep = vec2(u_texelWidth, u_texelHeight);
vec2 widthNegativeHeightStep = vec2(u_texelWidth, -u_texelHeight);
#ifdef VF_INPUT
v_textureCoordinate = vec2(u_xformMat * vec4(a_inputTextureCoordinate, 1.0, 1.0));
#else
v_textureCoordinate = a_inputTextureCoordinate;
#endif
v_texelOffset[0] = vec2(-2.0*u_texelWidth, u_texelHeight);
v_texelOffset[1] = vec2(-u_texelWidth, u_texelHeight);
v_texelOffset[2] = vec2(0.00, u_texelHeight);
v_texelOffset[3] = vec2(u_texelWidth, u_texelHeight);
v_texelOffset[4] = vec2(2.0*u_texelWidth, u_texelHeight);

v_texelOffset[5] = vec2(-2.0*u_texelWidth, 0.00 );
v_texelOffset[6] = vec2(-u_texelWidth, 0.00 );
v_texelOffset[7] = vec2(0.0, 0.0);
v_texelOffset[8] = vec2(u_texelWidth, u_texelHeight);
v_texelOffset[9] = vec2(2.0*u_texelWidth, u_texelHeight);

v_texelOffset[10] = vec2(-2.0*u_texelWidth, -u_texelHeight);
v_texelOffset[11] = vec2(-u_texelWidth, -u_texelHeight);
v_texelOffset[12] = vec2(0.00, -u_texelHeight);
v_texelOffset[13] = vec2(u_texelWidth, -u_texelHeight);
v_texelOffset[14] = vec2(2.0*u_texelWidth, -u_texelHeight);

}