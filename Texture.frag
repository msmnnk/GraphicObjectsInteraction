#version 330 core

/*
 * This file contains code fragments
 * from the initial shader Gouraud.frag
 */

// Task 3: fragment shader which implements blending of texture
in vec4 color;
in vec2 texCoord;
out vec4 fColor;
uniform sampler2D tex;

void main()
{
		fColor = color * texture(tex, texCoord);
}