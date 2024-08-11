#version 460

in vec4 finalPos;
in vec2 textureCoords;
uniform sampler2D albedoMap;

out vec4 fragmentColor;


void main() {
    vec4 temp = texture(albedoMap, textureCoords);


}