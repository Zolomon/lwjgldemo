package game;

import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import utils.*;
import utils.PNGDecoder.Format;

import base.GLProgram;

public class MainGame extends GLProgram {

	private static final int size_of_float = 4;
	private static final int size_of_vec4 = 4;
	private static final int nbr_of_color_vec4 = 3;

	public static void main(String[] args) {
		new MainGame().run(true);
	}

	private ShaderProgram program;
	private int vbo;
	private int indicesCount;

	// Texture variables
	private Texture uvgrid;
	private Texture bwgrid;
	private int vao;

	public MainGame() {
		super("Example 2.2", 500, 500, true);
	}

	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);

		uvgrid = new Texture("/uvgrid.png");
		bwgrid = new Texture("/bwgrid.png");

		glActiveTexture(GL_TEXTURE1);
		int tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, uvgrid.getWidth(), uvgrid.getHeight(), 0, GL_RGBA , GL_UNSIGNED_BYTE, uvgrid.getBuffer());
		
		
		HashMap<Integer, String> attributes = new HashMap<Integer, String>();

		attributes.put(0, "in_position");
		attributes.put(1, "in_color");
		attributes.put(2, "in_TextureCoord");

		program = new ShaderProgram(readFromFile("/game.vert"),
				readFromFile("/game.frag"), attributes);
		//
		// //
		// // final float[] vertexPositions = new float[] {
		// // // Left bottom triangle
		// // -0.5f, 0.5f, 0f,
		// // -0.5f, -0.5f, 0f,
		// // 0.5f, -0.5f, 0f,
		// // // Right top triangle
		// // 0.5f, -0.5f, 0f,
		// // 0.5f, 0.5f, 0f,
		// // -0.5f, 0.5f, 0f
		// // };
		//
		// We'll define our quad using 4 vertices of the custom 'TexturedVertex'
		// class
		TexturedVertex v0 = new TexturedVertex();
		v0.setXYZ(-0.5f, 0.5f, 0);
		v0.setRGB(1, 0, 0);
		v0.setST(0, 0);
		TexturedVertex v1 = new TexturedVertex();
		v1.setXYZ(-0.5f, -0.5f, 0);
		v1.setRGB(0, 1, 0);
		v1.setST(0, 1);
		TexturedVertex v2 = new TexturedVertex();
		v2.setXYZ(0.5f, -0.5f, 0);
		v2.setRGB(0, 0, 1);
		v2.setST(1, 1);
		TexturedVertex v3 = new TexturedVertex();
		v3.setXYZ(0.5f, 0.5f, 0);
		v3.setRGB(1, 1, 1);
		v3.setST(1, 0);

		TexturedVertex[] vertices = new TexturedVertex[] { v0, v1, v2, v3 };
		FloatBuffer verticesBuffer = createVertexBuffer(vertices);

		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = { 0, 1, 2, 2, 3, 0 };

		indicesCount = indices.length;
		ByteBuffer indicesBuffer = createIndexBuffer(indices);

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		vbo = glGenBuffers();
		// Bind vbo to Gl_ARRAY_BUFFER
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		// 1. Allocate memory in the GL_ARRAY_BUFFER with the size of our
		// vertexPositions
		// 2. Copy data from our vertexPositions into the GL_ARRAY_BUFFER on the
		// GPU.
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

		// Put the position coordinates in attribute list 0
		glVertexAttribPointer(0, TexturedVertex.positionElementCount, GL_FLOAT,
				false, TexturedVertex.stride, TexturedVertex.positionByteOffset);

		// Put the color components in attribute list 1
		glVertexAttribPointer(1, TexturedVertex.colorElementCount, GL_FLOAT,
				false, TexturedVertex.stride, TexturedVertex.colorByteOffset);

		// Put the texture coordinates in attribute list 2
		glVertexAttribPointer(2, TexturedVertex.textureElementCount, GL_FLOAT,
				false, TexturedVertex.stride, TexturedVertex.textureByteOffset);

		/*
		 * ### Clean up the buffer! ### By binding the buffer object 0 to
		 * GL_ARRAY_BUFFER, we cause the buffer object previously bound to that
		 * target to become unbound from it. Zero in this cases works a lot like
		 * the NULL pointer. This was not strictly necessary, as any later binds
		 * to this target will simply unbind what is already there. But unless
		 * you have very strict control over your rendering, it is usually a
		 * good idea to unbind the objects you bind.
		 */
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Cleanup VAO!
		glBindVertexArray(0);

		// Create a new VBO for the indices and select it (bind) - INDICES
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glGenBuffers());
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

	}

	private ByteBuffer createIndexBuffer(byte[] indices) {
		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		return indicesBuffer;
	}

	private FloatBuffer createVertexBuffer(TexturedVertex[] vertices) {
		// Put each 'Vertex' in one FloatBuffer
		FloatBuffer verticesBuffer = BufferUtils
				.createFloatBuffer(vertices.length
						* TexturedVertex.elementCount);
		for (int i = 0; i < vertices.length; i++) {
			// Add position, color and texture floats to the buffer
			verticesBuffer.put(vertices[i].getElements());
		}
		verticesBuffer.flip();
		return verticesBuffer;
	}

	@Override
	public void render() {

		glClearColor(0.21f, 0.18f, 0.18f, 0);
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT);

		program.begin();

		// glActiveTexture(GL_TEXTURE0);
		// glBindTexture(GL_TEXTURE_2D, texIds[textureSelector]);

		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);

		// Bind vbo to Gl_ARRAY_BUFFER
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
		
		
		/*
		 * This is how data flows down the pipeline in OpenGL. When rendering
		 * starts, vertex data in a buffer object is read based on setup work
		 * done by glVertexAttribPointer. This function describes where the data
		 * for an attribute comes from.
		 * 
		 * The connection between a particular call to glVertexAttribPointer and
		 * the string name of an input value to a vertex shader is somewhat
		 * complicated.
		 * 
		 * Each input to a vertex shader has an index location called an
		 * attribute index. The input in this shader was defined with this
		 * statement:
		 * 
		 * layout(location = 0) in vec4 position;
		 * 
		 * In code, when referring to attributes, they are always referred to by
		 * attribute index. The functions glEnableVertexAttribArray,
		 * glDisableVertexAttribArray, and glVertexAttribPointer all take as
		 * their first parameter an attribute index. We assigned the attribute
		 * index of the position attribute to 0 in the vertex shader, so the
		 * call to glEnableVertexAttribArray(0) enables the attribute index for
		 * the position attribute.
		 * 
		 * Without the call to glEnableVertexAttribArray, calling
		 * glVertexAttribPointer on that attribute index would not mean much.
		 * The enable call does not have to be called before the vertex
		 * attribute pointer call, but it does need to be called before
		 * rendering. If the attribute is not enabled, it will not be used
		 * during rendering.
		 */
		// glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		// glVertexAttribPointer(1, 4, GL_FLOAT, false, 0,
		// (size_of_float*size_of_vec4*nbr_of_color_vec4 ));

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, uvgrid.getWidth(),
		 uvgrid.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
		 uvgrid.getBuffer());

		// glDrawArrays(GL_TRIANGLES, 0, 6);

		glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_BYTE, 0);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		// Clean up the buffer
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindVertexArray(0);

		program.end();

	}
}
