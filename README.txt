CMT107 VISUAL COMPUTING COURSEWORK

--Compilation--

This project is recommended to be compiled with JDK version 14 and newer (because it contains new Java expressions such as new switch case syntax). Originally JDK 17.0.3 was used.
This project uses Maven as a build automation tool, so make sure you pressed Load Maven Changes button on the top right corner (Ctrl + Shift + O), as well as pressed Reload All Maven Projects before the compilation.
All the libraries and their versions used in this project could be found in the pom.xml file.


--Implemented tasks--

Task 1 (VCCW01, STeapot): the interactive model-view transformations with keyboard input were implemented. Those transformations are:
• on pressing the key 'M', the teapot expands;
• on pressing the key 'N', the teapot shrinks;
• on pressing the Left arrow key, the teapot moves left;
• on pressing the Right arrow key, the teapot moves right;
• on pressing the Up arrow key, the teapot moves up;
• on pressing the Down arrow key, the teapot moves down;
• on pressing the key 'X', the teapot rotates around the x-axis clockwise;
• on pressing the key 'C', the teapot rotates around the x-axis anti-clockwise;
• on pressing the key 'Y', the teapot rotates around the y-axis clockwise;
• on pressing the key 'U', the teapot rotates around the y-axis anti-clockwise.
*The initial program contained the rendering of a teapot and the key 'M' transformation.

Task 2 (VCCW02, SSphere, SCone): the 3D polygonal model was built and the scene including two objects (sphere and cone) with different material properties was rendered.
• new class SCone (which builds a cone model) extended from SObject was created;
• a cone was drawn and its material was set to turquoise according to the picture in the task;
• the sphere and cone were transformed to the appropriate positions according to the picture in the task.
SCone file is located in the package Objects.
*The initial program contained the rendering of a sphere.

Task 3 (VCCW03, SCube, Texture.vert, Texture.frag): the texture mapping onto a 3D cube was performed.
• new class SCube (which builds a cube model) extended from SObject was created;
• vertices, normals, indices and texture coordinates of a cube were defined;
• a cube was drawn and its material was set to pearl (for the texture to be better displayed);
• the given texture is applied to all six sides of the cube;
• vertex and fragment shaders to implement blending of texture and Gouraud shading were created.
SCube file is located in the package Objects; Texture.frag and Texture.vert are located in the project current directory.