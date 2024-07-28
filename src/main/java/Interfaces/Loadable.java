package Interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Loadable {
    void load(final String path) throws FileNotFoundException, IOException;
}
