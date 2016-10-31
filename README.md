CameraFilter
===================================
Adding OpenGL filter effect on camera preview and captured image.
You can add any new filter effect with GLSL script file very easy.
The app is on [google play][2]. And it's free to donwload.

Introduction
The Renderer class is main function to handle rendering to preview and image buffer.
The Filter class is for one filter effect with glsl file.

Create one filter effect extend from Filter class. Very easy to add ew one.
FilterList is managering all filter.

The Camera class is reference from [Camera2Basic Sample][1]
For handling camera setting and preview/capturing image size.

I had provides more working node in my [wordpress blog][3].

[1]: https://github.com/googlesamples/android-Camera2Basic
[2]: https://play.google.com/store/apps/details?id=com.yamate.camera
[3]: https://vincentcwblog.wordpress.com/category/tech/


Pre-requisites
--------------

- Android SDK 24
- Android Build Tools v24.0.1
- Android Support Repository

Screenshots
-------------

<img src="https://vincentcwblog.files.wordpress.com/2016/10/ycamera_1477803842814.jpg?w=2400&h=&crop=1" height="400" alt="Screenshot"/>
<img src="https://vincentcwblog.files.wordpress.com/2016/10/ycamera_1477803839169.jpg?w=2400&h=&crop=1" height="400" alt="Screenshot"/>
<img src="https://vincentcwblog.files.wordpress.com/2016/10/ycamera_1477803835597.jpg?w=2400&h=&crop=1" height="400" alt="Screenshot"/>
<img src="https://vincentcwblog.files.wordpress.com/2016/10/ycamera_1477803831567.jpg?w=2400&h=&crop=1" height="400" alt="Screenshot"/>
