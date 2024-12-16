#version 460

uniform mat4 projection_matrix;
uniform mat4 view_matrix;
uniform mat4 model_matrix;

uniform vec3 scale_vector;


layout (location = 0) in vec3 position;


void main() {
    vec3 scale_factors = scale_vector;
    mat4 scale_matrix = mat4(1.0); // Identity matrix
    scale_matrix[0][0] = scale_factors.x; // Scale X
    scale_matrix[1][1] = scale_factors.y; // Scale Y
    scale_matrix[2][2] = scale_factors.z; // Scale Z

    // Apply scaling to the model matrix
    mat4 scaled_model_matrix = model_matrix * scale_matrix;

    gl_Position = projection_matrix * view_matrix * scaled_model_matrix * vec4(position, 1);
}