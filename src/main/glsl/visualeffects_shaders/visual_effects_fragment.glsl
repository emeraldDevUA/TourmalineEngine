#version 460 core

#include <algorithms/gradient.glsl>

in vec3 fragPosition;  // Position of the fragment in world space
in vec3 viewPosition;  // Camera position in world space
in vec2 texCoords;

layout (location = 0) out vec4 fragColorMain;
layout (location = 1) out vec4 fragColor;    // Output color

layout (binding = 4) uniform sampler2D albedo_map;

uniform vec3 rocketPos;
uniform int effectType;

uniform float time;

void processJetEffect();

void processExplosion();



float noise(vec3 p) {
    return fract(sin(dot(p, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
}

void main() {

    switch(effectType){
        case 1:
            processJetEffect();
        break;

        case 2:
            processExplosion();
        break;

        default:
            fragColor = texture(albedo_map, texCoords);
        break;
    }

}

void processExplosion(){
    vec4 color1 = vec4(1.0, 1.0, 0.0, 1.0); // Bright yellow
    vec4 color2 = vec4(1.0, 0.5, 0.0, 1.0); // Orange
    vec4 color3 = vec4(1.0, 0.0, 0.0, 1.0); // Red
    vec4 color4 = vec4(0.2, 0.0, 0.0, 1.0); // Dark red/Almost black

    vec3 diff = fragPosition - rocketPos;
    float t = abs(12.8 - abs(length(diff)));

    fragColorMain = linearGradient4(color1, color2, color3, color4, t);
    //    fragColor = vec4(1);
}


void processJetEffect(){

    vec2 vST = vec2(fragPosition.xz);
    float uFrequency = 30.0 + 10.0 * sin(time);
    float uDensity = 0.5 + 0.3 * cos(time);
    vec2 stf = vST * uFrequency;



    // Calculate distance from the rocket
    vec3 diff = fragPosition - rocketPos;
    float distance = length(diff);


    // Initial and transition jet stream colors
    vec3 jetColorStart = normalize(vec3(239, 167, 153)); // Bright reddish-orange
    vec3 jetColorMid = normalize(vec3(207, 140, 17));    // Orangish-brown
    vec3 jetColorEnd = normalize(vec3(50, 50, 50));      // Dark gray for burnt effect

    // Define jet stream shape (cone in 3D space)
    float jetRadius = 0.2 + 0.5 * distance; // Cone widens as it moves away

    // Create a smooth gradient by blending colors
    vec3 jetColor;
    if (distance < 1.2) {
        jetColor = mix(jetColorStart, jetColorMid, distance / 1.0); // From start to mid
    } else {
        jetColor = mix(jetColorMid, jetColorEnd, (distance - 1.0) / 1.3); // From mid to end
    }

    // Check if the fragment is within the jet stream
    if (distance < 2.3) {
        // Add turbulence with increasing intensity at greater distances
        float turbulence = noise(fragPosition * 10.0 + time) * (0.3 + 0.2 * distance);

        // Simulate damage by reducing brightness and adding flicker
        float brightness = (1.0 - (distance / 5.0)) * (0.8 + 0.2 * noise(vec3(time * 0.5, distance, 0.0)));

        // Combine color and effects
        vec3 color = jetColor * (brightness + turbulence);

        // Apply a fading alpha based on distance for smoother blending
        float alpha = clamp(1.0 - (distance / 5), 0.0, 1.0);

        // Set the fragment color
        fragColor = vec4(color, alpha);
    } else {
        // Outside the jet stream, discard the fragment
        discard;
    }

}