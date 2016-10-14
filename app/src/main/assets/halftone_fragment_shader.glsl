#extension GL_OES_EGL_image_external : require
	  precision mediump float;
	  #ifdef INPUT_VF
	  uniform samplerExternalOES s_viewfindertexture;
	  #else
	  uniform sampler2D s_viewfindertexture;
	  #endif
	  varying vec2 v_textureCoordinate;
	  uniform float u_dots;
	  void main() {


	   float dotsize = 1.0 / u_dots ;
	   float half_step = dotsize / 2.0;

	   vec2 center = v_textureCoordinate - vec2(mod(v_textureCoordinate.x, dotsize),mod(v_textureCoordinate.y, dotsize)) + half_step;
	   vec4 inputrgb = texture2D( s_viewfindertexture, center );
	   float size = length(inputrgb);

	   if (distance(v_textureCoordinate,center) <= dotsize*size/4.0) {
	    gl_FragColor = inputrgb;
	   } else {
	    gl_FragColor = vec4(0.0,0.0,0.0,0.0);
	   }
	  }