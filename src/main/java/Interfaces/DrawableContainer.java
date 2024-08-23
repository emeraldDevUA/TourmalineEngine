package Interfaces;

public interface DrawableContainer<T> {

    void drawSkyBox();
    void drawItems();
    void clear();
    void clearSkyBox();
    void addDrawItem(T item);

    Boolean deleteItem(T item);



}
