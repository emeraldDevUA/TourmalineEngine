
// 1,0,0,0,1,1,1,1
struct pointLight{
    vec3 color;
    vec3 position;
    float intensity;
}PointLight;


struct directionalLight{
    vec3 color;
    vec3 direction;
    float intensity;
}DirectionalLight;


uniform int number_pointLights;
uniform int number_dirLights;



layout(std140, binding = 4) uniform LightBlock {
        directionalLight directionalLights[5];
        pointLight pointLights[50];
}lightBlock;