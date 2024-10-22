#version 460 core


layout (binding = 4) uniform sampler2D albedo_map;
layout (binding = 7) uniform sampler2D normal_map;

in vec2 textureCoords;
out vec4 fragment;

void main() {
    fragment = texture(albedo_map, textureCoords);
}