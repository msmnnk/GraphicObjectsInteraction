import static com.jogamp.opengl.GL3.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import Basic.ShaderProg;
import Basic.Transform;
import Basic.Vec4;
import Objects.STeapot;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

public class VCCW01 {
    final GLWindow window; // define a window
    final FPSAnimator animator = new FPSAnimator(60, true);
    final Renderer renderer = new Renderer();

    public VCCW01() {
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        window = GLWindow.create(caps);
        window.addGLEventListener(renderer); // set the window to listen GLEvents
        window.addKeyListener(renderer); // set the window to listen key events
        animator.add(window);
        window.setTitle("Coursework 1"); // set window title
        window.setSize(500, 500); // set window size
        window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        window.setVisible(true);
        animator.start();
    }

    public static void main(String[] args) {
        new VCCW01();
    }

    class Renderer implements GLEventListener, KeyListener {
        private Transform T = new Transform(); // model_view transformation

        // VAOs and VBOs parameters
        private int idPoint = 0, numVAOs = 1;
        private int idBuffer = 0, numVBOs = 1;
        private int idElement = 0, numEBOs = 1;
        private int[] VAOs = new int[numVAOs];
        private int[] VBOs = new int[numVBOs];
        private int[] EBOs = new int[numEBOs];

        // model parameters
        private int numElements;
        private int vPosition;
        private int vNormal;

        // TASK 1a: transformation parameters
        private int ModelView;
        private int Projection;
        private int NormalTransform;
        private float scale = 1;
        private float translateX = 0;
        private float translateY = 0;
        private float translateZ = 0;
        private float rotateX = 0;
        private float rotateY = 0;

        @Override
        public void display(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3(); // get the GL pipeline object
            gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            gl.glPointSize(5);
            gl.glLineWidth(5);
            T.initialize();
            // TASK 1c: key control interaction,
            // perform the transformations
            T.scale(scale, scale, scale);
            T.translate(translateX, translateY, translateZ);
            T.rotateX(rotateX);
            T.rotateY(rotateY);
            // locate camera
            T.lookAt(0, 0, 0, 0, 0, -1, 0, 1, 0); // default
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
            gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // default
            gl.glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_INT, 0); // for solid teapot
        }

        // close the window
        @Override
        public void dispose(GLAutoDrawable drawable) {
            System.exit(0);
        }

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3(); // get the GL pipeline object
            gl.glEnable(GL_PRIMITIVE_RESTART);
            gl.glPrimitiveRestartIndex(0xFFFF);
            gl.glEnable(GL_CULL_FACE);
            // create a teapot
            STeapot teapot = new STeapot(2);
            float[] vertexArray = teapot.getVertices();
            float[] normalArray = teapot.getNormals();
            int[] vertexIndexs = teapot.getIndices();
            numElements = teapot.getNumIndices();
            gl.glGenVertexArrays(numVAOs, VAOs, 0);
            gl.glBindVertexArray(VAOs[idPoint]);
            FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
            FloatBuffer normals = FloatBuffer.wrap(normalArray);
            gl.glGenBuffers(numVBOs, VBOs, 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);

            // create an empty buffer with the size needed
            // and a null pointer for the data values
            long vertexSize = vertexArray.length * (Float.SIZE / 8);
            long normalSize = normalArray.length * (Float.SIZE / 8);
            gl.glBufferData(GL_ARRAY_BUFFER, vertexSize + normalSize, null, GL_STATIC_DRAW);
            // load the real data separately,
            // put the colors right after the vertex coordinates,
            // the offset for colors is the size of vertices in bytes
            gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexSize, vertices);
            gl.glBufferSubData(GL_ARRAY_BUFFER, vertexSize, normalSize, normals);
            IntBuffer elements = IntBuffer.wrap(vertexIndexs);
            gl.glGenBuffers(numEBOs, EBOs, 0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);
            long indexSize = vertexIndexs.length * (Integer.SIZE / 8);
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, elements, GL_STATIC_DRAW);

            // compile and use the shader program
            ShaderProg shaderproc = new ShaderProg(gl, "Gouraud.vert", "Gouraud.frag");
            int program = shaderproc.getProgram();
            gl.glUseProgram(program);
            // initialize the vertex position attribute in the vertex shader
            vPosition = gl.glGetAttribLocation(program, "vPosition");
            gl.glEnableVertexAttribArray(vPosition);
            gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
            // initialize the vertex color attribute in the vertex shader,
            // the offset is the same as in the glBufferSubData, i.e. vertexSize
            // and is the starting point of the color data
            vNormal = gl.glGetAttribLocation(program, "vNormal");
            gl.glEnableVertexAttribArray(vNormal);
            gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
            // get connected with the ModelView matrix in the vertex shader
            ModelView = gl.glGetUniformLocation(program, "ModelView");
            NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
            Projection = gl.glGetUniformLocation(program, "Projection");

            // initialize shader lighting parameters
            float[] lightPosition = {10.0f, 10.0f, -10.0f, 0.0f};
            Vec4 lightAmbient = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            // set teapot material (brass)
            Vec4 materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
            Vec4 materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
            Vec4 materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
            float materialShininess = 27.8974f;
            Vec4 ambientProduct = lightAmbient.times(materialAmbient);
            float[] ambient = ambientProduct.getVector();
            Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse);
            float[] diffuse = diffuseProduct.getVector();
            Vec4 specularProduct = lightSpecular.times(materialSpecular);
            float[] specular = specularProduct.getVector();
            gl.glUniform4fv(gl.glGetUniformLocation(program, "AmbientProduct"), 1, ambient, 0);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "DiffuseProduct"), 1, diffuse, 0);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "SpecularProduct"), 1, specular, 0);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"), 1, lightPosition, 0);
            gl.glUniform1f(gl.glGetUniformLocation(program, "Shininess"), materialShininess);

            // this is necessary, otherwise, the color on back surface may display
            gl.glEnable(GL_DEPTH_TEST);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
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

        @Override
        public void keyPressed(KeyEvent ke) {
            int keyEvent = ke.getKeyCode();
            // TASK 1b: response to different key events
            switch (keyEvent) {
                // destroy the window when the key 'ESC' is pressed
                case KeyEvent.VK_ESCAPE -> window.destroy();
                // scale up the object when the key 'M' is pressed
                case KeyEvent.VK_M -> scale *= 1.1;
                // scale down the object when the key 'N' is pressed
                case KeyEvent.VK_N -> scale /= 1.1;
                // move the object left when the Left arrow key is pressed
                case KeyEvent.VK_LEFT -> translateX -= 0.1;
                // move the object right when the Right arrow key is pressed
                case KeyEvent.VK_RIGHT -> translateX += 0.1;
                // move the object up when the Up arrow key is pressed
                case KeyEvent.VK_UP -> translateY += 0.1;
                // move the object down when the down arrow key is pressed
                case KeyEvent.VK_DOWN -> translateY -= 0.1;
                // rotate the object around X-axis clockwise when the key 'X' is pressed
                case KeyEvent.VK_X -> rotateX += 10;
                // rotate the object around X-axis anti-clockwise when the key 'C' is pressed
                case KeyEvent.VK_C -> rotateX -= 10;
                // rotate the object around Y-axis clockwise when the key 'Y' is pressed
                case KeyEvent.VK_Y -> rotateY += 10;
                // rotate the object around Y-axis anti-clockwise when the key 'U' is pressed
                case KeyEvent.VK_U -> rotateY -= 10;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // TODO Auto-generated method stub
        }
    }

}
