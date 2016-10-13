attribute vec4 a_vposition;
attribute vec2 a_inputTextureCoordinate;
attribute vec2 a_frameTextureCoordinate;
uniform highp float u_texelWidth;
uniform highp float u_texelHeight;
varying vec2 v_textureCoordinate;
varying vec2 v_frameCoordinate;
varying  vec2 v_texelOffset[9];
uniform mat4 u_xformMat;

void main()
{
gl_Position = a_vposition;
#ifdef INPUT_VF
v_textureCoordinate = vec2(u_xformMat * vec4(a_inputTextureCoordinate, 1.0, 1.0));
v_frameCoordinate = a_frameTextureCoordinate;
#else
v_textureCoordinate = a_inputTextureCoordinate;
v_frameCoordinate = a_frameTextureCoordinate;
#endif
v_texelOffset[0] = vec2(-u_texelWidth, -u_texelHeight);
v_texelOffset[1] = vec2(0.00, -u_texelHeight);
v_texelOffset[2] = vec2(u_texelWidth, -u_texelHeight);
v_texelOffset[3] = vec2(-u_texelWidth, 0.00 );
v_texelOffset[4] = vec2(0.0, 0.0);
v_texelOffset[5] = vec2(u_texelWidth, 0.00);
v_texelOffset[6] = vec2(-u_texelWidth, u_texelHeight);
v_texelOffset[7] = vec2(0.00, u_texelHeight);
v_texelOffset[8] = vec2(u_texelWidth, u_texelHeight);

}