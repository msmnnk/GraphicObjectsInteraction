#version 330 core

/*
 * This file contains code fragments
 * from the initial shader Gouraud.vert
 */

// Task 3: vertex shader which implements Gouraud shading
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vNormal;
layout(location = 2) in vec2 vTexArray;

out vec4 color;
out vec2 texCoord;

uniform mat4 ModelView;
uniform mat4 NormalTransform;
uniform mat4 Projection;
uniform vec4 LightPosition;
uniform vec4 AmbientProduct, DiffuseProduct, SpecularProduct;
uniform float Shininess;

void main()
{
    // transform vertex position into eye coordinates
    vec3 ecPosition = (ModelView * vPosition).xyz;
    // the light position is defined in eye coordinates
    vec3 L = normalize(LightPosition.xyz - ecPosition);
    // if the light position is defined in world coordinates,
    // the next line is used instead of the above
    vec3 E = normalize(-ecPosition);
    vec3 H = normalize(L + E);
    // transform vertex normal into eye coordinates
    vec3 N = normalize((NormalTransform * vec4(vNormal, 0)).xyz);
    // compute terms in the illumination equation
    vec4 ambient = AmbientProduct;
    float Kd = max(dot(L, N), 0.0);
    vec4 diffuse = Kd*DiffuseProduct;
    float Ks = pow(max(dot(N, H), 0.0), Shininess);
    vec4 specular = Ks * SpecularProduct;
    if (dot(L, N) < 0.0) {
        specular = vec4(0.0, 0.0, 0.0, 1.0);
    }
    gl_Position = Projection * ModelView * vPosition;
    texCoord = vTexArray;
    color = ambient + diffuse + specular;
    color.a = 1.0;
}
