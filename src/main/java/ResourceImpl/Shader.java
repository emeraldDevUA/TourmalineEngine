package ResourceImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;

@SuppressWarnings("unused")
public class Shader implements Closeable {
    public static final int  POSITION_LOCATION = 0;
    public static final int    NORMAL_LOCATION = 1;
    public static final int       UVS_LOCATION = 2;
    public static final int MODEL_BLOCK = 0;
    public static final int CAMERA_BLOCK = 1;
    public static final int MATERIAL_BLOCK = 2;
    public static final int ALBEDO_MAP_BINDING = 4;
    public static final int METALNESS_MAP_BINDING = 5;
    public static final int ROUGHNESS_MAP_BINDING = 6;
    public static final int NORMAL_MAP_BINDING = 7;
    public static final int EMISSION_MAP_BINDING = 8;
    public static final int AMBIENT_OCCLUSION_MAP_BINDING = 9;
    public static final int SHADOW_MAP_BINDING = 10;

    @Getter
    private final int program;

    public Shader(String vertexPath, String fragmentPath)
    {
        String vertexSource = "";
        String fragmentSource = "";

        try {
            Path vPath = Paths.get(vertexPath);

            vertexSource = new String(Files.readAllBytes(vPath));
            fragmentSource = new String(Files.readAllBytes(Paths.get(fragmentPath)));

            Preprocessor preprocessor = new Preprocessor(vPath.getParent().toString());
            fragmentSource = preprocessor.processIncludeFiles(fragmentSource);
            vertexSource = preprocessor.processIncludeFiles(vertexSource);


        } catch (IOException e) {
            System.err.println("Shader reading failed");
            System.err.println(e.getMessage());
        }



        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        if (!glGetShaderInfoLog(vertexShader).isEmpty())
            System.err.println(STR."\{vertexPath}: \{glGetShaderInfoLog(vertexShader)}");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        if (!glGetShaderInfoLog(fragmentShader).isEmpty())
            System.err.println(STR."\{fragmentPath}: \{glGetShaderInfoLog(fragmentShader)}");

        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);
        if (!glGetProgramInfoLog(program).isEmpty())
            System.err.println(glGetProgramInfoLog(program));

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void use() {

        glUseProgram(program);
    }

    public void unbind(){
        glUseProgram(0);
    }
    @Override
    public void close() {

        glDeleteShader(program);
    }


    @RequiredArgsConstructor
    private static class Preprocessor {
        private final String currentPath;

        // Loads the file specified in the #include statement
        private String loadFile(final String includeStatement) {
            String pathToHeaderFile = "";
            String headerFileName = includeStatement.replace("#include", "")
                    .replaceAll("[<>]", "")
                    .trim();

            // Define path based on brackets or quotes
            if (includeStatement.contains("<")) {
                pathToHeaderFile = STR."src/main/glsl/\{headerFileName}";
            } else if (includeStatement.contains("\"")) {
                pathToHeaderFile = currentPath + headerFileName;
            }

            // Read file content
            try {
                return new String(Files.readAllBytes(Paths.get(pathToHeaderFile)));
            } catch (IOException e) {
                System.err.println(STR."Error loading file: \{pathToHeaderFile}");
                System.err.println(e.getMessage());
                return "";  // Return empty string on failure to avoid null issues
            }
        }

        // Replaces the #include statement with the loaded shader code
        private String insertShaderCode(String source, String includeStatement) {
            return source.replace(includeStatement, loadFile(includeStatement));
        }

        // Main processing method to replace all #include statements in the shader code
        public String processIncludeFiles(String shaderCode) {
            while (shaderCode.contains("#include")) {
                int index1 = shaderCode.indexOf("#include");
                int index2 = shaderCode.indexOf(">", index1);  // Find closing '>'

                if (index2 == -1) {  // Invalid syntax; '>' not found
                    System.err.println("Error: Invalid #include syntax.");
                    break;
                }

                String includeStatement = shaderCode.substring(index1, index2 + 1);
                shaderCode = insertShaderCode(shaderCode, includeStatement);
            }

            return shaderCode;
        }
    }

}
