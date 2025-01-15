

const float step = 0.01;
const float minRayStep = 0.05;
const float maxSteps = 20;
const int numBinarySearchSteps = 5;
const float reflectionSpecularFalloffExponent = 4.0;

float ssr_lod = 2;

vec3 hashVector(vec3 vector, vec3 scale, float K){

    vector = fract(vector*scale);
    vector += dot(vector, vector.yxz + K);

    return fract((vector.xxy + vector.yxx)*vector.zyx);
}

vec3 BinarySearch(sampler2D positionTexture, inout vec3 direction,
                    inout vec3 hitCoord, inout float dDepth)
{
    float depth;

    vec4 projectedCoord;

    for(int i = 0; i < numBinarySearchSteps; i++)
    {

        projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoord.xy /= projectedCoord.w;
        projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

        depth = textureLod(positionTexture, projectedCoord.xy, ssr_lod).z;


        dDepth = hitCoord.z - depth;

        direction *= 0.5;

        if(dDepth > 10E-6){
            hitCoord += direction;
        }
        else{
            hitCoord -= direction;
        }
    }

    projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
    projectedCoord.xy /= projectedCoord.w;
    projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

    return vec3(projectedCoord.xy, depth);
}

vec4 rayMarch(sampler2D positionTexture, vec3 direction,
                inout vec3 hitCoord, out float dDepth){
    vec4 projectedCoords;
    float depth;

    direction *= step;

    for(int i = 0; i < maxSteps; i ++){

        hitCoord += direction;

        projectedCoords = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoords.xy /= projectedCoords.w;
        projectedCoords.xy = projectedCoords.xy * 0.5 + 0.5;

        depth = textureLod(positionTexture, projectedCoords.xy, ssr_lod).z;
        if(depth > 1000.0){
            continue;
        }

        dDepth = hitCoord.z - depth;
        if((direction.z - dDepth) < 1.2)
        {
            if(dDepth <= 0.0)
            {
                return vec4(BinarySearch(positionTexture, direction, hitCoord, dDepth), 1.0);
            }
        }

        //steps++;

    }



    return vec4(projectedCoords.xy, depth, 0.0);

}