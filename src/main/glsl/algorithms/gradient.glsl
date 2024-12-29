
vec4 linearGradient(vec4 color1, vec4 color2, float t){

    return mix(color1, color2, t);
}


vec4 radialGradient(vec2 uv, vec2 center, float radius, vec4 innerColor, vec4 outerColor) {
    // Compute the distance from the center
    float dist = length(uv - center);

    // Normalize the distance to [0, 1] using the radius
    float t = clamp(dist / radius, 0.0, 1.0);

    // Interpolate between innerColor and outerColor
    return mix(innerColor, outerColor, t);
}


vec4 conicGradient(vec2 uv, vec2 center, vec4 color1, vec4 color2, vec4 color3) {
    // Compute the vector from the center to the UV
    vec2 delta = uv - center;

    // Calculate the angle in radians and normalize to [0, 1]
    float angle = atan(delta.y, delta.x); // [-π, π]
    float normalizedAngle = (angle + 3.14159265359) / (2.0 * 3.14159265359); // [0, 1]

    // Interpolate between the colors based on the angle
    if (normalizedAngle < 0.33) {
        return mix(color1, color2, normalizedAngle / 0.33);
    } else if (normalizedAngle < 0.66) {
        return mix(color2, color3, (normalizedAngle - 0.33) / 0.33);
    } else {
        return mix(color3, color1, (normalizedAngle - 0.66) / 0.34);
    }
}