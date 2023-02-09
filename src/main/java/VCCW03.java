import static com.jogamp.opengl.GL3.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import Basic.ShaderProg;
import Basic.Transform;
import Basic.Vec4;
import Objects.SCube;
import Objects.SObject;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

import javax.imageio.ImageIO;

/*
 * This file contains small code fragments
 * from the initial file of lab 5 (VC05.java)
 */

public class VCCW03 {
    final GLWindow window; // define a window
    final FPSAnimator animator = new FPSAnimator(60, true);
    final Renderer renderer = new Renderer();

    public VCCW03() {
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        window = GLWindow.create(caps);
        window.addGLEventListener(renderer); // set the window to listen GLEvents
        animator.add(window);
        window.setTitle("Coursework 3"); // set window title
        window.setSize(500, 500); // set window size
        window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        window.setVisible(true);
        animator.start();
    }

    public static void main(String[] args) {
        new VCCW03();
    }

    class Renderer implements GLEventListener {
        private Transform T = new Transform(); // model_view transformation

        // VAOs and VBOs parameters
        private int idPoint = 0, numVAOs = 2;
        private int idBuffer = 0, numVBOs = 2;
        private int idElement = 0, numEBOs = 2;
        private int[] VAOs = new int[numVAOs];
        private int[] VBOs = new int[numVBOs];
        private int[] EBOs = new int[numEBOs];

        // model parameters
        private int[] numElements = new int[numEBOs];
        private long vertexSize;
        private long normalSize;
        private long texSize;
        private int vPosition;
        private int vNormal;
        private int vTexArray;

        // transformation parameters
        private int ModelView;
        private int NormalTransform;
        private int Projection;

        // texture parameters
        ByteBuffer texImg;
        private int texWidth, texHeight;
        private int texName[] = new int[6];

        // lighting parameters
        private int AmbientProduct;
        private int DiffuseProduct;
        private int SpecularProduct;
        private int Shininess;
        private float[] ambient;
        private float[] diffuse;
        private float[] specular;
        private float materialShininess;

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3(); // get the GL pipeline object
            gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            // transformation for the cube
            T.initialize();
            T.rotateX(160);
            T.rotateY(45);
            T.scale(0.5f, 0.5f, 0.5f);
            T.translate(0.0f, 0.0f, 0.0f);
            // locate camera
            T.lookAt(0, 0, 0, 0, 0, -100, 0, 1, 0); // default
            /*
             * Send the model_view and normal transformation matrices to the shader.
             * Here parameter 'true' for transpose means to convert a row-major
             * matrix to a column-major one, which is required when vertices'
             * location vectors are pre-multiplied by the model_view matrix.
             * The normal transformation matrix is the inverse-transpose
             * matrix of the vertex transformation matrix.
             */
            gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);
            gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0);
            // send other uniform variables to the shader
            gl.glUniform4fv(AmbientProduct, 1, ambient, 0);
            gl.glUniform4fv(DiffuseProduct, 1, diffuse, 0);
            gl.glUniform4fv(SpecularProduct, 1, specular, 0);
            gl.glUniform1f(Shininess, materialShininess);
            // bind and draw the cube
            idPoint = 0;
            idBuffer = 0;
            idElement = 0;
            bindObject(gl);
            gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);
        }

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3(); // get the GL pipeline object
            System.out.print("GL_Version: " + gl.glGetString(GL_VERSION));
            // read in the texture image (should be in the current directory)
            try {
                texImg = readImage("WelshDragon.jpg");
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
            gl.glGenTextures(1, texName, 0); // create texture object
            gl.glBindTexture(GL_TEXTURE_2D, texName[0]);
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight,
                    0, GL_BGR, GL_UNSIGNED_BYTE, texImg); // specify texture image
            // indicate how the texture is to be applied to each pixel
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            // enable texture mapping
            gl.glEnable(GL_DEPTH_TEST);
            gl.glEnable(GL_CULL_FACE);
            // compile and use the shader program
            ShaderProg shaderproc = new ShaderProg(gl, "Texture.vert", "Texture.frag");
            int program = shaderproc.getProgram();
            gl.glUseProgram(program);
            // initialize the vertex position and normal attribute in the vertex shader
            vPosition = gl.glGetAttribLocation(program, "vPosition");
            vNormal = gl.glGetAttribLocation(program, "vNormal");
            vTexArray = gl.glGetAttribLocation(program, "vTexArray");
            // get connected with the ModelView, NormalTransform,
            // and Projection matrices in the vertex shader
            ModelView = gl.glGetUniformLocation(program, "ModelView");
            NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
            Projection = gl.glGetUniformLocation(program, "Projection");
            // get connected with uniform variables AmbientProduct, DiffuseProduct,
            // SpecularProduct, and Shininess in the vertex shader
            AmbientProduct = gl.glGetUniformLocation(program, "AmbientProduct");
            DiffuseProduct = gl.glGetUniformLocation(program, "DiffuseProduct");
            SpecularProduct = gl.glGetUniformLocation(program, "SpecularProduct");
            Shininess = gl.glGetUniformLocation(program, "Shininess");
            // generate VAOs, VBOs, and EBOs
            gl.glGenVertexArrays(numVAOs, VAOs, 0);
            gl.glGenBuffers(numVBOs, VBOs, 0);
            gl.glGenBuffers(numEBOs, EBOs, 0);
            // initialize shader lighting parameters
            float[] lightPosition = {10.0f, 15.0f, 20.0f, 1.0f};
            Vec4 lightAmbient = new Vec4(0.35f, 0.9f, 0.9f, 1.0f);
            Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 lightSpecular = new Vec4(1.0f, 0.5f, 0.5f, 1.0f);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"),
                    1, lightPosition, 0);

            // create a cube
            SObject cube = new SCube(1);
            idPoint = 0;
            idBuffer = 0;
            idElement = 0;
            createObject(gl, cube);
            // set cube material (pearl)
            Vec4 materialAmbient1 = new Vec4(0.25f, 0.20725f, 0.20725f, 0.922f);
            Vec4 materialDiffuse1 = new Vec4(1f, 0.829f, 0.829f, 0.922f);
            Vec4 materialSpecular1 = new Vec4(0.296648f, 0.296648f, 0.296648f, 0.922f);
            materialShininess = 11.264f;
            Vec4 ambientProduct = lightAmbient.times(materialAmbient1);
            ambient = ambientProduct.getVector();
            Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse1);
            diffuse = diffuseProduct.getVector();
            Vec4 specularProduct = lightSpecular.times(materialSpecular1);
            specular = specularProduct.getVector();
            gl.glUniform1i(gl.glGetUniformLocation(program, "tex"), 0);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int w,
                            int h) {
            GL3 gl = drawable.getGL().getGL3(); // get the GL pipeline object
            gl.glViewport(x, y, w, h);
            T.initialize();
            // projection
            if (h < 1) {
                h = 1;
            }
            if (w < 1) {
                w = 1;
            }
            float a = (float) w / h; // aspect
            if (w < h) {
                T.ortho(-1, 1, -1 / a, 1 / a, -1, 1);
            } else {
                T.ortho(-1 * a, 1 * a, -1, 1, -1, 1);
            }
            // convert right-hand to left-hand coordinate system
            T.reverseZ();
            gl.glUniformMatrix4fv(Projection, 1, true, T.getTransformv(), 0);
        }

        // close the window
        @Override
        public void dispose(GLAutoDrawable drawable) {
            System.exit(0);
        }

        public void createObject(GL3 gl, SObject obj) {
            float[] vertexArray = obj.getVertices();
            float[] normalArray = obj.getNormals();
            int[] vertexIndexs = obj.getIndices();
            float[] texArray = obj.getTextures();
            numElements[idElement] = obj.getNumIndices();
            bindObject(gl);
            FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
            FloatBuffer normals = FloatBuffer.wrap(normalArray);
            FloatBuffer textures = FloatBuffer.wrap(texArray);
            // create an empty buffer with the size needed
            // and a null pointer for the data values
            vertexSize = vertexArray.length * (Float.SIZE / 8);
            normalSize = normalArray.length * (Float.SIZE / 8);
            texSize = texArray.length * (Float.SIZE / 8);
            gl.glBufferData(GL_ARRAY_BUFFER, vertexSize + normalSize + texSize, null, GL_STATIC_DRAW);
            // load the real data separately, put the colors right after the vertex coordinates,
            // so the offset for colors is the size of vertices in bytes
            gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexSize, vertices);
            gl.glBufferSubData(GL_ARRAY_BUFFER, vertexSize, normalSize, normals);
            gl.glBufferSubData(GL_ARRAY_BUFFER, vertexSize + normalSize, texSize, textures);
            IntBuffer elements = IntBuffer.wrap(vertexIndexs);
            long indexSize = vertexIndexs.length * (Integer.SIZE / 8);
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, elements, GL_STATIC_DRAW);
            gl.glEnableVertexAttribArray(vPosition);
            gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
            gl.glEnableVertexAttribArray(vNormal);
            gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
            gl.glEnableVertexAttribArray(vTexArray);
            gl.glVertexAttribPointer(vTexArray, 2, GL_FLOAT, false, 0, vertexSize + normalSize);
        }

        public void bindObject(GL3 gl) {
            gl.glBindVertexArray(VAOs[idPoint]);
            gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);
        }

        private ByteBuffer readImage(String filename) throws IOException {
            ByteBuffer imgbuf;
            // read the texture image
            BufferedImage img = ImageIO.read(new FileInputStream(filename));
            texWidth = img.getWidth();
            texHeight = img.getHeight();
            DataBufferByte datbuf = (DataBufferByte) img.getData().getDataBuffer();
            imgbuf = ByteBuffer.wrap(datbuf.getData());
            return imgbuf;
        }
    }

}
