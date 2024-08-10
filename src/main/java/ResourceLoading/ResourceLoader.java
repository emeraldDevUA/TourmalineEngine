package ResourceLoading;

import Interfaces.Loadable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ResourceLoader extends Thread{

    private final List<Loadable> tasks;

    private final List<String> paths;

    @Override
    public void run() {
        try {
            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).load(paths.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void placeLoadable(Loadable loadable, String path){
        tasks.add(loadable);
        paths.add(path);
    }
}
