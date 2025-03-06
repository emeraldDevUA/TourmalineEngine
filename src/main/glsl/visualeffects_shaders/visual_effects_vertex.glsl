#version 460 core

#include<algorithms/Noise/PerlinNoise3d.glsl>

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 uvs;

uniform mat4 view_matrix;
uniform mat4 projection_matrix;
uniform mat4 model_matrix;


out vec3 fragPosition;
out vec3 viewPosition;


out vec2 texCoords;

uniform vec3 rocketPos;
uniform float time;        // Time variable for dynamics
uniform int effectType;


float f(float d){

    return 3/d;
}

void processJetEffect();
void processExplosion();

void main() {
    mat4 camera_direction = inverse(view_matrix);
    viewPosition = vec3(camera_direction[3][0], camera_direction[3][1], camera_direction[3][2]);


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

void processExplosion(){
    float noiseValue = cnoise(position);
//    vec3 normal = ( model_matrix* vec4(normal, 1) ).xyz;

    mat3 normal_matrix = transpose(inverse(mat3(model_matrix)));
    vec3 normal = normalize(normal_matrix * normal);

    vec3 newPos = position + normal * noiseValue;

//    mat4 scaleMatrix = mat4(1.0);
//    scaleMatrix[0][0] = newPos.x/position.x;
//    scaleMatrix[1][1] = newPos.y/position.y;
//    scaleMatrix[2][2] = newPos.z/position.z;
//
//    mat4 scaled_model_matrix = model_matrix * scaleMatrix;
        fragPosition = (model_matrix * vec4(newPos, 1)).xyz;


    gl_Position = projection_matrix * view_matrix * model_matrix* vec4(newPos, 1);
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