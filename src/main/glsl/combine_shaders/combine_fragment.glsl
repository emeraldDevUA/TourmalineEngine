#version 460 core

#include <algorithms/blur.glsl>

#define PI 3.1415926

in vec2 uv_frag;
in vec3 camera_position;

in vec3  light_sources[50];
in vec3  light_colors[10];
in float light_intensity[10];

layout (binding = 0) uniform sampler2D position;
layout (binding = 1) uniform sampler2D albedo_metalness;
layout (binding = 2) uniform sampler2D normal_roughness;
layout (binding = 3) uniform sampler2D environment_emission;

layout (binding = 4) uniform sampler2D shadow_position;
layout (binding = 5) uniform sampler2D shadow_map;
layout (binding = 6) uniform sampler2D previous_position;

layout (binding = 9) uniform sampler2D BRDFlookUp;

uniform mat4 view_matrix;
uniform mat4 previous_view_matrix;
uniform mat4 projection_matrix;

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

    // Calculate dynamic bias to prevent shadow acne
    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);

    // Perform Percentage-Closer Filtering (PCF)
    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadow_map, 0); // Size of a single texel

    int n  = 1;
    for (int x = -n; x <= n; ++x) {
        for (int y = -n; y <= n; ++y) {
            vec2 offset = vec2(x, y) * texelSize;
            float closestDepth = texture(shadow_map, projCoords.xy + offset).r;
            shadow += (currentDepth - bias > closestDepth) ? 0.8 : 0.0;
        }
    }

    shadow /= 9.0; // Average the results of the 3x3 PCF kernel

    return shadow;
}

vec4 getMotionBlur(int size, float separation, vec3 position_value, vec3 prev_position_value){

    vec2 texSize = textureSize(position, 0).xy;

    vec4 view_position = projection_matrix * view_matrix * vec4(position_value, 1.0);
    vec4 view_position_prev = projection_matrix * previous_view_matrix * vec4(prev_position_value, 1.0);

    view_position.xyz /= view_position.w;
    view_position.xy = view_position.xy * 0.5 + 0.5;

    view_position_prev.xyz /= view_position_prev.w;
    view_position_prev.xy = view_position_prev.xy * 0.5 + 0.5;

    vec2 direction = (view_position_prev.xy - view_position.xy);

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


            _albedo_value += gaussian_blur(albedo_metalness, forward);
            _albedo_value += gaussian_blur(albedo_metalness, backward);

            count += 2.0;
        }

        _albedo_value /= count;
    }

    return _albedo_value;
}

void main()
{
//    vec3 light_positions[] = { vec3( -5, -5, -5), vec3( 5, -5, -5), vec3( 5, 5, -5), vec3( -5, 5, -5),
//    vec3( -5, -5,  5), vec3( 5, -5,  5), vec3( 5, 5, -5), vec3( -5, 5,  5)}
    // Real scene lights are not implemented yet so I am using these "built-it" for testing
    vec3 light_positions[] = {
        vec3( -5, 500, -5), vec3( 5, 500, -5), vec3( 5, 500, -5), vec3( -5, 500, -5),
        vec3( -5, 500,  5), vec3( 5, 500,  5), vec3( 5, 500, -5), vec3( -5, 500,  5)
    };


    vec3 light_color = vec3(10.0, 10.0, 10.0);

    float metalness_value = texture(albedo_metalness, uv_frag).a;
    float roughness_value = texture(normal_roughness, uv_frag).a;

    vec3 position_value = texture(position, uv_frag).rgb;
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
    int size = 12; // Increased sample size for smoother blur
    float separation = 0.001; // Reduced separation for softer blur

    albedo_value = getMotionBlur(size, separation, position_value, prev_position_value).rgb;



    for(int i = 0; i < 8; i++) {

        vec3 L = normalize(light_positions[i] - position_value);
        vec3 H = normalize(V + L);

        float distance = length(light_positions[i] - position_value);
        float attenuation = 1.0 / ( pow(distance, 1.0/2.0));
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

        float NdotL = max(dot(N, L), 0.0);
        Lo += (kD * albedo_value / PI + specular) * radiance * NdotL;
    }



    vec4 temp = (view_matrix * vec4(position_value, 1));
    vec3 hitPos = temp.xyz;

    temp =  (view_matrix * vec4(N, 1));
    mat3 view_normal_matrix = transpose(inverse(mat3(view_matrix)));
    vec3 view_normal = normalize(view_normal_matrix * N);


    vec3 reflected =
                normalize(reflect(normalize(position_value), normalize(view_normal)));

    float dDepth;
    float spec = metalness_value;

    vec3 wp = vec3(position_value);
    vec3 jitt = mix(vec3(0.0),
                    hashVector(wp, vec3(.8, .8, .8), 19.19), spec);

    float vp_z = hitPos.z;
    reflected *= (1.0 / tan(PI/2 * 0.5));
    vec4 coords = rayMarch(position,
                           (vec3(jitt) + reflected * max(minRayStep, -vp_z)),
                           hitPos, dDepth);

    vec2 dCoords = smoothstep(0.2, 0.6, abs(vec2(0.5, 0.5) - coords.xy));


    float screenEdgefactor = clamp(1.0 - (dCoords.x + dCoords.y), 0.0, 1.0);

    float ReflectionMultiplier = pow(metalness_value, reflectionSpecularFalloffExponent)
        * screenEdgefactor * (-reflected.z);

    // Compute distance scaling factor
    float distanceScale = 1.0 / (distance(hitPos.z, position_value.z) + 1.0);

    // Blend between original and scaled coordinates
    float blendFactor = smoothstep(0.0, 100.0, distance(hitPos.z, position_value.z));
    vec2 blendedCoords = mix(coords.xy, coords.xy * distanceScale, blendFactor);



    vec3 SSR = textureLod(albedo_metalness, blendedCoords, 0).rgb
                * clamp(ReflectionMultiplier, 0.0, 0.9);


    fragment = (1 - shadow_value ) * vec4(Lo + mix(environment_emission_value,
                                                   SSR, metalness_value), 1.0);
//  fragment =  vec4(SSR, 1.0);
    if(dot(fragment.rgb, vec3(0.2126, 0.7152, 0.0722)) > 1) {
        bloom = vec4(fragment.rgb, 1.0);
    } else {
        bloom = vec4(SSR, 1.0);
    }
}