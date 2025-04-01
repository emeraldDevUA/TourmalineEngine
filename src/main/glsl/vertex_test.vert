#version 460 core



layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

uniform mat4 projection_matrix;
uniform mat4 view_matrix;
uniform mat4 model_matrix;


out vec2 textureCoords;
out vec4 currentPosition;


void main() {
    // WHY IS THIS NOT WORKING???
    gl_Position = (projection_matrix*view_matrix*model_matrix * vec4(position, 1));
    currentPosition = projection_matrix*view_matrix*model_matrix * vec4(position, 1);
    textureCoords = uv;

}