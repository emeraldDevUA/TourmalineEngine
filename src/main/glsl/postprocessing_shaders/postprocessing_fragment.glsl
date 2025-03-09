#version 460 core


//vec4 gaussian_blur(vec2 uvs);


in vec2 uv_frag;

layout (binding = 0) uniform sampler2D color;
layout (binding = 1) uniform sampler2D bloom;

layout (location = 0) out vec4 fragment;

#include <algorithms/blur.glsl>
#include <algorithms/fxaa.glsl>
#include <algorithms/color_spaces.glsl>
#include <algorithms/gradient.glsl>

void main()
{

    vec2 uViewportSize = vec2(1920, 1018);

    float gamma = 1.4;
    float exposure = 1.0;

    vec4 bloomColor = gaussian_blur5(bloom, uv_frag);

    vec4 fxaa_color = applyFXAA(color, uv_frag, uViewportSize);
    uViewportSize = vec2(1, 1);
    fxaa_color += bloomColor;

    fragment = vec4(pow(fxaa_color.rgb * exposure, vec3(1.0 / gamma)), 1.0);
    //fragment = bloomColor;

}



//vec4 gaussian_blur(vec2 uvs){
//    // Kernel size and range
//    int k = 1;
//    vec3 blurredVertex = vec3(0, 0, 0);
//
//    // 3x3 Gaussian kernel normalized
//    mat3 gaussKernel =
//    mat3(1, 2, 1, 2, 4, 2, 1, 2, 1);
//
//    gaussKernel /= 16.0;
//
//    vec2 texOffset = vec2(1.0 / textureSize(color, 0));  // Assuming 'color' is the sampler2D
//
//    // Iterate over the kernel
//    for(int i = -k; i <= k; i++){
//        for(int j = -k; j <= k; j++){
//
//            int kernelX = i + k;
//            int kernelY = j + k;
//
//            blurredVertex += gaussKernel[kernelX][kernelY] *
//
//            texture(color, uvs + vec2(i, j) * texOffset).xyz;
//        }
//    }
//
//    return vec4(blurredVertex, 1.0);
//}