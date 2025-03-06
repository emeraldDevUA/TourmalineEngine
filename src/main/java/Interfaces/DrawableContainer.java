package Interfaces;

public interface DrawableContainer<T, B, C> {

    void drawSkyBox();
    void drawItems();
    void drawWater();

    void drawEffects();
    void clear();
    void clearSkyBox();
    void addDrawItem(T item);
    void addEffect(B item);
    void addLiquidBody(C item);

    Boolean deleteItem(T item);



}
