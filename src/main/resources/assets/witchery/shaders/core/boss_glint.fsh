#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec3 texel = texture(Sampler0, texCoord0).rgb;
    float intensity = min(max(texel.r, max(texel.g, texel.b)) * (255.0 / 164.0), 1.0);
    fragColor = intensity * vertexColor * ColorModulator * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}
