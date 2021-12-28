package agh.ics.oop;

import javafx.scene.paint.Color;

public class Grass implements IMapElement{
    private final Vector2d position;

    public Grass(Vector2d position){
        this.position = position;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public void move(MoveDirection moveDirection){
    }

    @Override
    public void  addObserver(IPositionChangeObserver observer){
    }

    @Override
    public void removeObserver(IPositionChangeObserver observer){
    }

    @Override
    public boolean hasGenes() {
        return false;
    }

    @Override
    public String getGenesString() {
        return "";
    }

    @Override
    public String toString() {
        return "*";
    }

    @Override
    public Color toColor() {
        return Color.GREEN;
    }
}
