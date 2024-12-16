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

//d/map_Kd (base/diffuse) // reuse
//Ks/map_Ks (specular) // reuse
//d or Tr (opacity) // reuse
//map_d/map_Tr (opacitymap) // reuse UNSUPPORTED
//Tf (translucency) // reuse
//bump/bm (bump map) // reuse
//disp (displacement map) // reuse UNSUPPORTED
//Pr/map_Pr (roughness) // new
//Pm/map_Pm (metallic) // new
//Ps/map_Ps (sheen) // new
//Pc (clearcoat thickness) // new
//Pcr (clearcoat roughness) // new
//Ke/map_Ke (emissive) // new
//aniso (anisotropy) // new UNSUPPORTED
//anisor (anisotropy rotation) // new  UNSUPPORTED
//norm (normal map) // new UNSUPPORTED

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

                    String[] split_values = line.split(" ");
                    String tag = split_values[0];
                    Float[] arguments = new Float[3];

                    try {
                        for(int i = 1; i < line.split(" ").length; i++){
                            arguments[i-1] = Float.parseFloat(split_values[i]);
                        }
                    }catch (NullPointerException|NumberFormatException|ArrayIndexOutOfBoundsException e){
                        System.err.println(e.getMessage());
                    }
                    setValues(tag, arguments);

                    mat_libs.put(mat_name, new EnhancedMTL());
                }

            }
        }
    }

    private void setValues(String tag, Float[] arguments) {
        switch (tag){
            case "Kd":
                diffuse.x = arguments[0];
                diffuse.y = arguments[1];
                diffuse.z = arguments[2];
            break;

            case "Ks":
                specular.x = arguments[0];
                specular.y = arguments[1];
                specular.z = arguments[2];break;
            case "d", "Tr":
                opacity = arguments[0];
            break;

            case "Pr": break;
            case "Pm": break;
            case "Ps": break;
            case "Pc": break;
            case "Pcr":break;
        }
    }
}
