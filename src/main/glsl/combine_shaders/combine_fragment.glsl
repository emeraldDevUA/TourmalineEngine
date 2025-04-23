#version 460 core
#include <algorithms/blur.glsl>

#define PI 3.1415926

in vec2 uv_frag;


layout (binding = 0) uniform sampler2D position;
layout (binding = 1) uniform sampler2D albedo_metalness;
layout (binding = 2) uniform sampler2D normal_roughness;
layout (binding = 3) uniform sampler2D environment_emission;

layout (binding = 4) uniform sampler2D shadow_position;
layout (binding = 5) uniform sampler2D shadow_map;
layout (binding = 6) uniform sampler2D previous_position;
layout (binding = 7) uniform sampler2D reflection_value;

layout (binding = 9) uniform sampler2D BRDFlookUp;


#include <algorithms/LightStructs.glsl>

uniform mat4 view_matrix;
uniform mat4 previous_view_matrix;
uniform mat4 projection_matrix;
uniform vec3 camera_position;

#include <algorithms/Reflections/Reflections.glsl>

layout (location = 0) out highp vec4 fragment;
layout (location = 1) out vec4 bloom;


vec3 fresnelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

float DistributionGGX(vec3 N, vec3 H, float rough)
 {
     float a      = rough*rough;
     float a2     = a*a;
     float NdotH  = max(dot(N, H), 0.0);
     float NdotH2 = NdotH * NdotH;

     float num   = a2;
     float denom = (NdotH2 * (a2 - 1.0) + 1.0);
     denom = PI * denom * denom;

     return num / denom;
 }

 float GeometrySchlickGGX(float NdotV, float rough)
 {
     float r = (rough + 1.0);
     float k = (r*r) / 8.0;

     float num   = NdotV;
     float denom = NdotV * (1.0 - k) + k;

     return num / denom;
 }

float DistributionTR(vec3 N, vec3 H, float rough)
{
    float a = rough * rough;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;

    float num = a * a;
    float denom = (NdotH2 * (a * a - 1.0) + 1.0);
    denom = PI * denom * denom;

    return num / denom;
}

 vec3 FresnelSchlickRoughness(float cosTheta, vec3 F0, float rough)
 {
     return F0 + (max(vec3(1.0 - rough), F0) - F0) * pow(1.0 - cosTheta, 5.0);
 }
 
 float GeometrySmith(vec3 N, vec3 V, vec3 L, float rough)
 {
     float NdotV = max(dot(N, V), 0.0);
     float NdotL = max(dot(N, L), 0.0);
     float ggx2  = GeometrySchlickGGX(NdotV, rough);
     float ggx1  = GeometrySchlickGGX(NdotL, rough);

     return ggx1 * ggx2;
 }



//https://developer.nvidia.com/gpugems/gpugems2/part-ii-shading-lighting-and-shadows/chapter-17-efficient-soft-edged-shadows-using
float ShadowCalculation(vec3 fragPosLightSpace, vec3 normal, vec3 lightDir)
{
    // Transform to [0,1] range
    vec3 projCoords = fragPosLightSpace * 0.5 + 0.5;

    // Check bounds to ensure valid texture sampling
    if (projCoords.x < 0.0 || projCoords.x > 1.0 || projCoords.y < 0.0 || projCoords.y > 1.0)
        return 0.0;

    // Get the depth of the current fragment
    float currentDepth = projCoords.z;

    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);

    // Perform Percentage-Closer Filtering (PCF)
    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadow_map, 0); // Size of a single texel

    int n = 1; // Kernel radius
    int samples = 0; // Counter for total samples

    for (float x = -n; x <= n; x += 0.5) {
        for (float y = -n; y <= n; y += 0.5) {
            vec2 offset = vec2(x, y) * texelSize;
            float closestDepth = texture(shadow_map, projCoords.xy + offset).r;
            shadow += (currentDepth - bias >= closestDepth) ? 0.8 : 0.0;
            samples++; // Increment sample counter
        }
    }

    // Average the shadow based on the actual number of samples
    shadow /= samples;

    return shadow;
}

vec4 getMotionBlur(int size, float separation, vec3 position_value, vec3 prev_position_value){

    vec2 texSize = textureSize(position, 0).xy;

    vec4 view_position = projection_matrix * view_matrix * vec4(position_value*2, 1.0);
    vec4 view_position_prev = projection_matrix * previous_view_matrix * vec4(prev_position_value*2, 1.0);

    view_position.xyz /= view_position.w;
    view_position.xy = view_position.xy * 0.5 + 0.5;

    view_position_prev.xyz /= view_position_prev.w;
    view_position_prev.xy = view_position_prev.xy * 0.5 + 0.5;

    vec2 direction = (view_position_prev.xy - view_position.xy);

    float zDiff = abs(view_position_prev.z - view_position.z);
    // Clamp the direction to ensure it's not too aggressive
    direction = clamp((direction) * separation, -0.01, 0.01);

    vec4 _albedo_value = texture(albedo_metalness, uv_frag); // Initialize
    float count = 1.0;
    float alpha = _albedo_value.a;
    if (length(direction) > 0) {
        vec2 forward = uv_frag;
        vec2 backward = uv_frag;

        for (int i = 1; i < size; ++i) {
            forward += direction;
            backward -= direction;

            forward = clamp(forward, vec2(0.0), vec2(1.0));
            backward = clamp(backward, vec2(0.0), vec2(1.0));


            _albedo_value += fast_blur(albedo_metalness, forward);
            _albedo_value += fast_blur(albedo_metalness, backward);

            count += 2.0;
        }

        _albedo_value /= count;
    }

    return _albedo_value;
}
float sqr(float x)
{
    return x * x;
}

float attenuate_no_cusp(float distance, float radius,
                        float max_intensity, float falloff)
{
    float s = distance / radius;

    if (s >= 1.0)
    return 0.0;

    float s2 = sqr(s);

    return max_intensity * sqr(1 - s2) / (1 + falloff * s2);
}


void main()
{

    vec3 light_color = vec3(10.0, 10.0, 10.0);

    float metalness_value = texture(albedo_metalness, uv_frag).a;
    float roughness_value = texture(normal_roughness, uv_frag).a;

    vec3 position_value =  texture(position, uv_frag).rgb;
    vec3 prev_position_value = texture(previous_position, uv_frag).rgb;

    vec3 albedo_value = texture(albedo_metalness, uv_frag).rgb;

    vec3 environment_emission_value = texture(environment_emission, uv_frag).rgb;

    vec3 N = normalize(texture(normal_roughness, uv_frag).rgb);
    vec3 V = normalize(camera_position - position_value);

    vec3 Lo = vec3(0.0);
    float shadow_value = ShadowCalculation(
        texture(shadow_position, uv_frag).xyz, N, vec3(0,100,0)-position_value
    );

    /**/
    int size = 4; // Increased sample size for smoother blur
    float separation = 0.01; // Reduced separation for softer blur

//   albedo_value = getMotionBlur(size, separation, position_value, prev_position_value).rgb;



    for(int i = 0; i < number_pointLights; i++) {

        vec3 L = normalize( lightBlock.pointLights[i].position - position_value);
        vec3 H = normalize(V + L);

        float distance = length(lightBlock.pointLights[i].position - position_value);
        float attenuation =
        attenuate_no_cusp(distance, 50,
                        lightBlock.pointLights[i].intensity, 20);

        light_color = lightBlock.pointLights[i].color;

        vec3 radiance = light_color * attenuation;

        vec3 F0 = vec3(0.04);
        vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
        float NDF = DistributionGGX(N, H, roughness_value);
        float G = GeometrySmith(N, V, L, roughness_value);

        vec3 nominator = NDF * G * F;
        float denominator = 4 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001;
        vec3 specular = nominator / denominator;

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metalness_value;

        float NdotL = max(dot(N, L), 0.03);
        Lo += (kD * albedo_value / PI + specular) * radiance * NdotL;
    }

    for (int i = 0; i < number_dirLights; i++) {
        // Light direction (normalized)
        vec3 L = -normalize(
            (lightBlock.directionalLights[i].direction));
        vec3 H = normalize(V + L);

        vec3 radiance = lightBlock.directionalLights[i].color *
                        lightBlock.directionalLights[i].intensity;
        // No attenuation for directional lights

        vec3 F0 = vec3(0.04);
        vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
        float NDF = DistributionGGX(N, H, roughness_value);
        float G = GeometrySmith(N, V, L, roughness_value);

        vec3 nominator = NDF * G * F;
        float denominator = 4 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001;
        vec3 specular = nominator / denominator;

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metalness_value;

        float NdotL = max(dot(N, L), 0.0);
        Lo += (kD * albedo_value / PI + specular) * radiance * NdotL;
    }

    vec3 viewNormal = texture2D(normal_roughness, uv_frag).xyz;

    vec4 pos = (view_matrix*vec4(texture2D(position, uv_frag).xyz, 1));
    vec3 viewPos = pos.xyz/pos.w;


    // Reflection vector
    vec3 reflected = normalize(reflect(normalize(-viewPos), normalize(viewNormal)));


    // Ray cast

    float dDepth;
    float selfReflectionBias = 0.2;
    float minDDepth = 3;
    vec3 jitt = mix(vec3(0.0),
                    hashVector(viewPos, vec3(.8, .8, .8), 19.19), metalness_value);
    vec3 hitPos = viewPos + normalize(viewNormal) * selfReflectionBias + jitt;
    ;

    vec4 coords = rayMarch(position, reflected * max(minRayStep, -viewPos.z), hitPos, dDepth);
    if (abs(dDepth) < minDDepth) {
        coords = vec4(0.0, 0.0, 0.0, 0.0);
    }
    vec2 screenSize = vec2(1920, 1018);
    vec2 dCoords = abs(gl_FragCoord.xy / screenSize - coords.xy);


    float screenEdgefactor = clamp(1.0 - (dCoords.x + dCoords.y), 0.0, 1.0);


    vec3 SSR = vec3(0);




//////

    vec2 x = clamp(coords.xy, 0.0, 1.0);

    float reflectionFactor = pow(metalness_value, reflectionSpecularFalloffExponent) *
    screenEdgefactor * clamp(-reflected.z, 0.0, 1.0) *
    clamp((searchDist - length(viewPos - hitPos)) * searchDistInv, 0.0, 1.0) * coords.w;

    vec4 ssr_value = vec4(texture2D(environment_emission, x).rgb, reflectionFactor);
    if (coords.w <= 10e-12) {
        ssr_value.rgb = vec3(0.0);
    }

    vec4 reflection = texture(reflection_value, x);
    vec4 reflected_color = texture(reflection_value, uv_frag);

    if (reflection.r > 0.1) {
        ssr_value = vec4(0.0);
    }

    if(reflected_color.g < 0.9){
        ssr_value = vec4(0.0);

    }

//    if (length(temp2.rgb - camera_position) > 200||length(temp.rgb - camera_position) < 1) {
//        ssr_value = vec4(0.0);
//    }

    fragment = (1 - shadow_value ) * vec4(Lo + environment_emission_value, 1.0) + ssr_value;
    if(dot(fragment.rgb, vec3(0.2126, 0.7152, 0.0722)) > 1) {
        bloom = vec4(fragment.rgb, 1.0);
    } else {
       // bloom = vec4(ssr_value);
    }

    //    fragment = vec4(ssr_value);
}