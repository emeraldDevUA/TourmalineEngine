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
        Vector3f firstVertex = vertices.getFirst();
        if(findMax) {
            for (Vector3f vertex : vertices) {
                if (firstVertex.x < vertex.x) {
                    firstVertex = vertex;
                }
            }
        }
        else {
            for (Vector3f vertex : vertices) {
                if (firstVertex.x > vertex.x) {
                    firstVertex = vertex;
                }
            }
        }

        return firstVertex;
    }
    public static Vector3f yPivot(List<Vector3f> vertices, boolean findMax){

        Vector3f firstVertex = vertices.getFirst();
        if(findMax) {
            for (Vector3f vertex : vertices) {
                if (firstVertex.y < vertex.y) {
                    firstVertex = vertex;
                }
            }
        }
        else {
            for (Vector3f vertex : vertices) {
                if (firstVertex.y > vertex.y) {
                    firstVertex = vertex;
                }
            }
        }

        return firstVertex;
    }

    public static Vector3f zPivot(List<Vector3f> vertices, boolean findMax){

        Vector3f firstVertex = vertices.getFirst();
        if(findMax) {
            for (Vector3f vertex : vertices) {
                if (firstVertex.z < vertex.z) {
                    firstVertex = vertex;
                }
            }
        }
        else {
            for (Vector3f vertex : vertices) {
                if (firstVertex.z > vertex.z) {
                    firstVertex = vertex;
                }
            }
        }

        return firstVertex;
    }

}

