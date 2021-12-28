package agh.ics.oop;

import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Animal implements IMapElement{
    private MapDirection direction;
    private Vector2d position;
    private final IWorldMap map;
    private int energy;
    private final int startEnergy;
    private final Genes genes;
    private final HashSet<IPositionChangeObserver> observers = new HashSet<>();
    private int age;
    private int numberOfChildren;

    //LOSOWO POWSTALE ZWIERZE
    public Animal(IWorldMap map,Vector2d initialPosition,int energy){
        this.map=map;
        this.direction=randomDirection();
        this.genes=new Genes();
        this.position=initialPosition;
        this.energy=energy;
        this.startEnergy=energy;
        this.age=0;
        this.numberOfChildren=0;
    }

    //ZWIERZE POWSTALE Z ROZMNOZENIA.
    public Animal(IWorldMap map,Vector2d initialPosition,int energy,Genes genes){
        this.map=map;
        this.direction=randomDirection();
        this.genes=genes;
        this.position=initialPosition;
        this.energy=energy;
        this.startEnergy=energy;
        this.age=0;
        this.numberOfChildren=0;
    }

    private MapDirection randomDirection(){
        int result = (int) (Math.random() * 8);
        return switch (result) {
            case 0 -> MapDirection.NORTH;
            case 1 -> MapDirection.SOUTH;
            case 2 -> MapDirection.EAST;
            case 3 -> MapDirection.WEST;
            case 4 -> MapDirection.NORTHWEST;
            case 5 -> MapDirection.NORTHEAST;
            case 6 -> MapDirection.SOUTHWEST;
            default -> MapDirection.SOUTHEAST;
        };
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, position, map, energy, startEnergy, genes, observers, age, numberOfChildren);
    }

    @Override
    public void move(MoveDirection step) {
        switch (step) {
            case LEFT -> direction=direction.previous();
            case RIGHT -> direction=direction.next();
            case BACKWARD  -> moveForwardBackward(this.position.subtract(this.direction.toUnitVector()));
            case FORWARD -> moveForwardBackward(this.position.add(this.direction.toUnitVector()));
        }
    }

    private void moveForwardBackward(Vector2d vector){
        if(this.map.canMoveTo(vector)) {
            Vector2d oldPosition = this.position;
            this.position = vector;
            positionChanged(oldPosition,vector,this);
        }
    }

    public void randomMove() {
        int random = genes.getRandom();
        if(random == 0){
            this.move(MoveDirection.FORWARD);
        }
        else if(random == 4){
            this.move(MoveDirection.BACKWARD);
        }
        else {
            for(int i = 0; i<random; i++) {
                this.move(MoveDirection.RIGHT);
            }
        }
    }

    public boolean isAlive(){
        return this.energy > 0;
    }

    public void changeEnergy(int value){
        this.energy = this.energy + value;
    }

    public void daySurvived(){
        age += 1;
    }

    public void newChild(){
        numberOfChildren += 1;
    }

    //GETTERS/SETTERS
    public int getAge(){
        return age;
    }

    public int getNumberOfChildren(){
        return numberOfChildren;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    public void setPosition(Vector2d position){
        this.position=position;
    }

    public int getEnergy(){
        return this.energy;
    }

    public Genes getGenes(){
        return this.genes;
    }

    public String getGenesString(){
        return Arrays.toString(genes.getArray());
    }

    public boolean hasGenes(){
        return true;
    }

    //DISPLAY
    @Override
    public String toString() {
        return direction.toString();
    }

    @Override
    public Color toColor() {
        if(energy >= 0.5*startEnergy){
            return Color.BLUE;
        }
        if(energy >= 0.3*startEnergy){
            return Color.LIGHTBLUE;
        }
        if(energy >= 0.2*startEnergy){
            return Color.LIGHTGRAY;
        }
        if(energy >= 0.1*startEnergy){
            return Color.GRAY;
        }
        return Color.BLACK;
    }

    //OBSERVER
    @Override
    public void addObserver(IPositionChangeObserver observer){
        observers.add(observer);
    }

    @Override
    public void removeObserver(IPositionChangeObserver observer){
        observers.remove(observer);
    }

    private void positionChanged(Vector2d oldPosition,Vector2d newPosition,Animal animal){
        for(IPositionChangeObserver observer : observers){
            observer.positionChanged(oldPosition,newPosition,this);
        }
    }
}

