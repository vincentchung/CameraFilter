#extension GL_OES_EGL_image_external : require
	      precision mediump float;
		  #ifdef INPUT_VF
	  uniform samplerExternalOES s_viewfindertexture;
	  #else
	  uniform sampler2D s_viewfindertexture;
	  #endif

	      varying vec2 v_textureCoordinate;

		  void main () {
		    const vec4 kRGBToYPrime = vec4 (0.299, 0.587, 0.114, 0.0);
		    const vec4 kRGBToI = vec4 (0.596, -0.275, -0.321, 0.0);
		    const vec4 kRGBToQ = vec4 (0.212, -0.523, 0.311, 0.0);

		    const vec4 kYIQToR = vec4 (1.0, 0.956, 0.621, 0.0);
		    const vec4 kYIQToG = vec4 (1.0, -0.272, -0.647, 0.0);
		    const vec4 kYIQToB = vec4 (1.0, -1.107, 1.704, 0.0);

	        float hueAdjust = 3.1;

		    vec4 color = texture2D(s_viewfindertexture, v_textureCoordinate);
		    float YPrime = dot (color, kRGBToYPrime);
		    float I = dot (color, kRGBToI);
		    float Q = dot (color, kRGBToQ);
		    float hue = atan (Q, I);
		    float chroma = sqrt (I * I + Q * Q);

		    hue += hueAdjust;
		    Q = chroma * sin (hue);
		    I = chroma * cos (hue);
		    vec4 yIQ = vec4 (YPrime, I, Q, 0.0);
		    color.r = dot (yIQ, kYIQToR);
		    color.g = dot (yIQ, kYIQToG);
		    color.b = dot (yIQ, kYIQToB);

		    gl_FragColor = color;
		    }