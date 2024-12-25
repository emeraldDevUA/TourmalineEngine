#version 460 core


layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

uniform mat4 view_matrix;
uniform mat4 projection_matrix;
uniform mat4 model_matrix;

uniform mat4 shadow_view_matrix;
uniform mat4 shadow_projection_matrix;


out VS_OUT
{
    vec3 position;
    vec3 normal;
    vec3 bitangent;
    vec2 uv;
    vec3 camera_position;
    vec3 shadow_position;
} vs_out;


void main()
{
    vec3 bitangent = vec3(0,0,0);
    // restored as a perpendicular vector.
    bitangent.r =  normal.r;
    bitangent.g = -normal.r;
    bitangent.b =  normal.g;

    vec4 model_position =  (model_matrix * vec4(position, 1.0));

    vs_out.position  = model_position.xyz;
    vs_out.normal    = (model_matrix * vec4(normal, 0.0)).xyz;
    vs_out.bitangent = (model_matrix * vec4(bitangent, 0.0)).xyz;
    vs_out.uv = uv;

    mat4 camera_direction = inverse(view_matrix);

    vs_out.camera_position = vec3(camera_direction[3][0], camera_direction[3][1], camera_direction[3][2]);

    vec4 sp = shadow_projection_matrix * shadow_view_matrix * model_position;

    vs_out.shadow_position = sp.xyz/sp.w;

    gl_Position = projection_matrix * view_matrix * model_position;
}