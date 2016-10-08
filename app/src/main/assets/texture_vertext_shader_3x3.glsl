attribute vec4 a_vposition;
attribute vec2 a_inputTextureCoordinate;
uniform  float u_texelWidth;
uniform  float u_texelHeight;

varying vec2 v_textureCoordinate;
varying vec2 v_surroundingTextureCoordinates[8];
uniform mat4 u_xformMat;

void main()
{
gl_Position = a_vposition;
vec2 widthStep = vec2(u_texelWidth, 0.0);
vec2 heightStep = vec2(0.0, u_texelHeight);
vec2 widthHeightStep = vec2(u_texelWidth, u_texelHeight);
vec2 widthNegativeHeightStep = vec2(u_texelWidth, -u_texelHeight);

v_textureCoordinate = vec2(u_xformMat * vec4(a_inputTextureCoordinate, 1.0, 1.0));
v_surroundingTextureCoordinates[0] = vec2(u_xformMat * vec4(a_inputTextureCoordinate-widthHeightStep, 1.0, 1.0));
v_surroundingTextureCoordinates[1] = vec2(u_xformMat * vec4(a_inputTextureCoordinate-heightStep, 1.0, 1.0));
v_surroundingTextureCoordinates[2] = vec2(u_xformMat * vec4(a_inputTextureCoordinate+widthNegativeHeightStep, 1.0, 1.0));
v_surroundingTextureCoordinates[3] = vec2(u_xformMat * vec4(a_inputTextureCoordinate-widthStep, 1.0, 1.0));
v_surroundingTextureCoordinates[4] = vec2(u_xformMat * vec4(a_inputTextureCoordinate+widthStep, 1.0, 1.0));
v_surroundingTextureCoordinates[5] = vec2(u_xformMat * vec4(a_inputTextureCoordinate-widthNegativeHeightStep, 1.0, 1.0));
v_surroundingTextureCoordinates[6] = vec2(u_xformMat * vec4(a_inputTextureCoordinate+heightStep, 1.0, 1.0));
v_surroundingTextureCoordinates[7] = vec2(u_xformMat * vec4(a_inputTextureCoordinate+widthHeightStep, 1.0, 1.0));
}
