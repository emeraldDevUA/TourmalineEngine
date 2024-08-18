package ResourceImpl;

import Interfaces.Loadable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joml.Vector3f;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("unused")

@AllArgsConstructor
@NoArgsConstructor
public class EnhancedMTL implements Loadable {
    public Vector3f diffuse;
    public Vector3f specular;
    public float opacity;
    public float translucency;
    public float roughness;
    public float metallic;
    public float sheen;
    public float emissive;
    public float clearcoat_thickness;
    public float clearcoat_roughness;
    public static final Map<String, EnhancedMTL> mat_libs = new HashMap<>();
    @Override
    public void load(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream bis = new BufferedInputStream(fis);
        String mat_name = "NULL";
        try (Scanner sc = new Scanner(bis)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // empty or commented out
                if (line.isBlank() || line.trim().charAt(0) == '#') {
                    continue;
                } else if (line.contains("newmtl")) {
                    mat_name = line.split(" ")[1];
                } else {
                    line = line.trim();
                    String tag = line.split(" ")[0];



                    mat_libs.put(mat_name, new EnhancedMTL());
                }

            }
        }
    }
}
