#version 460 core

layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;


out vec4 finalPosition;
out vec2 textureCoords;

void main() {

    finalPosition = projectionMatrix * viewMatrix * modelMatrix * position;
    textureCoords = texCoords;

}