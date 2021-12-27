package agh.ics.oop;

import javafx.scene.paint.Color;

public interface IMapElement {
    Vector2d getPosition();

    void move(MoveDirection moveDirection);

    void addObserver(IPositionChangeObserver observer);

    void removeObserver(IPositionChangeObserver observer);

    boolean hasGenome();

    String getGenomeString();

    @Override
    String toString();

    Color toColor();
}
