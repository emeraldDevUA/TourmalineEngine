#version 460 core

in vec3 uv_frag;

layout (binding = 10) uniform samplerCube skybox;


//layout (binding = N) uniform samplerCube dayTime;
//layout (binding = M) uniform samplerCube nightTime;

//uniform float time;

layout (location = 0) out highp vec4 fragment;

void main()
{
    fragment = texture(skybox, uv_frag);
}