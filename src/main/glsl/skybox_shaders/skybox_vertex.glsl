#version 460 core

layout (location = 0) in vec3 position;

uniform mat4 view_matrix;
uniform mat4 projection_matrix;

out vec3 uv_frag;



void main()
{
    uv_frag = position;

    // Excluding camera translation
    mat4 view_direction = view_matrix;

    view_direction[3][0] = 0.0;
    view_direction[3][1] = 0.0;
    view_direction[3][2] = 0.0;

    gl_Position = (projection_matrix * view_direction * vec4(position, 1.0)).xyww;
    gl_Position.z = gl_Position.w - 0.00001;
    //fix to far plane
}