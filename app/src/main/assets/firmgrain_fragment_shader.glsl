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
  uniform sampler2D s_inputTexture;
#endif

/**
  * Tile mode configuration parameters
  */
#ifdef TILE_MODE
  uniform int u_tilesTotal;    // to how many tiles per axis the input was split
  uniform int u_tilesXcurrent; // current X tile [1..u_tilesTotal}
  uniform int u_tilesYcurrent; // current Y tile [1..u_tilesTotal}
#endif

			    uniform sampler2D s_frametexture1;
			    uniform sampler2D s_noisetexture;
				uniform int u_runningTime;

				void main ()
				{

#ifndef OPENGL_ES
  vec2 v_textureCoordinate = gl_TexCoord[0].st;
#endif

#ifdef TILE_MODE
  vec2 frameCoordinate;
  frameCoordinate.s = v_frameCoordinate.s/float(u_tilesTotal) + (float(u_tilesXcurrent) - 1.0)/float(u_tilesTotal);
  frameCoordinate.t = v_frameCoordinate.t/float(u_tilesTotal) + (float(u_tilesYcurrent) - 1.0)/float(u_tilesTotal);
#else
  vec2 frameCoordinate = v_textureCoordinate;
#endif

				  vec4 finalColor;
				  vec3 bwcolor;

				  vec2 uv = vec2(0.0, 0.0);
				  uv.x = 0.4*sin(float(u_runningTime));
				  uv.y = 0.4*cos(float(u_runningTime));
				  vec3 noise = texture2D(s_noisetexture, (v_textureCoordinate*3.5) + uv).rgb;

				  vec4 frame = texture2D(s_frametexture1, vec2(frameCoordinate.x+float(u_runningTime)/150.0, frameCoordinate.y));

				  vec3 rgb = texture2D(s_viewfindertexture, v_textureCoordinate + (noise.xy*0.005)).rgb;

				  bwcolor.r = dot(rgb, vec3(.3, .59, .11));
				  bwcolor.g = dot(rgb, vec3(.3, .59, .11));
				  bwcolor.b = dot(rgb, vec3(.3, .59, .11));

				  finalColor.rgb = (bwcolor + (noise*0.2));
				  gl_FragColor.rgb = mix(finalColor.rgb, frame.rgb, frame.a);
				  gl_FragColor.a = 1.0;
				}