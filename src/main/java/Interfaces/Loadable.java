package Interfaces;

import java.io.FileNotFoundException;
public interface Loadable {
    void load(final String path) throws FileNotFoundException;
}
