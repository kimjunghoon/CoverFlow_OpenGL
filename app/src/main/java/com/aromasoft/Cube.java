package com.aromasoft;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Cube
{
    private IntBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private ByteBuffer mIndexBuffer;

    int one = 0x10000;

    int vertices[] =
    {
        -one, -one, 0,
        one, -one, 0,
        one, one, 0,
        -one, one, 0,
    };

    private float texture[] =
    {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };

    private byte indices[] =
    {
       0, 3, 2,
       0, 2, 1
    };


    public Cube()
    {
        initCube();
    }

    private void initCube()
    {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asIntBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer textureBuf = ByteBuffer.allocateDirect(texture.length * 4);
        textureBuf.order(ByteOrder.nativeOrder());
        mTextureBuffer = textureBuf.asFloatBuffer();
        mTextureBuffer.put(texture);
        mTextureBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }

    public void draw(GL10 gl)
    {
        gl.glFrontFace(GL10.GL_CW);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        gl.glTexCoordPointer( 2, GL10.GL_FLOAT, 0, mTextureBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
    }
}
