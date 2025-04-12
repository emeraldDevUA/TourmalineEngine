#version 460 core

uniform vec2 uViewportSize;
uniform bool enableFXAA;
uniform float gamma;

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

    float exposure = 1.0;
    vec4 bloomColor = fast_blur(bloom, uv_frag);
    vec4 fxaa_color;


    if(enableFXAA){
        fxaa_color = applyFXAA(color, gl_FragCoord.xy, uViewportSize);

    }else{
        fxaa_color= texture(color, uv_frag);

    }

    if(rgbToGrayScale(bloomColor.rgb) >= 1){
        fxaa_color.rgb = mix(fxaa_color.rgb, bloomColor.rgb, 0.6);

    }else{
        fxaa_color.rgb += bloomColor.rgb;

    }



    fragment = vec4(pow(fxaa_color.rgb * exposure, vec3(1.0 / gamma)),1);

}