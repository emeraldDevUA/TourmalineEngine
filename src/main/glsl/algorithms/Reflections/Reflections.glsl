

const float step = 0.01;
const float minRayStep = 0.05;
const float maxSteps = 64;
const int numBinarySearchSteps = 5;
const float reflectionSpecularFalloffExponent = 4.0;

float ssr_lod = 2;

vec3 hashVector(vec3 vector, vec3 scale, float K){

    vector = fract(vector*scale);
    vector += dot(vector, vector.yxz + K);

    return fract((vector.xxy + vector.yxx)*vector.zyx);
}

//vec3 BinarySearch(sampler2D positionTexture, inout vec3 direction,
//                    inout vec3 hitCoord, inout float dDepth)
//{
//    float depth;
//
//    vec4 projectedCoord;
//
//    for(int i = 0; i < numBinarySearchSteps; i++)
//    {
//
//        projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
//        projectedCoord.xy /= projectedCoord.w;
//        projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;
//
//        depth = textureLod(positionTexture, projectedCoord.xy, ssr_lod).z;
//
//
//        dDepth = hitCoord.z - depth;
//
//        direction *= 0.5;
//
//        if(dDepth > 10E-6){
//            hitCoord += direction;
//        }
//        else{
//            hitCoord -= direction;
//        }
//    }
//
//    projectedCoord = projection_matrix * vec4(hitCoord, 1.0);
//    projectedCoord.xy /= projectedCoord.w;
//    projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;
//
//    return vec3(projectedCoord.xy, depth);
//}

vec3 BinarySearch(sampler2D positionTexture, vec3 direction, vec3 hitCoord, float dDepth) {
    for (int j = 0; j < 5; j++) { // 5 refinement steps
         hitCoord += direction * 0.5; // Refine in smaller steps
         float depth = textureLod(positionTexture, hitCoord.xy, 0.0).z; // Use LOD 0 for accuracy
         dDepth = hitCoord.z - depth;
         if (abs(dDepth) < 0.003) {
             break;
         }

    }
    return hitCoord;
}

vec4 rayMarch(sampler2D positionTexture, vec3 direction, inout vec3 hitCoord, out float dDepth) {
    vec4 projectedCoords;
    float depth;

    direction = normalize(direction) * step; // Ensure proper step size

    for (int i = 0; i < maxSteps; i++) {
        hitCoord += direction;

        projectedCoords = projection_matrix * vec4(hitCoord, 1.0);
        projectedCoords.xy /= projectedCoords.w;
        projectedCoords.xy = projectedCoords.xy * 0.5 + 0.5;

        if (projectedCoords.x < 0.0 || projectedCoords.x > 1.0 ||
        projectedCoords.y < 0.0 || projectedCoords.y > 1.0) {
            break; // Exit if outside screen bounds
        }

        depth = textureLod(positionTexture, projectedCoords.xy, 0.0).z;

        if (depth == 0.0 || depth > 500) {
            continue; // Ignore invalid depth values
        }

        dDepth = hitCoord.z - depth;
        float epsilon = 0.003; // Adjusted epsilon for better precision

        if (abs(dDepth) < epsilon) {
            return vec4(BinarySearch(positionTexture, direction, hitCoord, dDepth), 1.0);
        }
    }
    return vec4(projectedCoords.xy, depth, 0.0);
}
