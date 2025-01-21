struct Wave {
    vec3 position;
    vec2 direction;

    float wavelength;
    float amplitude;
    float steepness;
    float speed;
    float phase;
};

vec3 generateWave(Wave wave, float time) {
    // Calculate constants for the Gerstner wave
    float k = 2.0 * 3.14159 / wave.wavelength;  // Wave number
    float omega = sqrt(9.81 * k);              // Angular frequency (gravity waves)
    float theta = dot(wave.direction, wave.position.xz); // Wave phase
    float phi = omega * time + wave.phase;     // Time-dependent phase

    // Wave steepness factor
    float q = wave.steepness;

    // Calculate vertical displacement (wave height)
    float height = wave.amplitude * sin(k * theta - phi);

    // Calculate horizontal displacement
    vec3 displacement = vec3(
    q * wave.amplitude * wave.direction.x * cos(k * theta - phi), // Displacement along x
    height,                                                      // Displacement along y
    q * wave.amplitude * wave.direction.y * cos(k * theta - phi) // Displacement along z
    );

    return displacement;
}

vec3 generateWaveNormal(Wave wave, float time) {
    float k = 2.0 * 3.14159 / wave.wavelength;  // Wave number
    float omega = sqrt(9.81 * k);               // Angular frequency
    float phi = omega * time + wave.phase;      // Time-dependent phase

    // Compute partial derivatives (tangents)
    float theta = k * dot(wave.direction, wave.position.xz);
    float cosThetaPhi = cos(theta + phi);

    vec3 tangentX = vec3(
    1.0,
    -wave.amplitude * k * wave.direction.x * cosThetaPhi,
    0.0
    );

    vec3 tangentZ = vec3(
    0.0,
    -wave.amplitude * k * wave.direction.y * cosThetaPhi,
    1.0
    );

    // Compute the normal using cross product of tangents
    vec3 normal = normalize(cross(tangentZ, tangentX));
    return normal;
}

vec3 gerstnerPositions(sampler2D coefficientSampler, Wave inWave, float time, int NB_WAVES) {
    vec3 finalVector = inWave.position;

    // Add the initial wave
    finalVector += generateWave(inWave, time);

    float tx = 0.0;
    float ty = 0.0;

    for (int i = 0; i < NB_WAVES; i++) {
        tx++;
        if (tx > 7.0) {
            tx = 0.0;
            ty++;
        }

        vec4 rgb1 = texture(coefficientSampler, vec2(tx + 0.01, ty + 0.01) / 8.0);
        tx++;
        if (tx > 7.0) {
            tx = 0.0;
            ty++;
        }

        vec4 rgb2 = texture(coefficientSampler, vec2(tx + 0.01, ty + 0.01) / 8.0);

        // Parse coefficients from texture
        float phi = rgb1.r * 2 * 3.14159;
        inWave.direction = vec2(cos(phi), sin(phi));

        float frequencyI = rgb1.g * 0.4;
        inWave.wavelength = inWave.speed / frequencyI;

        inWave.amplitude = rgb1.b * 40.0;
        inWave.steepness = rgb2.r * 1.0;
        inWave.phase = rgb2.g * 5.0;

        // Add wave displacement
        finalVector += generateWave(inWave, time);
    }

    return finalVector;
}

// not ready.
vec3 gerstnerNormals(sampler2D coefficientSampler, Wave inWave, float time, int NB_WAVES) {
    vec3 finalVector = inWave.position;

    // Add the initial wave
    finalVector += generateWaveNormal(inWave, time);

    float tx = 0.0;
    float ty = 0.0;

    for (int i = 0; i < NB_WAVES; i++) {
        tx++;
        if (tx > 7.0) {
            tx = 0.0;
            ty++;
        }

        vec4 rgb1 = texture(coefficientSampler, vec2(tx + 0.01, ty + 0.01) / 8.0);
        tx++;
        if (tx > 7.0) {
            tx = 0.0;
            ty++;
        }

        vec4 rgb2 = texture(coefficientSampler, vec2(tx + 0.01, ty + 0.01) / 8.0);

        // Parse coefficients from texture
        float phi = rgb1.r * 2.0 * 3.14159;
        inWave.direction = vec2(cos(phi), sin(phi));

        float frequencyI = rgb1.g * 0.4;
        inWave.wavelength = inWave.speed / frequencyI;

        inWave.amplitude = rgb1.b * 40.0;
        inWave.steepness = rgb2.r * 1.0;
        inWave.phase = rgb2.g * 5.0;

        // Add wave displacement
        finalVector += generateWaveNormal(inWave, time);
    }

    return normalize(finalVector);
}