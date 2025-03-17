/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.examples.java.common.samplerender;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.util.Log;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A collection of vertices, faces, and other attributes that define how to render a 3D object.
 *
 * <p>To render the mesh, use {@link SampleRender#draw()}.
 */
public class Mesh implements Closeable {
  private static final String TAG = Mesh.class.getSimpleName();

  /**
   * The kind of primitive to render.
   *
   * <p>This determines how the data in {@link VertexBuffer}s are interpreted. See <a
   * href="https://www.khronos.org/opengl/wiki/Primitive">here</a> for more on how primitives
   * behave.
   */
  public enum PrimitiveMode {
    POINTS(GLES30.GL_POINTS),
    LINE_STRIP(GLES30.GL_LINE_STRIP),
    LINE_LOOP(GLES30.GL_LINE_LOOP),
    LINES(GLES30.GL_LINES),
    TRIANGLE_STRIP(GLES30.GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GLES30.GL_TRIANGLE_FAN),
    TRIANGLES(GLES30.GL_TRIANGLES);

    /* package-private */
    final int glesEnum;

    private PrimitiveMode(int glesEnum) {
      this.glesEnum = glesEnum;
    }
  }

  private final int[] vertexArrayId = {0};
  private final PrimitiveMode primitiveMode;
  private final IndexBuffer indexBuffer;
  private final VertexBuffer[] vertexBuffers;

  /**
   * Construct a {@link Mesh}.
   *
   * <p>The data in the given {@link IndexBuffer} and {@link VertexBuffer}s does not need to be
   * finalized; they may be freely changed throughout the lifetime of a {@link Mesh} using their
   * respective {@code set()} methods.
   *
   * <p>The ordering of the {@code vertexBuffers} is significant. Their array indices will
   * correspond to their attribute locations, which must be taken into account in shader code. The
   * <a href="https://www.khronos.org/opengl/wiki/Layout_Qualifier_(GLSL)">layout qualifier</a> must
   * be used in the vertex shader code to explicitly associate attributes with these indices.
   */
  public Mesh(
      SampleRender render,
      PrimitiveMode primitiveMode,
      IndexBuffer indexBuffer,
      VertexBuffer[] vertexBuffers) {
    if (vertexBuffers == null || vertexBuffers.length == 0) {
      throw new IllegalArgumentException("Must pass at least one vertex buffer");
    }

    this.primitiveMode = primitiveMode;
    this.indexBuffer = indexBuffer;
    this.vertexBuffers = vertexBuffers;

    try {
      // Create vertex array
      GLES30.glGenVertexArrays(1, vertexArrayId, 0);
      GLError.maybeThrowGLException("Failed to generate a vertex array", "glGenVertexArrays");

      // Bind vertex array
      GLES30.glBindVertexArray(vertexArrayId[0]);
      GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");

      if (indexBuffer != null) {
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
      }

      for (int i = 0; i < vertexBuffers.length; ++i) {
        // Bind each vertex buffer to vertex array
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffers[i].getBufferId());
        GLError.maybeThrowGLException("Failed to bind vertex buffer", "glBindBuffer");
        GLES30.glVertexAttribPointer(
            i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_FLOAT, false, 0, 0);
        GLError.maybeThrowGLException(
            "Failed to associate vertex buffer with vertex array", "glVertexAttribPointer");
        GLES30.glEnableVertexAttribArray(i);
        GLError.maybeThrowGLException(
            "Failed to enable vertex buffer", "glEnableVertexAttribArray");
      }
    } catch (Throwable t) {
      close();
      throw t;
    }
  }

  /**
   * Constructs a {@link Mesh} from the given Wavefront OBJ file.
   *
   * <p>The {@link Mesh} will be constructed with three attributes, indexed in the order of local
   * coordinates (location 0, vec3), texture coordinates (location 1, vec2), and vertex normals
   * (location 2, vec3).
   */
  public static Mesh createFromAsset(SampleRender render, String assetFileName) throws IOException {
    try (InputStream inputStream = render.getAssets().open(assetFileName)) {
      Obj obj = ObjUtils.convertToRenderable(ObjReader.read(inputStream));

      // Obtain the data from the OBJ, as direct buffers:
      IntBuffer vertexIndices = ObjData.getFaceVertexIndices(obj, /*numVerticesPerFace=*/ 3);
      FloatBuffer localCoordinates = ObjData.getVertices(obj);
      FloatBuffer textureCoordinates = ObjData.getTexCoords(obj, /*dimensions=*/ 2);
      FloatBuffer normals = ObjData.getNormals(obj);

      VertexBuffer[] vertexBuffers = {
        new VertexBuffer(render, 3, localCoordinates),
        new VertexBuffer(render, 2, textureCoordinates),
        new VertexBuffer(render, 3, normals),
      };

      IndexBuffer indexBuffer = new IndexBuffer(render, vertexIndices);

      return new Mesh(render, Mesh.PrimitiveMode.TRIANGLES, indexBuffer, vertexBuffers);
    }
  }

  /**
   * Constructs a {@link Mesh} from a given Bitmap.
   *
   * <p>The {@link Mesh} will represent a simple rectangular plane with the Bitmap texture applied.
   */
  public static Mesh createFromBitmap(SampleRender render, Bitmap bitmap) throws IOException {
    // Define the 2D rectangle vertices (a simple quad)
    float[] vertices = {
            -1.0f, -1.0f, 0.0f, // Bottom-left corner (x, y, z)
            1.0f, -1.0f, 0.0f, // Bottom-right corner (x, y, z)
            -1.0f,  1.0f, 0.0f, // Top-left corner (x, y, z)
            1.0f,  1.0f, 0.0f  // Top-right corner (x, y, z)
    };

    // Define the texture coordinates corresponding to the vertices
    float[] textureCoords = {
            0.0f, 1.0f, // Bottom-left corner (U, V)
            1.0f, 1.0f, // Bottom-right corner (U, V)
            0.0f, 0.0f, // Top-left corner (U, V)
            1.0f, 0.0f  // Top-right corner (U, V)
    };

    // Define the indices for two triangles forming the rectangle
    int[] indices = {
            0, 1, 2, // First triangle (bottom-left, bottom-right, top-left)
            1, 3, 2  // Second triangle (bottom-right, top-right, top-left)
    };

    // Allocate direct buffers for vertex data and texture coordinates
    ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
    vertexByteBuffer.order(ByteOrder.nativeOrder());
    FloatBuffer vertexBuffer = vertexByteBuffer.asFloatBuffer();
    vertexBuffer.put(vertices);
    vertexBuffer.position(0);

    ByteBuffer texCoordByteBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4);
    texCoordByteBuffer.order(ByteOrder.nativeOrder());
    FloatBuffer texCoordBuffer = texCoordByteBuffer.asFloatBuffer();
    texCoordBuffer.put(textureCoords);
    texCoordBuffer.position(0);

    // Create index buffer (note that IndexBuffer requires IntBuffer)
    ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(indices.length * 4);
    indexByteBuffer.order(ByteOrder.nativeOrder());
    IntBuffer indexBuffer = indexByteBuffer.asIntBuffer();
    indexBuffer.put(indices);
    indexBuffer.position(0);

    // Create vertex buffers for positions and texture coordinates
    VertexBuffer[] vertexBuffers = {
            new VertexBuffer(render, 3, vertexBuffer), // Vertex positions (3D)
            new VertexBuffer(render, 2, texCoordBuffer) // Texture coordinates (2D)
    };

    // Create index buffer for drawing the mesh
    IndexBuffer meshIndexBuffer = new IndexBuffer(render, indexBuffer);

    // Create and return the mesh
    return new Mesh(render, Mesh.PrimitiveMode.TRIANGLES, meshIndexBuffer, vertexBuffers);
  }



  @Override
  public void close() {
    if (vertexArrayId[0] != 0) {
      GLES30.glDeleteVertexArrays(1, vertexArrayId, 0);
      GLError.maybeLogGLError(
          Log.WARN, TAG, "Failed to free vertex array object", "glDeleteVertexArrays");
    }
  }

  /**
   * Draws the mesh. Don't call this directly unless you are doing low level OpenGL code; instead,
   * prefer {@link SampleRender#draw}.
   */
  public void lowLevelDraw() {
    if (vertexArrayId[0] == 0) {
      throw new IllegalStateException("Tried to draw a freed Mesh");
    }

    GLES30.glBindVertexArray(vertexArrayId[0]);
    GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");
    if (indexBuffer == null) {
      // Sanity check for debugging
      int numberOfVertices = vertexBuffers[0].getNumberOfVertices();
      for (int i = 1; i < vertexBuffers.length; ++i) {
        if (vertexBuffers[i].getNumberOfVertices() != numberOfVertices) {
          throw new IllegalStateException("Vertex buffers have mismatching numbers of vertices");
        }
      }
      GLES30.glDrawArrays(primitiveMode.glesEnum, 0, numberOfVertices);
      GLError.maybeThrowGLException("Failed to draw vertex array object", "glDrawArrays");
    } else {
      GLES30.glDrawElements(
          primitiveMode.glesEnum, indexBuffer.getSize(), GLES30.GL_UNSIGNED_INT, 0);
      GLError.maybeThrowGLException(
          "Failed to draw vertex array object with indices", "glDrawElements");
    }
  }
}
