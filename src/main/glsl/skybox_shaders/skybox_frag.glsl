#version 460 core

in vec3 uv_frag;

layout (binding = 10) uniform samplerCube skybox;

out vec4 fragment;

void main()
{
    fragment = texture(skybox, uv_frag);
}