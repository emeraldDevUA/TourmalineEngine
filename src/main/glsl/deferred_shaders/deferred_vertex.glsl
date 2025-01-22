#version 460 core

#include <algorithms/water/WaterGeneration.glsl>

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

uniform mat4 inverted_view_matrix;
uniform mat4 view_matrix;
uniform mat4 projection_matrix;
uniform mat4 model_matrix;

uniform mat4 shadow_view_matrix;
uniform mat4 shadow_projection_matrix;

uniform bool isWater;
uniform int waveNumber;
uniform float time;

layout (binding = 13) uniform sampler2D coefficients;
layout (binding = 14) uniform sampler2D cullTexture;



layout (std140, binding = 3) uniform waveBlock{
    vec2 direction;
    float wavelength;
    float amplitude;
    float steepness;
    float speed;
    float phase;
};



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
    mat4 camera_direction = inverse(view_matrix);
    vs_out.camera_position = vec3(camera_direction[3][0], camera_direction[3][1], camera_direction[3][2]);
    vs_out.uv = uv;


    vec4 model_position =  (model_matrix * vec4(position, 1.0));

    if(isWater){


        Wave initialWave = Wave(model_position.xyz, direction, wavelength, amplitude, steepness, speed, phase);
        vec3 wave_position = gerstnerPositions(coefficients, initialWave, time, waveNumber);

        wave_position += position;

        vec3 wave_normal = gerstnerNormals(coefficients, initialWave, time, waveNumber);
        vec3 bitangent = vec3(wave_normal.r, -wave_normal.r, wave_normal.g);

        vs_out.position  = (model_matrix * vec4(wave_position, 1.0)).xyz;
        vs_out.normal    = (model_matrix * vec4(wave_normal, 1.0)).xyz;
        vs_out.bitangent = (model_matrix * vec4(bitangent, 0.0)).xyz;

        vec4 sp = shadow_projection_matrix * shadow_view_matrix * model_matrix* vec4(wave_position, 1);
        vs_out.shadow_position = sp.xyz/sp.w;

        gl_Position = projection_matrix * view_matrix * model_matrix*vec4(wave_position, 1);
    }
    else {

        vec3 bitangent = vec3(normal.r,-normal.r,normal.g);

        vs_out.position  = model_position.xyz;
        vs_out.normal    = (model_matrix * vec4(normal, 0.0)).xyz;
        vs_out.bitangent = (model_matrix * vec4(bitangent, 0.0)).xyz;


        vec4 sp = shadow_projection_matrix * shadow_view_matrix * model_position;
        vs_out.shadow_position = sp.xyz/sp.w;


        gl_Position = projection_matrix * view_matrix * model_position;
    }

}