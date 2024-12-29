
float rgbToGrayScale(vec3 color){
    return color.r * 0.2116 + color.g * 0.7152 + color.b * 0.0722;
}


vec3 rgbToYCbCr(vec3 color) {
    float Y  = color.r * 0.299 + color.g * 0.587 + color.b * 0.114; // Luminance
    float Cb = color.r * -0.168736 + color.g * -0.331264 + color.b * 0.5 + 0.5; // Blue-difference chroma
    float Cr = color.r * 0.5 + color.g * -0.418688 + color.b * -0.081312 + 0.5; // Red-difference chroma
    return vec3(Y, Cb, Cr);
}

vec4 rgbToCmyk(vec3 color) {
    float K = 1.0 - max(max(color.r, color.g), color.b);
    float C = (1.0 - color.r - K) / (1.0 - K);
    float M = (1.0 - color.g - K) / (1.0 - K);
    float Y = (1.0 - color.b - K) / (1.0 - K);

    // Handle black edge case
    if (K >= 1.0) {
        C = 0.0;
        M = 0.0;
        Y = 0.0;
    }

    return vec4(C, M, Y, K); // CMYK as vec4
}


vec3 rgbToHsv(vec3 color) {
    float maxColor = max(max(color.r, color.g), color.b);
    float minColor = min(min(color.r, color.g), color.b);
    float delta = maxColor - minColor;

    float H = 0.0; // Hue
    if (delta > 0.0) {
        if (maxColor == color.r) {
            H = mod((color.g - color.b) / delta, 6.0);
        } else if (maxColor == color.g) {
            H = (color.b - color.r) / delta + 2.0;
        } else {
            H = (color.r - color.g) / delta + 4.0;
        }
        H *= 60.0;
    }

    float S = (maxColor == 0.0) ? 0.0 : delta / maxColor; // Saturation
    float V = maxColor;                                  // Value

    return vec3(H, S, V); // HSV as vec3
}