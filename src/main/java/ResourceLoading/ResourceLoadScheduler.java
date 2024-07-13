package ResourceLoading;

import Interfaces.Loadable;

import java.util.ArrayList;
import java.util.List;

public class ResourceLoadScheduler {
    public static final int coreNumber = 8;
    // 4 on models, 3 on textures, 1 for any other data
    private final List<ResourceLoader> workingThreads;

    public ResourceLoadScheduler(){

        workingThreads = new ArrayList<>(coreNumber);

    }

    public void addResource(Loadable l, String path){


    }

    public void loadResources(){
        workingThreads.forEach(ResourceLoader::start);
    }

}
