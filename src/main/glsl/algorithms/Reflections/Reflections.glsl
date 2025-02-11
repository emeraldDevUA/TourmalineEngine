
const float rayStep = 0.25;
const float minRayStep = 0.1;
const float maxSteps = 20;
const float searchDist = 5;
const float searchDistInv = 0.2;
const int numBinarySearchSteps = 5;
const float maxDDepth = 1.0;
const float maxDDepthInv = 1.0;


const float reflectionSpecularFalloffExponent = 3.0;

float ssr_lod = 2;

vec3 hashVector(vec3 vector, vec3 scale, float K){

    vector = fract(vector*scale);
    vector += dot(vector, vector.yxz + K);

    return fract((vector.xxy + vector.yxx)*vector.zyx);
}


vec3 BinarySearch(sampler2D positionTexture, vec3 dir, inout vec3 hitCoord, out float dDepth)
{
    float depth;


    for(int i = 0; i < numBinarySearchSteps; i++)
    {
        vec4 projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoord.xy /= projectedCoord.w;
        projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;


        depth = texture2D(positionTexture, projectedCoord.xy).z;


        dDepth = hitCoord.z - depth;


        if(dDepth > 0.0)
        hitCoord += dir;


        dir *= 0.5;
        hitCoord -= dir;
    }


    vec4 projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
    projectedCoord.xy /= projectedCoord.w;
    projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;


    return vec3(projectedCoord.xy, depth);
}
vec4 rayMarch(sampler2D positionTexture, vec3 dir, inout vec3 hitCoord, out float dDepth)
{
    dir *= rayStep;


    float depth;


    for(int i = 0; i < maxSteps; i++)
    {
        hitCoord += dir;


        vec4 projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoord.xy /= projectedCoord.w;
        projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;


        depth = texture2D(positionTexture, projectedCoord.xy).z;


        dDepth = hitCoord.z - depth;


        if(dDepth < 0.0)
        return vec4(BinarySearch(positionTexture, dir, hitCoord, dDepth), 1.0);
    }


    return vec4(0.0, 0.0, 0.0, 0.0);
}