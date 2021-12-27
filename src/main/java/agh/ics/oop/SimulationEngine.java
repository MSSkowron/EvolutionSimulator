package agh.ics.oop;

import agh.ics.oop.gui.App;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class SimulationEngine implements IEngine,Runnable {
    private final JungleMap map;
    private App observer;
    private final GridPane gridPane;
    private final VBox dataBox;
    private boolean stopped;
    private final int moveDelay;

    public SimulationEngine(JungleMap map, GridPane gridPane, VBox gridData, int moveDelay) throws IllegalArgumentException{
        this.map = map;
        this.gridPane=gridPane;
        this.dataBox=gridData;
        this.stopped = false;
        this.moveDelay=moveDelay;
    }

    public void startOrResume(){
        this.stopped = !this.stopped;
    }

    public void setObserver(App observer){
        this.observer = observer;
    }

    public void observerUpdate(JungleMap map) throws InterruptedException {
        this.observer.update(map,gridPane,dataBox);
    }

    @Override
    public void run() {
        boolean flag = true;
        while (flag){
            if(!this.stopped){
                //Jeśli na mapie nie ma już żywych zwierząt to kończę pętle.
                if(map.removeDeadAnimals()){
                    flag = false;
                }
                map.magicalTry();
                map.randomMovesAnimals();
                map.dailyEnergyCost();
                map.eat();
                map.copulation();
                map.randomGrass();
                try {
                    observerUpdate(this.map);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    Thread.sleep(moveDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
