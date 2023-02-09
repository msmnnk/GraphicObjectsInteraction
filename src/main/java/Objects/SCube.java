package Objects;

// Task 3: create an SCube class extended from SObject
public class SCube extends SObject {
    private float side;

    public SCube() {
        super();
        init();
        update();
    }

    public SCube(float side) {
        super();
        this.side = side;
        update();
    }

    private void init() {
        this.side = 1;
    }

    @Override
    protected void genData() {
        numVertices = 6;
        // multiply by 2 triangles and 3 points of each triangle
        numIndices = numVertices * 2 * 3;
        /*
         *    v6-------- v5
         *   /|         /|
         *  v1---------v0|
         *  | |        | |
         *  | |v7------|-|v4
         *  |/         |/
         *  v2---------v3
        */

        // Task 3: define normals
        normals = new float[] {
                // front: v0, v1, v2, v3
                0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
                // right: v0, v3, v4, v5
                1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
                // top: v0, v5, v6, v1
                0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
                // left: v1, v6, v7, v2
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
                // bottom: v7, v4, v3, v2
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
                // back: v4, v7, v6, v5
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1
        };

        // Task 3: define vertices
        vertices = new float[] {
                // front: v0, v1, v2, v3
                1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,
                // right: v0, v3, v4, v5
                1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,
                // top: v0, v5, v6, v1
                1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,
                // left: v1, v6, v7, v2
                -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1,
                // bottom: v7, v4, v3, v2
                -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1,
                // back: v4, v7, v6, v5
                1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1
        };
        for (int i = 0; i < vertices.length; ++i) {
            vertices[i] *= side;
        }

        // Task 3: define texture coordinates
        textures = new float[] {
                // front
                1, 1,
                0, 1,
                0, 0,
                1, 0,
                // right
                0, 1,
                0, 0,
                1, 0,
                1, 1,
                // top
                1, 0,
                1, 1,
                0, 1,
                0, 0,
                // left
                1, 1,
                0, 1,
                0, 0,
                1, 0,
                // bottom
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                // back
                1, 0,
                0, 0,
                0, 1,
                1, 1
        };

        // Task 3: define indices
        indices = new int[] {
                // front
                0, 1, 2,
                2, 3, 0,
                // right
                4, 5, 6,
                6, 7, 4,
                // top
                8, 9, 10,
                10, 11, 8,
                // left
                12, 13, 14,
                14, 15, 12,
                // bottom
                16, 17, 18,
                18, 19, 16,
                // back
                20, 21, 22,
                22, 23, 20
        };
    }

    public void setSide(float side) {
        this.side = side;
        updated = false;
    }

    public float getSide() {
        return side;
    }

}
