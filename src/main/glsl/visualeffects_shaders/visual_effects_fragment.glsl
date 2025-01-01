#version 460 core


in vec3 fragPosition;  // Position of the fragment in world space
in vec3 viewPosition;  // Camera position in world space

layout (location = 1) out vec4 fragColor;    // Output color

uniform vec3 rocketPos;    // Rocket's position in world space
//uniform vec3 jetColor;     // Base color of the jet stream
uniform float time;        // Time variable for dynamics



// Simple noise function for turbulence
float noise(vec3 p) {
    return fract(sin(dot(p, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
}

void main() {

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

