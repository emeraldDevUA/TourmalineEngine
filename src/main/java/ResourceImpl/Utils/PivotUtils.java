package ResourceImpl.Utils;

import org.joml.Vector3f;

import java.util.List;

public class PivotUtils {


    public static Vector3f geoCenterPivot(List<Vector3f> vertices){

        Vector3f finalVec = new Vector3f(0);
        vertices.forEach(finalVec::add);

        return finalVec.div(vertices.size());
    }

    public static Vector3f xPivot(List<Vector3f> vertices, boolean findMax){


        return new Vector3f(0);
    }
    public static Vector3f yPivot(List<Vector3f> vertices, boolean findMax){


        return new Vector3f(0);
    }

    public static Vector3f zPivot(List<Vector3f> vertices, boolean findMax){


        return new Vector3f(0);
    }

}

