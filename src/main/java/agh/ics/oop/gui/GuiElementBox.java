package agh.ics.oop.gui;
import agh.ics.oop.IMapElement;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class GuiElementBox {
    private final IMapElement element;
    private final double mapWidth;
    private final double mapHeight;


    public GuiElementBox(IMapElement element,double width,double height){
        this.element=element;
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public VBox getBox(){
        VBox box = new VBox();
        Circle circle = new Circle();
        circle.setCenterX(1.0f);
        circle.setCenterY(1.0f);
        circle.setRadius(Math.min(mapWidth,mapHeight)/3);
        circle.setFill(element.toColor());
        if(element.hasGenes()){
            circle.setOnMouseClicked(event -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Animal genome");
                a.setHeaderText("Animal genome is shown below.");
                a.setWidth(200);
                String genome = element.getGenesString();
                a.setContentText(genome);
                a.show();
            });
        }
        box.getChildren().add(circle);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
