

struct pointLight{
    vec3 color;
    vec3 position;
    float intenstity;
    float padding;
}PointLight;


struct directionalLight{
    vec3 color;
    vec3 direction;
    float intenstity;
    float padding;

}DirectionalLight;


uniform int number_pointLights;
uniform int number_dirLights;


layout(std140, binding = 4) uniform LightBlock {
    directionalLight dirLights[5];
    pointLight pointLights[50];
};