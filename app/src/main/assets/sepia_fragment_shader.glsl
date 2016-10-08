#extension GL_OES_EGL_image_external : require
				precision mediump float;
#ifdef INPUT_VF
uniform samplerExternalOES s_viewfindertexture;
#else
uniform sampler2D s_viewfindertexture;
#endif
				uniform int u_runningTime;
				uniform float u_capturing;
				varying vec2 v_textureCoordinate;
				void main ()
				{
				  vec3 rgb;
	              rgb = texture2D(s_viewfindertexture, v_textureCoordinate).rgb;

				  gl_FragColor.r = dot(rgb, vec3(.393, .769, .189));
				  gl_FragColor.g = dot(rgb, vec3(.349, .686, .168));
				  gl_FragColor.b = dot(rgb, vec3(.272, .534, .131));
				  gl_FragColor.a = 1.0;
				}