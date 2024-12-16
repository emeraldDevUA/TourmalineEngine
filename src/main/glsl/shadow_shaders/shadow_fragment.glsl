#version 460

//layout (location = 0) out vec4 shadowColor;


void main() {

//    shadowColor = vec4(gl_FragCoord.z);
        gl_FragDepth = gl_FragCoord.z;
    //
}