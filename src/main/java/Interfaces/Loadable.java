package Interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Loadable {

    /**
     * Asynchronous loading method. It significantly speeds up the loading process.
     */
    void load(final String path) throws FileNotFoundException, IOException;
}
