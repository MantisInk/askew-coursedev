/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
 ******************************************************************************/

package askew;
//import static com.badlogic.gdx.graphics.g2d.Sprite.SPRITE_SIZE;
//import static com.badlogic.gdx.graphics.g2d.Sprite.VERTEX_SIZE;


import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.NumberUtils;

/** A PolygonSpriteBatch is used to draw 2D polygons that reference a texture (region). The class will batch the drawing commands
 * and optimize them for processing by the GPU.
 * <p>
 * To draw something with a PolygonSpriteBatch one has to first call the {@link PolygonSpriteBatch#begin()} method which will
 * setup appropriate render states. When you are done with drawing you have to call {@link PolygonSpriteBatch#end()} which will
 * actually draw the things you specified.
 * <p>
 * All drawing commands of the PolygonSpriteBatch operate in screen coordinates. The screen coordinate system has an x-axis
 * pointing to the right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also
 * provide your own transformation and projection matrices if you so wish.
 * <p>
 * A PolygonSpriteBatch is managed. In case the OpenGL context is lost all OpenGL resources a PolygonSpriteBatch uses internally
 * get invalidated. A context is lost when a user switches to another application or receives an incoming call on Android. A
 * SpritPolygonSpriteBatcheBatch will be automatically reloaded after the OpenGL context is restored.
 * <p>
 * A PolygonSpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * <p>
 * A PolygonSpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use its own custom shader to draw
 * all provided sprites. You can set your own custom shader via {@link #setShader(ShaderProgram)}.
 * <p>
 * A PolygonSpriteBatch has to be disposed if it is no longer used.
 * @author mzechner
 * @author Stefan Bachmann
 * @author Nathan Sweet */
public class CustomSpriteBatch implements Batch {
    private Mesh mesh;

    private final int VERTEX_SIZE = 6;
    private final int SPRITE_SIZE = 4 * VERTEX_SIZE;


    private final float[] vertices;
    private final short[] triangles;
    private int vertexIndex, triangleIndex;
    private Texture lastTexture;
    private float invTexWidth = 0, invTexHeight = 0;
    private boolean drawing;

    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();

    private boolean blendingDisabled;
    private int blendSrcFunc = GL20.GL_SRC_ALPHA;
    private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;

    private final ShaderProgram shader;
    private ShaderProgram customShader;
    private boolean ownsShader;

    float color = Color.WHITE.toFloatBits();
    private Color tempColor = new Color(1, 1, 1, 1);

    /** Number of render calls since the last {@link #begin()}. **/
    public int renderCalls = 0;

    /** Number of rendering calls, ever. Will not be reset unless set manually. **/
    public int totalRenderCalls = 0;

    /** The maximum number of triangles rendered in one batch so far. **/
    public int maxTrianglesInBatch = 0;

    /** Constructs a new PolygonSpriteBatch with a size of 2000, the default shader, and one buffer.
     * @see PolygonSpriteBatch#PolygonSpriteBatch(int, ShaderProgram) */
    public CustomSpriteBatch () {
        this(2000, null);
    }

    /** Constructs a PolygonSpriteBatch with the default shader and one buffer.
     * @see PolygonSpriteBatch#PolygonSpriteBatch(int, ShaderProgram) */
    public CustomSpriteBatch (int size) {
        this(size, null);
    }

    /** Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
     * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
     * with respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link SpriteBatch#createDefaultShader()}.
     * @param size The max number of vertices and number of triangles in a single batch. Max of 10920.
     * @param defaultShader The default shader to use. This is not owned by the PolygonSpriteBatch and must be disposed
     *           separately. */
    public CustomSpriteBatch (int size, ShaderProgram defaultShader) {
        // 32767 is max index, so 32767 / 3 - (32767 / 3 % 3) = 10920.
        if (size > 10920) throw new IllegalArgumentException("Can't have more than 10920 triangles per batch: " + size);

        Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexArray;
        if (Gdx.gl30 != null) {
            vertexDataType = VertexDataType.VertexBufferObjectWithVAO;
        }
        mesh = new Mesh(vertexDataType, false, size, size * 3,
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        vertices = new float[size * VERTEX_SIZE];
        triangles = new short[size * 3];

        if (defaultShader == null) {
            shader = createDefaultShader();
            ownsShader = true;
        } else
            shader = defaultShader;

        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    static public ShaderProgram createDefaultShader () {
        String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
        String fragmentShader = "#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP \n" //
                + "#endif\n" //
                + "varying LOWP vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "uniform sampler2D u_texture;\n" //
                + "void main()\n"//
                + "{\n" //
                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
                + "}";

        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }

    @Override
    public void begin () {
        if (drawing) throw new IllegalStateException("PolygonSpriteBatch.end must be called before begin.");
        renderCalls = 0;

        Gdx.gl.glDepthMask(true);
        if (customShader != null)
            customShader.begin();
        else
            shader.begin();
        setupMatrices();

        drawing = true;
    }

    @Override
    public void end () {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before end.");
        if (vertexIndex > 0) flush();
        lastTexture = null;
        drawing = false;

        GL20 gl = Gdx.gl;
        gl.glDepthMask(true);
        if (isBlendingEnabled()) gl.glDisable(GL20.GL_BLEND);

        if (customShader != null)
            customShader.end();
        else
            shader.end();
    }

    @Override
    public void setColor (Color tint) {
        color = tint.toFloatBits();
    }

    @Override
    public void setColor (float r, float g, float b, float a) {
        int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
        color = NumberUtils.intToFloatColor(intBits);
    }

    @Override
    public void setColor (float color) {
        this.color = color;
    }

    @Override
    public Color getColor () {
        int intBits = NumberUtils.floatToIntColor(color);
        Color color = this.tempColor;
        color.r = (intBits & 0xff) / 255f;
        color.g = ((intBits >>> 8) & 0xff) / 255f;
        color.b = ((intBits >>> 16) & 0xff) / 255f;
        color.a = ((intBits >>> 24) & 0xff) / 255f;
        return color;
    }

    @Override
    public float getPackedColor () {
        return color;
    }

    /** Draws a polygon region with the bottom left corner at x,y having the width and height of the region. */
    public void draw (PolygonRegion region, float x, float y) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final short[] regionTriangles = region.getTriangles();
        final int regionTrianglesLength = regionTriangles.length;
        final float[] regionVertices = region.getVertices();
        final int regionVerticesLength = regionVertices.length;

        final Texture texture = region.getRegion().getTexture();
        if (texture != lastTexture)
            switchTexture(texture);
        else if (triangleIndex + regionTrianglesLength > triangles.length
                || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length) flush();

        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = 0; i < regionTrianglesLength; i++)
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        final float[] vertices = this.vertices;
        final float color = this.color;
        final float[] textureCoords = region.getTextureCoords();

        for (int i = 0; i < regionVerticesLength; i += 2) {
            vertices[vertexIndex++] = regionVertices[i] + x;
            vertices[vertexIndex++] = regionVertices[i + 1] + y;
            vertices[vertexIndex++] = 0;
            vertices[vertexIndex++] = color;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
        }
        this.vertexIndex = vertexIndex;
    }

    /** Draws a polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height. */
    public void draw (PolygonRegion region, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final short[] regionTriangles = region.getTriangles();
        final int regionTrianglesLength = regionTriangles.length;
        final float[] regionVertices = region.getVertices();
        final int regionVerticesLength = regionVertices.length;
        final TextureRegion textureRegion = region.getRegion();

        final Texture texture = textureRegion.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);
        else if (triangleIndex + regionTrianglesLength > triangles.length
                || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length) flush();

        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = 0, n = regionTriangles.length; i < n; i++)
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        final float[] vertices = this.vertices;
        final float color = this.color;
        final float[] textureCoords = region.getTextureCoords();
        final float sX = width / textureRegion.getRegionWidth();
        final float sY = height / textureRegion.getRegionHeight();

        for (int i = 0; i < regionVerticesLength; i += 2) {
            vertices[vertexIndex++] = regionVertices[i] * sX + x;
            vertices[vertexIndex++] = regionVertices[i + 1] * sY + y;
            vertices[vertexIndex++] = 0;
            vertices[vertexIndex++] = color;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
        }
        this.vertexIndex = vertexIndex;
    }

    /** Draws the polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height.
     * The polygon region is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the
     * polygon region should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the
     * rectangle around originX, originY. */
    public void draw (PolygonRegion region, float x, float y, float originX, float originY, float width, float height,
                      float scaleX, float scaleY, float rotation) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final short[] regionTriangles = region.getTriangles();
        final int regionTrianglesLength = regionTriangles.length;
        final float[] regionVertices = region.getVertices();
        final int regionVerticesLength = regionVertices.length;
        final TextureRegion textureRegion = region.getRegion();

        Texture texture = textureRegion.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);
        else if (triangleIndex + regionTrianglesLength > triangles.length
                || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length) flush();

        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = 0; i < regionTrianglesLength; i++)
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        final float[] vertices = this.vertices;
        final float color = this.color;
        final float[] textureCoords = region.getTextureCoords();

        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        final float sX = width / textureRegion.getRegionWidth();
        final float sY = height / textureRegion.getRegionHeight();
        final float cos = MathUtils.cosDeg(rotation);
        final float sin = MathUtils.sinDeg(rotation);

        float fx, fy;
        for (int i = 0; i < regionVerticesLength; i += 2) {
            fx = (regionVertices[i] * sX - originX) * scaleX;
            fy = (regionVertices[i + 1] * sY - originY) * scaleY;
            vertices[vertexIndex++] = cos * fx - sin * fy + worldOriginX;
            vertices[vertexIndex++] = sin * fx + cos * fy + worldOriginY;
            vertices[vertexIndex++] = 0;
            vertices[vertexIndex++] = color;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
        }
        this.vertexIndex = vertexIndex;
    }


    @Override
    public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
                      float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        System.out.println("DELETED, REIMPLEMENT IF NEEDED 1");
        return;
    }

    @Override
    public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
                      int srcHeight, boolean flipX, boolean flipY) {
        System.out.println("DELETED, REIMPLEMENT IF NEEDED 2");
        return;
    }

    @Override
    public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        System.out.println("DELETED, REIMPLEMENT IF NEEDED 3");
    }

    @Override
    public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        System.out.println("DELETED, REIMPLEMENT IF NEEDED 4");
    }

    @Override
    public void draw (Texture texture, float x, float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw (Texture texture, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = 0;
        final float v = 1;
        final float u2 = 1;
        final float v2 = 0;

        float color = this.color;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;
        final int triangleCount = count / 20 * 6;
        if (texture != lastTexture) {
            switchTexture(texture);
        }
        else if (triangleIndex + triangleCount > triangles.length || vertexIndex + count > vertices.length) //
            flush();

        int vertexIndex = this.vertexIndex;
        int triangleIndex = this.triangleIndex;
        int vertex = (vertexIndex / VERTEX_SIZE);
        for (int n = triangleIndex + triangleCount; triangleIndex < n; triangleIndex += 6, vertex += 4) {
            triangles[triangleIndex] = (short)vertex;
            triangles[triangleIndex + 1] = (short)(vertex + 1);
            triangles[triangleIndex + 2] = (short)(vertex + 2);
            triangles[triangleIndex + 3] = (short)(vertex + 2);
            triangles[triangleIndex + 4] = (short)(vertex + 3);
            triangles[triangleIndex + 5] = (short)vertex;
        }
        this.triangleIndex = triangleIndex;

        for (int i = 0; i < count; i += 5) {
            vertices[vertexIndex++] = spriteVertices[offset + i];
            vertices[vertexIndex++] = spriteVertices[offset + i + 1];
            vertices[vertexIndex++] = 0f;
            vertices[vertexIndex++] = spriteVertices[offset + i + 2];
            vertices[vertexIndex++] = spriteVertices[offset + i + 3];
            vertices[vertexIndex++] = spriteVertices[offset + i + 4];
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw (TextureRegion region, float x, float y) {
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void draw (TextureRegion region, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);
        else if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        float color = this.color;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                      float scaleX, float scaleY, float rotation) {
        System.out.println("DELETED, REIMPLEMENT IF NEEDED 6");
    }

    @Override
    public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                      float scaleX, float scaleY, float rotation, boolean clockwise) {
        System.out.println("DELETED, REIMPLEMENT IF NEEDED 7");
    }

    @Override
    public void draw (TextureRegion region, float width, float height, Affine2 transform) {
        if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);
        else if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        // construct corner points
        float x1 = transform.m02;
        float y1 = transform.m12;
        float x2 = transform.m01 * height + transform.m02;
        float y2 = transform.m11 * height + transform.m12;
        float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
        float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
        float x4 = transform.m00 * width + transform.m02;
        float y4 = transform.m10 * width + transform.m12;

        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();

        float color = this.color;
        int idx = vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = 0;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertexIndex = idx;
    }

    @Override
    public void flush () {
        if (vertexIndex == 0) return;

        renderCalls++;
        totalRenderCalls++;
        int trianglesInBatch = triangleIndex;
        if (trianglesInBatch > maxTrianglesInBatch) maxTrianglesInBatch = trianglesInBatch;

        lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, vertexIndex);
        mesh.setIndices(triangles, 0, triangleIndex);

        if (blendingDisabled) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        } else {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
        }

        mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, trianglesInBatch);

        vertexIndex = 0;
        triangleIndex = 0;
    }

    @Override
    public void disableBlending () {
        flush();
        blendingDisabled = true;
    }

    @Override
    public void enableBlending () {
        flush();
        blendingDisabled = false;
    }

    @Override
    public void setBlendFunction (int srcFunc, int dstFunc) {
        if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc) return;
        flush();
        blendSrcFunc = srcFunc;
        blendDstFunc = dstFunc;
    }

    @Override
    public int getBlendSrcFunc () {
        return blendSrcFunc;
    }

    @Override
    public int getBlendDstFunc () {
        return blendDstFunc;
    }

    @Override
    public void dispose () {
        mesh.dispose();
        if (ownsShader && shader != null) shader.dispose();
    }

    @Override
    public Matrix4 getProjectionMatrix () {
        return projectionMatrix;
    }

    @Override
    public Matrix4 getTransformMatrix () {
        return transformMatrix;
    }

    @Override
    public void setProjectionMatrix (Matrix4 projection) {
        if (drawing) flush();
        projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    @Override
    public void setTransformMatrix (Matrix4 transform) {
        if (drawing) flush();
        transformMatrix.set(transform);
        if (drawing) setupMatrices();
    }

    private void setupMatrices () {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        if (customShader != null) {
            customShader.setUniformMatrix("u_projTrans", combinedMatrix);
            customShader.setUniformi("u_texture", 0);
        } else {
            shader.setUniformMatrix("u_projTrans", combinedMatrix);
            shader.setUniformi("u_texture", 0);
        }
    }

    private void switchTexture (Texture texture) {
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
    }

    @Override
    public void setShader (ShaderProgram shader) {
        if (drawing) {
            flush();
            if (customShader != null)
                customShader.end();
            else
                this.shader.end();
        }
        customShader = shader;
        if (drawing) {
            if (customShader != null)
                customShader.begin();
            else
                this.shader.begin();
            setupMatrices();
        }
    }

    @Override
    public ShaderProgram getShader () {
        if (customShader == null) {
            return shader;
        }
        return customShader;
    }

    @Override
    public boolean isBlendingEnabled () {
        return !blendingDisabled;
    }

    @Override
    public boolean isDrawing () {
        return drawing;
    }
}
