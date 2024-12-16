package ResourceLoading;

import Interfaces.Loadable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ResourceLoadScheduler {

    public static final int coreNumber = 8;
    // 4 on models, 3 on textures, 1 for any other data
    private final List<ResourceLoader> workingThreads;
    private int model_counter, texture_counter;

    public ResourceLoadScheduler(){
        workingThreads = new ArrayList<>(coreNumber);
        for(int i = 0; i < coreNumber; i++){
            workingThreads.add(i, new ResourceLoader(new ArrayList<>(), new ArrayList<>()));
        }
    }

    public void addResource(Loadable loadable, @NotNull String path){
        final String models_formats = ".obj .fbx .glb .stl";
        final String texture_formats = ".png .jpg";

        String format =  path.split("\\.")[1];
        if(models_formats.contains(format)){
            if(model_counter == coreNumber/2){model_counter = 0;}
            workingThreads.get(model_counter++).placeLoadable(loadable, path);
        }else if(texture_formats.contains(format)){
            if(texture_counter == coreNumber/2-1){texture_counter = 0;}
            workingThreads.get(4+texture_counter++).placeLoadable(loadable, path);
        }
        else {
            workingThreads.get(coreNumber-1).placeLoadable(loadable, path);
        }


    }


    public void loadResources(){
        workingThreads.forEach(ResourceLoader::start);
    }

    public void reset(){
        workingThreads.clear();
        for(int i = 0; i < coreNumber; i++){
            workingThreads.add(i, new ResourceLoader(new ArrayList<>(), new ArrayList<>()));
        }
    }
    /**
     * Returns a float value that represents the amount of completed work.
     * n = N(workingThreads)/N(allThreads)
     */
    public float getReadiness(){
        int alive = 0;
        for(int i = 0; i < coreNumber; i ++){
            if(workingThreads.get(i).isAlive()){ alive++;}
        }

        return ((float) (coreNumber-alive)/ (float) (coreNumber));
    }
}
