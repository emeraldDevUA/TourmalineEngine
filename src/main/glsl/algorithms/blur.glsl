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

vec4 gaussian_blur5(sampler2D color, vec2 uvs) {
    // Kernel size and range (6x6 kernel)
    int k = 3; // Kernel radius for 6x6
    vec3 blurredVertex = vec3(0.0);

    // 6x6 Gaussian kernel normalized
    float gaussKernel[6][6] = float[6][6](
    float[6](1,  4,  7,  7,  4,  1),
    float[6](4, 16, 26, 26, 16,  4),
    float[6](7, 26, 41, 41, 26,  7),
    float[6](7, 26, 41, 41, 26,  7),
    float[6](4, 16, 26, 26, 16,  4),
    float[6](1,  4,  7,  7,  4,  1)
    );

    // Normalize the kernel by dividing by its total sum (273)
    float normalizationFactor = 273.0;

    // Calculate texture offset for sampling
    vec2 texOffset = vec2(1.0 / textureSize(color, 0)); // texel size

    // Iterate over the kernel
    for (int i = -k; i <= k; i++) {
        for (int j = -k; j <= k; j++) {
            int kernelX = i + k; // Map kernel index to array index
            int kernelY = j + k;

            // Accumulate weighted samples from the texture
            blurredVertex += gaussKernel[kernelX][kernelY] / normalizationFactor *
            texture(color, uvs + vec2(i, j) * texOffset).rgb;
        }
    }

    return vec4(blurredVertex, 1.0);
}

vec4 bloom_blur(sampler2D color, vec2 uvs ){

    const float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
    vec3 result = texture(color, uvs).rgb * weight[0];


    if(0 == 0)
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(color, uvs + vec2(uvs.x * i, 0.0)).rgb * weight[i];
            result += texture(color, uvs - vec2(uvs.x * i, 0.0)).rgb * weight[i];
        }
    }
    else
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(color, uvs + vec2(0.0, uvs.y * i)).rgb * weight[i];
            result += texture(color, uvs - vec2(0.0, uvs.y * i)).rgb * weight[i];
        }
    }

    return vec4(result, 1.0);
}

#endif