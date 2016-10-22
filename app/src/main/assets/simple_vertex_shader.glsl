attribute vec4 a_vposition;
attribute vec2 a_inputTextureCoordinate;
attribute vec2 a_frameTextureCoordinate;
varying vec2 v_textureCoordinate;
varying vec2 v_frameCoordinate;
uniform mat4 u_xformMat;
void main() {
gl_Position = a_vposition;
#ifdef INPUT_VF
v_textureCoordinate = vec2(u_xformMat * vec4(a_inputTextureCoordinate, 1.0, 1.0));
v_frameCoordinate = vec2(u_xformMat * vec4(a_frameTextureCoordinate, 1.0, 1.0));
#else
v_textureCoordinate = a_inputTextureCoordinate;
v_frameCoordinate = a_frameTextureCoordinate;
#endif
}