package agh.ics.oop.gui;
import agh.ics.oop.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class App extends Application {
    private int moveDelay;

    public void update(JungleMap updatedMap, GridPane gridPane, VBox data) throws InterruptedException {
        Platform.runLater(()-> {
            updateGridPane(updatedMap,gridPane);
            updateData(updatedMap,data);
        });
        try{
            Thread.sleep(moveDelay);
        }catch (InterruptedException exception){
            throw new InterruptedException(exception.getMessage());
        }
    }

    private void updateGridPane(JungleMap map, GridPane gridPane){
        double constWidth = 260/((double)map.getWidth());
        double constHeight = 260/((double)map.getHeight());

        gridPane.setGridLinesVisible(false);
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        gridPane.setGridLinesVisible(true);
        Vector2d lowerLeft = map.getLowerLeftBorder();
        Vector2d upperRight = map.getUpperRightBorder();

        int width = upperRight.x - lowerLeft.x + 1;
        int height= upperRight.y - lowerLeft.y + 1;

        ColumnConstraints colConst = new ColumnConstraints(constWidth);
        RowConstraints rowConst = new RowConstraints(constHeight);

        for(int i = 0; i < width; i++){
            gridPane.getColumnConstraints().add(colConst);
        }
        for(int i = 0; i < height; i++){
            gridPane.getRowConstraints().add(rowConst);
        }

        for(IMapElement element:map.getElements()){
            gridPane.add(new GuiElementBox(element,constWidth,constHeight).getBox(),element.getPosition().x -lowerLeft.x,upperRight.y -element.getPosition().y);
        }
    }

    private void updateData(JungleMap map, VBox data){
        data.getChildren().clear();
        Label day = new Label("Day: " + map.getDay());
        Label numberOfAliveAnimals = new Label("Animals: "+ map.getNumberOfAliveAnimals());
        Label numberOfGrasses = new Label("Grasses: " + map.getNumberOfGrasses());
        Label dominatingGenotype = new Label("Dominating genotype: " + map.getDominantOfGenotypes());
        Label avgEnergy = new Label("Average energy: " + map.getAverageOfEnergy());
        Label avgDeadLifeSpan = new Label("Average lifespan for dead animals: " + map.getAverageAgeOfDeadAnimals());
        Label avgChildren = new Label("Average number of children :" + map.getAverageOfChildren());
        data.getChildren().addAll(day,numberOfAliveAnimals,numberOfGrasses,dominatingGenotype,avgEnergy,avgDeadLifeSpan,avgChildren);
    }

    private void createObjectsChart(LineChart<String,Number> lineChart, JungleMap jungleMap, ScheduledExecutorService scheduledExecutorService){
        CategoryAxis dayAxis = (CategoryAxis) lineChart.getXAxis();
        NumberAxis dataAxis = (NumberAxis) lineChart.getYAxis();

        dayAxis.setMaxWidth(15);

        lineChart.setTitle("Objects");
        dayAxis.setLabel("Day");
        dataAxis.setLabel("Value");

        lineChart.setMaxSize(300,250);
        lineChart.setMinSize(300,250);

        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);

        XYChart.Series<String,Number> animalSeries = new XYChart.Series<>();
        animalSeries.setName("Animals");
        XYChart.Series<String,Number> grassSeries = new XYChart.Series<>();
        grassSeries.setName("Grasses");
        lineChart.getData().addAll(animalSeries,grassSeries);

        scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(()-> {
            int day = jungleMap.getDay();
            animalSeries.getData().add(new XYChart.Data<>(String.valueOf(day),jungleMap.getNumberOfAliveAnimals()));
            grassSeries.getData().add(new XYChart.Data<>(String.valueOf(day),jungleMap.getNumberOfGrasses()));

            if(animalSeries.getData().size()>15){
                animalSeries.getData().remove(0);
            }
            if(grassSeries.getData().size()>15){
                grassSeries.getData().remove(0);
            }
        }),0,moveDelay, TimeUnit.MILLISECONDS);
    }

    private void createAverageChart(LineChart<String,Number> lineChart, JungleMap jungleMap, ScheduledExecutorService scheduledExecutorService){
        CategoryAxis dayAxis = (CategoryAxis) lineChart.getXAxis();
        NumberAxis dataAxis = (NumberAxis) lineChart.getYAxis();

        dayAxis.setMaxWidth(15);

        lineChart.setTitle("Averages");
        dayAxis.setLabel("Day");
        dataAxis.setLabel("Value");

        lineChart.setMaxSize(300,300);
        lineChart.setMinSize(300,300);

        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);

        XYChart.Series<String,Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Average Energy");
        XYChart.Series<String,Number> childrenSeries = new XYChart.Series<>();
        childrenSeries.setName("Average number of children");
        XYChart.Series<String,Number> lifeSpanSeries = new XYChart.Series<>();
        lifeSpanSeries.setName("Average life span dead animals");
        lineChart.getData().addAll(energySeries,lifeSpanSeries,childrenSeries);

        scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(()-> {
            int day = jungleMap.getDay();
            energySeries.getData().add(new XYChart.Data<>(String.valueOf(day),jungleMap.getAverageOfEnergy()));
            childrenSeries.getData().add(new XYChart.Data<>(String.valueOf(day),jungleMap.getAverageOfChildren()));
            lifeSpanSeries.getData().add(new XYChart.Data<>(String.valueOf(day),jungleMap.getAverageAgeOfDeadAnimals()));

            if(energySeries.getData().size()>15){
                energySeries.getData().remove(0);
            }
            if(lifeSpanSeries.getData().size()>15){
                lifeSpanSeries.getData().remove(0);
            }
            if(childrenSeries.getData().size()>15){
                childrenSeries.getData().remove(0);
            }
        }),0,moveDelay, TimeUnit.MILLISECONDS);
    }

    private void startSimulation(int width,int height,double jungleRatio, int plantEnergy,int moveEnergy,int startEnergy,int initialNumberOfAnimals,int moveDelay,int magical){
        this.moveDelay = moveDelay;
        boolean isMagic = magical == 1;

        JungleMap jungleMap1 = new JungleMap(width, height, jungleRatio, plantEnergy, moveEnergy, startEnergy, true, isMagic, initialNumberOfAnimals);
        JungleMap jungleMap2 = new JungleMap(width, height, jungleRatio, plantEnergy, moveEnergy, startEnergy, false, isMagic, initialNumberOfAnimals);

        GridPane gridPane1 = new GridPane();
        GridPane gridPane2 = new GridPane();

        VBox dataBox1 = new VBox();
        VBox dataBox2 = new VBox();

        ScheduledExecutorService scheduledExecutorService1=Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService scheduledExecutorService2=Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService scheduledExecutorService3=Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService scheduledExecutorService4=Executors.newSingleThreadScheduledExecutor();

        LineChart<String, Number> objectsChart1 = new LineChart<>(new CategoryAxis(), new NumberAxis());
        LineChart<String, Number> averagesChart1 = new LineChart<>(new CategoryAxis(), new NumberAxis());
        LineChart<String, Number> objectsChart2 = new LineChart<>(new CategoryAxis(), new NumberAxis());
        LineChart<String, Number> averagesChart2 = new LineChart<>(new CategoryAxis(), new NumberAxis());

        SimulationEngine simulationEngine1 = new SimulationEngine(jungleMap1,gridPane1,dataBox1,moveDelay);
        simulationEngine1.setObserver(this);
        SimulationEngine simulationEngine2 = new SimulationEngine(jungleMap2,gridPane2,dataBox2,moveDelay);
        simulationEngine2.setObserver(this);

        createObjectsChart(objectsChart1, jungleMap1,scheduledExecutorService1);
        createAverageChart(averagesChart1, jungleMap1,scheduledExecutorService2);
        createObjectsChart(objectsChart2, jungleMap2,scheduledExecutorService3);
        createAverageChart(averagesChart2, jungleMap2,scheduledExecutorService4);

        HBox chartsBox1 = new HBox();
        chartsBox1.getChildren().addAll(objectsChart1,averagesChart1);
        HBox chartsBox2 = new HBox();
        chartsBox2.getChildren().addAll(objectsChart2,averagesChart2);

        HBox buttonAndMapBox1 = new HBox(10);
        Button startButton1 = new Button("Start/Resume");
        startButton1.setOnAction(event -> simulationEngine1.startOrResume());
        buttonAndMapBox1.getChildren().addAll(startButton1,gridPane1);
        HBox buttonAndMapBox2 = new HBox(10);
        Button startButton2 = new Button("Start/Resume");
        startButton2.setOnAction(event -> simulationEngine2.startOrResume());
        buttonAndMapBox2.getChildren().addAll(startButton2,gridPane2);

        HBox mapInfo1 = new HBox();
        Label borderInfo1 = new Label("Borders on!");
        Label magicInfo1;
        if(isMagic){
            magicInfo1 = new Label("Magic ");
        }
        else{
            magicInfo1 = new Label("Not Magic ");
        }
        HBox mapInfo2 = new HBox();
        Label borderInfo2 = new Label("Borders off!");
        Label magicInfo2;
        if(isMagic){
            magicInfo2 = new Label("Magic ");
        }
        else{
            magicInfo2 = new Label("Not Magic ");
        }
        Font font = Font.font("Verdana", FontWeight.EXTRA_BOLD, 15);
        magicInfo1.setFont(font);
        magicInfo2.setFont(font);
        borderInfo1.setFont(font);
        borderInfo2.setFont(font);
        mapInfo1.getChildren().addAll(magicInfo1,borderInfo1);
        mapInfo2.getChildren().addAll(magicInfo2,borderInfo2);

        VBox leftLayout = new VBox();
        leftLayout.getChildren().addAll(mapInfo1,buttonAndMapBox1,dataBox1,chartsBox1);
        VBox rightLayout = new VBox();
        rightLayout.getChildren().addAll(mapInfo2,buttonAndMapBox2,dataBox2,chartsBox2);

        HBox mainLayout = new HBox();
        mainLayout.setSpacing(50);
        mainLayout.getChildren().addAll(leftLayout,rightLayout);

        Scene scene = new Scene(mainLayout);
        Stage mainStage = new Stage();
        mainStage.setMaximized(true);
        mainStage.setResizable(true);
        mainStage.setScene(scene);
        mainStage.show();

        Thread curThread1 = new Thread(simulationEngine1);
        curThread1.setDaemon(true);
        Thread curThread2 = new Thread(simulationEngine2);
        curThread2.setDaemon(true);

        curThread1.start();
        curThread2.start();
    }

    @Override
    public void start(Stage primaryStage){
        //FILTERS AND TEXT-FORMATTERS
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c ;
            } else {
                return null ;
            }
        };

        StringConverter<Double> converter = new StringConverter<>() {
            @Override
            public Double fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0.0;
                } else {
                    return Double.valueOf(s);
                }
            }

            @Override
            public String toString(Double d) {
                return d.toString();
            }
        };

        TextFormatter<Double> doubleTextFormatter = new TextFormatter<>(converter, 0.2, filter);

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String input = change.getText();
            if (input.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        UnaryOperator<TextFormatter.Change> bitFilter = change -> {
            String input = change.getText();
            if (input.matches("[0-1]*")) {
                return change;
            }
            return null;
        };
        // INPUT STAGE
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label widthLabel = new Label("Width");
        final TextField width = new TextField();
        width.setPromptText("Width");
        width.setPrefColumnCount(10);
        GridPane.setConstraints(widthLabel, 0, 0);
        grid.getChildren().add(widthLabel);
        GridPane.setConstraints(width, 0, 1);
        grid.getChildren().add(width);
        width.setTextFormatter(new TextFormatter<String>(integerFilter));

        Label heightLabel = new Label("Height");
        final TextField height = new TextField();
        height.setPromptText("Height.");
        GridPane.setConstraints(heightLabel, 0, 2);
        grid.getChildren().add(heightLabel);
        GridPane.setConstraints(height, 0, 3);
        grid.getChildren().add(height);
        height.setTextFormatter(new TextFormatter<String>(integerFilter));

        Label jungleRatioLabel = new Label("Jungle ratio");
        final TextField jungleRatio = new TextField();
        jungleRatio.setPrefColumnCount(15);
        jungleRatio.setPromptText("JungleRatio");
        GridPane.setConstraints(jungleRatioLabel, 0, 4);
        grid.getChildren().add(jungleRatioLabel);
        GridPane.setConstraints(jungleRatio, 0, 5);
        grid.getChildren().add(jungleRatio);
        jungleRatio.setTextFormatter(doubleTextFormatter);

        Label plantEnergyLabel = new Label("Plant energy");
        final TextField plantEnergy = new TextField();
        plantEnergy.setPrefColumnCount(15);
        plantEnergy.setPromptText("PlantEnergy");
        GridPane.setConstraints(plantEnergyLabel, 0, 6);
        grid.getChildren().add(plantEnergyLabel);
        GridPane.setConstraints(plantEnergy, 0, 7);
        grid.getChildren().add(plantEnergy);
        plantEnergy.setTextFormatter(new TextFormatter<String>(integerFilter));

        Label moveEnergyLabel = new Label("Move energy");
        final TextField moveEnergy = new TextField();
        moveEnergy.setPrefColumnCount(15);
        moveEnergy.setPromptText("MoveEnergy");
        GridPane.setConstraints(moveEnergyLabel, 0, 8);
        grid.getChildren().add(moveEnergyLabel);
        GridPane.setConstraints(moveEnergy, 0, 9);
        grid.getChildren().add(moveEnergy);
        moveEnergy.setTextFormatter(new TextFormatter<String>(integerFilter));

        Label startEnergyLabel = new Label("Start energy");
        final TextField startEnergy = new TextField();
        startEnergy.setPrefColumnCount(15);
        startEnergy.setPromptText("StartEnergy");
        GridPane.setConstraints(startEnergyLabel, 0, 10);
        grid.getChildren().add(startEnergyLabel);
        GridPane.setConstraints(startEnergy, 0, 11);
        grid.getChildren().add(startEnergy);
        startEnergy.setTextFormatter(new TextFormatter<String>(integerFilter));

        Label initialNumberOfAnimalsLabel = new Label("Initial number of animals");
        final TextField initialNumberOfAnimals = new TextField();
        initialNumberOfAnimals.setPrefColumnCount(15);
        initialNumberOfAnimals.setPromptText("InitialNumberOfAnimals");
        GridPane.setConstraints(initialNumberOfAnimalsLabel, 0, 12);
        grid.getChildren().add(initialNumberOfAnimalsLabel);
        GridPane.setConstraints(initialNumberOfAnimals, 0, 13);
        grid.getChildren().add(initialNumberOfAnimals);
        initialNumberOfAnimals.setTextFormatter(new TextFormatter<String>(integerFilter));

        Label magicLabel = new Label("Choose 1 if magic, 0 otherwise.");
        final TextField magical = new TextField();
        magical.setPrefColumnCount(15);
        magical.setPromptText("Magical-1, Not Magical-0");
        GridPane.setConstraints(magicLabel, 0, 14);
        grid.getChildren().add(magicLabel);
        GridPane.setConstraints(magical, 0, 15);
        grid.getChildren().add(magical);
        magical.setTextFormatter(new TextFormatter<String>(bitFilter));
        magical.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Integer.parseInt(newValue);
                if(newValue.length() > 1)
                    magical.setText(oldValue);
            } catch (Exception e) {
                magical.setText(oldValue);
            }
        });

        Label moveDelayLabel = new Label("Move delay");
        final TextField moveDelay = new TextField();
        moveDelay.setPrefColumnCount(15);
        moveDelay.setPromptText("moveDelay");
        GridPane.setConstraints(moveDelayLabel, 0, 16);
        grid.getChildren().add(moveDelayLabel);
        GridPane.setConstraints(moveDelay, 0, 17);
        grid.getChildren().add(moveDelay);
        moveDelay.setTextFormatter(new TextFormatter<String>(integerFilter));

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        Button submit = new Button("Submit");
        Button clear = new Button("Clear");
        buttons.getChildren().addAll(submit,clear);
        GridPane.setConstraints(buttons, 0, 18);
        grid.getChildren().add(buttons);

        //WYŚWIETLANY TEKTS JEŚLI INPUT JEST NIEPOPRAWNY.
        final Label label = new Label();
        GridPane.setConstraints(label, 0, 19);
        GridPane.setColumnSpan(label, 2);
        grid.getChildren().add(label);

        //PRZYCISK ZATWIERDZAJĄCY FORMULARZ
        submit.setOnAction(e -> {
            if ((width.getText() != null && !height.getText().isEmpty() &&!jungleRatio.getText().isEmpty() && !plantEnergy.getText().isEmpty() && !moveEnergy.getText().isEmpty() && !startEnergy.getText().isEmpty() && !initialNumberOfAnimals.getText().isEmpty() && !moveDelay.getText().isEmpty() && !magical.getText().isEmpty())) {
                int wh;
                wh = Integer.parseInt(width.getText());

                int ht;
                ht = Integer.parseInt(height.getText());

                double jr;
                jr = Double.parseDouble(jungleRatio.getText());

                int pe;
                pe = Integer.parseInt(plantEnergy.getText());

                int me;
                me = Integer.parseInt(moveEnergy.getText());

                int se;
                se = Integer.parseInt(startEnergy.getText());

                int inoa;
                inoa = Integer.parseInt(initialNumberOfAnimals.getText());

                int md;
                md = Integer.parseInt(moveDelay.getText());

                int mg;
                mg = Integer.parseInt(magical.getText());

                startSimulation(wh,ht,jr,pe,me,se,inoa,md,mg);

            } else {
                label.setText("You need to fill all fields!");
            }

        });

        //PRZYCISK RESETUJĄCY FORMULARZ
        clear.setOnAction(e -> {
            width.clear();
            height.clear();
            jungleRatio.clear();
            plantEnergy.clear();
            moveEnergy.clear();
            startEnergy.clear();
            initialNumberOfAnimals.clear();
            moveDelay.clear();
            magical.clear();
            label.setText(null);
        });

        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
