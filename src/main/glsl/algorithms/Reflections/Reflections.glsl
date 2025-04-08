// Ray marching parameters
const float rayStep = 0.25;       // Smaller steps for more accuracy
const float minRayStep = 0.05;    // Prevents skipping small details
const float maxSteps = 32.0;      // Higher for better reflections
const float searchDist = 20.0;    // Extend to allow further reflections
const float searchDistInv = 1.0 / searchDist;

// Binary search refinement
const int numBinarySearchSteps = 6;  // Smoother edges

// Depth difference threshold
const float maxDDepth = 0.2;         // Avoids artifacts from depth discontinuities
const float maxDDepthInv = 1.0 / maxDDepth;


const float reflectionSpecularFalloffExponent = 3.0;

float ssr_lod = 2;

vec3 hashVector(vec3 vector, vec3 scale, float K){

    vector = fract(vector*scale);
    vector += dot(vector, vector.yxz + K);

    return fract((vector.xxy + vector.yxx)*vector.zyx);
}

vec3 SamplePositionTexture(sampler2D positionTexture, vec2 uv)
{
    // Jitter offsets for multisampling
    const vec2 offsets[4] = vec2[](
    vec2(-0.5, -0.5), vec2(0.5, -0.5),
    vec2(-0.5,  0.5), vec2(0.5,  0.5)
    );

    float depth = 0.0;

    for (int i = 0; i < 4; i++)
    {
        depth += texture2D(positionTexture, uv + offsets[i] * 0.001).z;
    }

    return vec3(uv, depth / 4.0); // Average the samples
}

vec3 BinarySearch(sampler2D positionTexture, vec3 dir, inout vec3 hitCoord, out float dDepth)
{
    float depth;

    for (int i = 0; i < numBinarySearchSteps; i++)
    {
        vec4 projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoord.xy /= projectedCoord.w;
        projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

        if (projectedCoord.x < 0.0 || projectedCoord.x > 1.0 ||
        projectedCoord.y < 0.0 || projectedCoord.y > 1.0)
        break; // Stop if out of bounds

        vec4 temp = texture2D(positionTexture, projectedCoord.xy);
        float linearDepth = temp.z;  // Assuming view-space depth is stored

        dDepth = hitCoord.z - linearDepth;

        if (dDepth > 0.0)
        hitCoord -= dir; // Move back if overshot

        dir *= 0.5;
        hitCoord += dir; // Fine-tune step
    }

    vec4 projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
    projectedCoord.xy /= projectedCoord.w;
    projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

    return vec3(projectedCoord.xy, depth);
}

vec4 rayMarch(sampler2D positionTexture, vec3 dir, inout vec3 hitCoord, out float dDepth)
{
    dir *= max(rayStep * (hitCoord.z * 0.1), minRayStep);

    float depth;
    for (int i = 0; i < maxSteps; i++)
    {
        hitCoord += dir;

        vec4 projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoord.xy /= projectedCoord.w;
        projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

        if (projectedCoord.x < 0.0 || projectedCoord.x > 1.0 ||
        projectedCoord.y < 0.0 || projectedCoord.y > 1.0)
        return vec4(0.0);

        vec4 temp = texture2D(positionTexture, projectedCoord.xy);
        float linearDepth = temp.z;  // Assuming view-space depth

        dDepth = hitCoord.z - linearDepth;

        if (dDepth < 0.0)
        return vec4(BinarySearch(positionTexture, dir, hitCoord, dDepth), 1.0);
    }

    return vec4(0.0);
}
