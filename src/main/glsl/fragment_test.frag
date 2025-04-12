#version 460 core

uniform mat4 projection_matrix;
uniform mat4 view_matrix;
uniform mat4 model_matrix;

layout (binding = 4) uniform sampler2D albedo_map;
layout (binding = 7) uniform sampler2D normal_map;


layout (binding = 16) uniform sampler2D positionTexture;
layout (location = 0 ) out vec4 fragment;

in vec2 textureCoords;
in vec4 currentPosition;
in vec3 camera_position;

uniform vec2 uViewportSize;
uniform float far;
uniform float near;

#include <algorithms/fxaa.glsl>

void main() {
    vec2 uv = gl_FragCoord.xy / uViewportSize;

    float eps = 10e-24;
    vec4 fetchedPos = texture(positionTexture, uv);

    float deferredDepth = (2.0 * near * far) / (far + near - (2.0 * fetchedPos.r - 1.0) * (far - near));
    float z = gl_FragCoord.z;
    float transparentDepth = (2.0 * near * far) / (far + near - z * (far - near));


    if (transparentDepth > deferredDepth + eps) {
        // check the depth difference
        // if it is bigger than the epsilon and in front of the skybox, discard
        float temp = abs(transparentDepth - (deferredDepth));
        if(temp > eps && deferredDepth > 1f-eps){
            if(length(camera_position-currentPosition.xyz) > 12)
            discard;
        }

    }
    //
    fragment = texture(albedo_map, uv) + vec4(0, 0, 0, 0.2);

}