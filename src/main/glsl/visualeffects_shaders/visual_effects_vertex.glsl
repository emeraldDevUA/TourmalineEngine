#version 460 core

#include<algorithms/Noise/PerlinNoise3d.glsl>

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 uvs;

uniform mat4 view_matrix;
uniform mat4 projection_matrix;
uniform mat4 model_matrix;

uniform vec3 camera_position;

out vec3 fragPosition;
out vec3 viewPosition;


out vec2 texCoords;

uniform vec3 rocketPos;
uniform float time;        // Time variable for dynamics
uniform int effectType;
uniform int phase;  // 0 = explosion, 1 = pulse


float f(float d){

    return 3/d;
}

void processJetEffect();
void processExplosion();

void main() {
    mat4 camera_direction = inverse(view_matrix);
    viewPosition = camera_position;


    switch(effectType){
            case 1:
                processJetEffect();
            break;

            case 2:
                processExplosion();
            break;

            default:
                     texCoords = uvs;
                     vec4 worldPosition = model_matrix * vec4(position, 1) ;
                     fragPosition = worldPosition.xyz;

                    gl_Position  = projection_matrix * view_matrix * worldPosition;

            break;
    }
    // Calculate world position and fragment position

}

void processExplosion() {
    // Transform center to object space (inverse of model matrix)
    vec3 center = vec3(0.0);
    vec3 worldCenter = vec3(model_matrix * vec4(center, 1.0));

    // Calculate direction and distance in WORLD SPACE
    vec3 worldPos = vec3(model_matrix * vec4(position, 1.0));
    vec3 worldDirection = normalize(worldPos - worldCenter);
    float worldDistance = length(worldPos - worldCenter);

    mat3 normal_matrix = transpose(inverse(mat3(model_matrix)));
    vec3 worldNormal = normalize(normal_matrix * normal);

    // Noise calculation (still in object space for texture consistency)
    float noiseScale = 3.0;
    float noiseSpeed = 5.0;
    float noise = cnoise(position * noiseScale + vec3(time * noiseSpeed));

    // Apply scaling factor from model matrix
    float avgScale = length(vec3(model_matrix[0].x, model_matrix[1].y, model_matrix[2].z));

    float explosionStrength = 1.5 * avgScale; // Scale explosion strength
    float shrinkDuration = 2.0;

    vec3 outward = worldDirection * (worldDistance + noise) * explosionStrength;
    vec3 surfaceWarp = worldNormal * noise * avgScale;

    // Phase calculations
    vec3 newPos;
    if (phase == 0) {
        float t = smoothstep(0.0, 1.0, time);
        newPos = worldPos + (outward + surfaceWarp) * t;
    } else {
        float normalizedShrinkTime = time / shrinkDuration;
        float t = 1.0 - smoothstep(0.0, 1.0, normalizedShrinkTime);
        t = pow(t, 0.5);
        newPos = worldPos + (outward + surfaceWarp) * t;
    }

    // Transform back to object space for final rendering
    newPos = vec3(inverse(model_matrix) * vec4(newPos, 1.0));

    fragPosition = vec3(model_matrix * vec4(newPos, 1.0));
    gl_Position = projection_matrix * view_matrix * vec4(fragPosition, 1.0);
}

void processJetEffect(){

    vec4 worldPosition = model_matrix * vec4(position, 1);
    fragPosition = worldPosition.xyz;

    // Calculate camera position in world space

    // Calculate distance from the rocket
    vec3 diff = fragPosition - rocketPos;
    float distance = length(diff);

    // Adjust scaling based on distance
    float baseScale = 1.0;
    float dynamicScale = 2; // Cone-like scaling
    float taperingFactor = 1.0 / (1.0 + 0.5 * distance); // Smooth tapering near the end
    float turbulenceFactor = 0.2 * sin(distance * 10.0 + time); // Simulate turbulence
    if(distance > 2.1){
        dynamicScale= 1/distance;
    }
    // Apply scaling with tapering and turbulence
    mat4 scaleMatrix = mat4(1.0);
    scaleMatrix[0][0] = baseScale + turbulenceFactor * 0.5;  // Slight lateral distortion
    scaleMatrix[1][1] = dynamicScale * taperingFactor;       // Adjust Y-axis scaling
    scaleMatrix[2][2] = dynamicScale * taperingFactor;       // Adjust Z-axis scaling

    // Combine the scale matrix with the model matrix
    mat4 scaled_model_matrix = model_matrix * scaleMatrix;

    // Calculate final vertex position
    gl_Position = projection_matrix * view_matrix * scaled_model_matrix * vec4(position, 1);

}