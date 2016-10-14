/** PiCore OpenGL ES Shader effect: Vignette
  * 
  * Vertex shader: SIMPLE_VERTEX_SHADER
  *   
  * Effect specific parameters:
  *   u_vignetteStart - radius for area where vignette begins
  *   u_vignetteEnd - radius for area where vignette turns completely black
  * 
  */

/**
  * Generic precompiler settings and uniform sampler variables
  */
  #define OPENGL_ES 1
#ifdef OPENGL_ES
  #extension GL_OES_EGL_image_external : require
  precision mediump float;
  
  #ifdef INPUT_VF
    uniform samplerExternalOES s_viewfindertexture;
  #else
    uniform sampler2D s_viewfindertexture;
  #endif
  
  varying vec2 v_textureCoordinate;
  varying vec2 v_frameCoordinate;
#else
  uniform sampler2D s_viewfindertexture;
#endif


/**
  * Tile mode configuration parameters
  */
#ifdef TILE_MODE
  uniform int u_tilesTotal;    // to how many tiles per axis the input was split
  uniform int u_tilesXcurrent; // current X tile [1..u_tilesTotal}
  uniform int u_tilesYcurrent; // current Y tile [1..u_tilesTotal}
#endif

			    uniform sampler2D s_noisetexture;
				uniform int u_runningTime;

				void main() {
#ifndef OPENGL_ES
  vec2 v_textureCoordinate = gl_TexCoord[0].st;
#endif
  
  vec2 effectCenter = vec2(0.5, 0.5);
  
#ifdef TILE_MODE
  vec2 globalCoord = vec2(v_frameCoordinate.s + (float(u_tilesXcurrent)-1.0), v_frameCoordinate.t + (float(u_tilesYcurrent)-1.0));
  vec2 globalEffectCenter = effectCenter*float(u_tilesTotal);
#else
  vec2 globalCoord = v_textureCoordinate;
  vec2 globalEffectCenter = effectCenter;
  int u_tilesTotal = 1;
#endif				
				
				  float luminanceThreshold = 0.2;
				  float colorAmplification = 4.0;
				
				  vec4 finalColor;

				  vec2 uv = vec2(0.0, 0.0);           
				  uv.x = 0.4*sin(float(u_runningTime));                                 
				  uv.y = 0.4*cos(float(u_runningTime));                                 
				
				  vec3 n = texture2D(s_noisetexture, (v_frameCoordinate*3.5) + uv).rgb;
				  vec3 rgb = texture2D(s_viewfindertexture, v_textureCoordinate + (n.xy*0.005)).rgb;
				  
				  float d = distance(globalCoord , globalEffectCenter);
				  
				  float lum = dot(vec3(0.30, 0.59, 0.11), rgb);
				  if (lum < luminanceThreshold) rgb *= colorAmplification;
				  
				  vec3 visionColor = vec3(0.1, 0.95, 0.2);
				
				  finalColor.rgb = (rgb + (n*0.2)) * visionColor * smoothstep(0.5*float(u_tilesTotal), 0.4*float(u_tilesTotal), d);
				  finalColor.a = 1.0;

				  gl_FragColor = finalColor;
							
				}