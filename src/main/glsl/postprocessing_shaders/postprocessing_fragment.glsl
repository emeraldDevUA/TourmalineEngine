#version 460 core

#include <algorithms/blur.glsl>
#include <algorithms/fxaa.glsl>
#include <algorithms/color_spaces.glsl>
#include <algorithms/gradient.glsl>

in vec2 uv_frag;

layout (binding = 0) uniform sampler2D color;
layout (binding = 1) uniform sampler2D bloom;


layout (location = 0) out highp  vec4 fragment;




void main()
{

    vec2 uViewportSize = vec2(1920, 1080);

    float gamma = 1.4;
    float exposure = 1.0;

    vec4 bloomColor = gaussian_blur5(bloom, uv_frag);

    uViewportSize = vec2(1, 1);
    vec4 fxaa_color = applyFXAA(color, uv_frag, uViewportSize);

    //fxaa_color.rgb += bloomColor.rgb;
    if(rgbToGrayScale(bloomColor.rgb) >= 1){
        fxaa_color.rgb = mix(fxaa_color.rgb, bloomColor.rgb, 0.6);
    }else{
        fxaa_color.rgb += bloomColor.rgb;

    }


    fragment = vec4(pow(fxaa_color.rgb * exposure, vec3(1.0 / gamma)), bloomColor.a);
    //fragment = bloomColor;

}

