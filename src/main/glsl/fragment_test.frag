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

uniform vec2 uViewportSize;
uniform float far;
uniform float near;

#include <algorithms/fxaa.glsl>

void main() {
    vec2 uv = gl_FragCoord.xy / uViewportSize;

    float eps = 10e-5;
    vec4 fetchedPos = texture(positionTexture, uv);

    float linearDepth = (2.0 * near * far) / (far + near - (2.0 * fetchedPos.r - 1.0) * (far - near));
    //    camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 2000,0.1f);
    //    float far = 2000; // move to uniforms later
    //    float near = 0.1f;


    if (gl_FragCoord.z < linearDepth - eps) {
        if(abs(gl_FragCoord.z - (linearDepth - eps))< 1 - eps){discard;}
    }

    fragment = texture(albedo_map, uv) + vec4(0, 0, 0, 0.2);
}