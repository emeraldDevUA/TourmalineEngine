#ifndef GAUSS_BLUR_GLSL_INCLUDED
#define GAUSS_BLUR_GLSL_INCLUDED

vec4 gaussian_blur(sampler2D color, vec2 uvs){
    // Kernel size and range
    int k = 1;
    vec3 blurredVertex = vec3(0, 0, 0);

    // 3x3 Gaussian kernel normalized
    mat3 gaussKernel =
    mat3(1, 2, 1, 2, 4, 2, 1, 2, 1);

    gaussKernel /= 16.0;

    vec2 texOffset = vec2(1.0 / textureSize(color, 0));  // Assuming 'color' is the sampler2D

    // Iterate over the kernel
    for(int i = -k; i <= k; i++){
        for(int j = -k; j <= k; j++){

            int kernelX = i + k;
            int kernelY = j + k;

            blurredVertex += gaussKernel[kernelX][kernelY] *

            texture(color, uvs + vec2(i, j) * texOffset).xyz;
        }
    }

    return vec4(blurredVertex, 1.0);
}
#endif