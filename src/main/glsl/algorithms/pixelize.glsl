#ifndef PIXELIZE_FRAGMENT
#define PIXELIZE_FRAGMENT

vec4 pixelize_fragment(sampler2D colorTexture, vec2 uvs, int pixelSize) {
    vec2 texSize = textureSize(colorTexture, 0).xy; // Texture size in pixels

    // Convert pixelSize to texture coordinate space
    vec2 pixelStep = vec2(pixelSize) / texSize;

    // Snap UVs to the center of the nearest pixel block
    vec2 pixelizedUV = floor(uvs / pixelStep) * pixelStep + pixelStep * 0.5;

    // Sample the texture using the pixelized coordinates
    return texture(colorTexture, pixelizedUV);
}
#endif