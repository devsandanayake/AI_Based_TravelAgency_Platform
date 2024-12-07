uniform mat4 u_ModelViewProjection;
attribute vec3 a_Position;
attribute vec2 a_TexCoord;
varying vec2 v_TexCoord;

void main() {
    v_TexCoord = a_TexCoord;
    gl_Position = u_ModelViewProjection * vec4(a_Position, 1.0);
}
