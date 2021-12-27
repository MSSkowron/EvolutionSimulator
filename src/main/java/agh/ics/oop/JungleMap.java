package agh.ics.oop;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class JungleMap implements IWorldMap,IPositionChangeObserver {
    private final int width;
    private final int height;
    private final Vector2d upperRightBorder;
    private final Vector2d lowerLeftBorder;
    private final int widthJungle;
    private final int heightJungle;
    private  Vector2d upperRightJungle;
    private  Vector2d lowerLeftJungle;
    private final int plantEnergy;
    private final int moveEnergy;
    private final int startEnergy;
    private final boolean borderOn;
    private final boolean magical;
    private int magicCounter;
    private int numberOfAliveAnimals;
    private int numberOfGrasses;
    private int day;
    private int deadCounter;
    private int totalDeadAge;

    Map<String, Integer> genotypes;
    Map<Vector2d, SortedSet<Animal>> animals;
    Map<Vector2d, Grass> grasses;
    List<Animal> animalsList;
    List<Grass> grassesList;

    public JungleMap(int width, int height, double jungleRatio, int plantEnergy, int moveEnergy, int startEnergy, boolean borderOn, boolean magical, int initialNumberOfAnimals){
        this.deadCounter=0;
        this.totalDeadAge=0;
        this.numberOfAliveAnimals=0;
        this.numberOfGrasses=0;
        this.startEnergy=startEnergy;
        this.plantEnergy=plantEnergy;
        this.moveEnergy=moveEnergy;
        this.borderOn=borderOn;
        this.magical=magical;
        this.magicCounter = 0;
        this.width=width;
        this.height=height;
        this.widthJungle= (int) Math.floor(jungleRatio*width);
        this.heightJungle = (int) Math.floor(jungleRatio*height);
        this.upperRightBorder = new Vector2d(width-1,height-1);
        this.lowerLeftBorder = new Vector2d(0,0);
        this.animals = new ConcurrentHashMap<>();
        this.grasses = new ConcurrentHashMap<>();
        this.genotypes=new ConcurrentHashMap<>();
        this.animalsList = Collections.synchronizedList(new ArrayList<>());
        this.grassesList =Collections.synchronizedList(new ArrayList<>());
        calculateJungleBorders();
        initialPlacement(initialNumberOfAnimals);
    }

    //POCZĄTKOWY SPAWN ZWIERZAT NA MAPIE.
    public void initialPlacement(int number){
        LinkedList<Vector2d> ll = getFreePositions();
        for(int i = 0;i<number;i++){
            int index = (int) (Math.random() * ll.size());
            Vector2d randomPosition = ll.get(index);
            place(new Animal(this,randomPosition,startEnergy));
            ll.remove(index);
        }
    }

    //USUWANIE MARTWYCH ZWIERZAT.
    public boolean removeDeadAnimals() {
        day += 1;
        Iterator<Animal> iterator = animalsList.iterator();
        while(iterator.hasNext()){
            Animal animal = iterator.next();
            if(!animal.isAlive()){
                removeAnimalFromSet(animal.getPosition(),animal);
                removeGenotype(animal.getGenes().getArray());
                totalDeadAge += animal.getAge();
                deadCounter += 1;
                numberOfAliveAnimals -= 1;
                iterator.remove();
            }
            else{
                animal.daySurvived();
            }
        }
        return numberOfAliveAnimals == 0;
    }

    //PORUSZENIE SIE KAZDYM ZE ZWIERZĄT
    public void randomMovesAnimals() {
        for (Animal animal : animalsList) {
            animal.randomMove();
        }
    }

    //POBIERANIE OPLATY OD ZWIERZAT ZA DZIEN ZYCIA.
    public void dailyEnergyCost() {
        for (Animal animal : animalsList) {
            animal.changeEnergy((-1) * moveEnergy);
        }
    }

    //JEDZENIE TRAWY.
    public void eat() {
        Iterator<Grass> iterator = grassesList.iterator();
        while(iterator.hasNext()){
            Grass grass = iterator.next();
            SortedSet<Animal> ss =  animals.get(grass.getPosition());
            if(ss != null){
                removeGrassFromSet(grass.getPosition());
                int counter = 0;
                int energyRequiredToEat = ss.first().getEnergy();
                for(Animal animal:ss){
                    if(animal.getEnergy()==energyRequiredToEat){
                        counter += 1;
                    }
                    else{
                        break;
                    }
                }
                for(Animal animal:ss){
                    if(animal.getEnergy()==energyRequiredToEat){
                        animal.changeEnergy((int) (plantEnergy/((double)counter)));
                    }
                    else{
                        break;
                    }
                }
                iterator.remove();
            }
        }
    }

    //ROZMNAZANIE
    public void copulation() {
        // Pozycje na których doszło do rozmnożenia. Muszę je później zaktualizować, żeby utrzymać elementy posortowane w danym Secie.
        HashSet<Vector2d> toUpdate = new HashSet<>();
        for(SortedSet<Animal> ss : animals.values()){
            if(ss!=null && ss.size()>=2){
                int requiredEnergy = ss.first().getEnergy();
                //Jeśli pierwsze i drugie zwierzę z największą ilością energii mają taką samą energie to potencjalnie może być więcej zwierząt o tej samej enerii co dwa
                //pierwsze, a wtedy może dojść do większej ilości rozmnożeń na danej pozycji. Natomiast jeśli mają różną energię to rozmnażają się tylko te dwa pierwsze.
                //Biore zwierzęta które będą potencjalnymi rodzicami czyli zwierzęta mające największą energie na pozycji lub dwa pierwsze.
                LinkedList<Animal> parents = getAnimalsToCopulation(ss,requiredEnergy);
                int i = 0;
                //Rodziców biorę parami z parents.
                while(i<parents.size() && i + 1 < parents.size()){
                    Animal mother = parents.get(i);
                    Animal father = parents.get(i+1);
                    if(mother.getEnergy() >= 0.5*startEnergy && father.getEnergy() >= 0.5*startEnergy){
                        toUpdate.add(father.getPosition());
                        mother.newChild();
                        father.newChild();
                        Animal child = createChild(father,mother);
                        mother.changeEnergy((int) -(0.25*mother.getEnergy()));
                        father.changeEnergy((int) -(0.25*father.getEnergy()));
                        place(child);
                    }
                    i+=2;
                }
            }
        }
        updateSets(toUpdate);
    }

    //AKTUALIZACJA SETOW.
    private void updateSets(HashSet<Vector2d> toUpdate) {
        for(Vector2d position : toUpdate){
            SortedSet<Animal> newSet = new ConcurrentSkipListSet<>(cmp);
            for(Animal animal:animals.get(position)){
                newSet.add(animal);
            }
            animals.put(position,newSet);
        }
    }

    //DZIECKO POWSTALE W SKUTEK ROZMNOZENIA.
    private Animal createChild(Animal father, Animal mother){
        int energy = (int) (0.25*father.getEnergy() + 0.25*mother.getEnergy());
        Genes genes  = new Genes(father.getGenes().getArray(),father.getEnergy(),mother.getGenes().getArray(),mother.getEnergy());
        return new Animal(this,mother.getPosition(),energy,genes);
    }

    //LOSOWY SPAWN TRAWY W STEPIE I JUNLGI.
    public void randomGrass(){
        LinkedList<Vector2d> ll_jungleFreePositions = new LinkedList<>();
        LinkedList<Vector2d> ll_steppeFreePositions = new LinkedList<>();
        for(int i = lowerLeftBorder.x;i<=upperRightBorder.x;i++){
            for(int j = lowerLeftBorder.y;j<=upperRightBorder.y;j++){
                Vector2d vector2d = new Vector2d(i,j);
                //Wybieram tylko te pozycje, gdzie nie ma zwierzęcia oraz trawy.
                if(objectAt(vector2d)==null){
                    if(vector2d.precedes(upperRightJungle) && vector2d.follows(lowerLeftJungle)){
                        ll_jungleFreePositions.add(vector2d);
                    }
                    else{
                        ll_steppeFreePositions.add(vector2d);
                    }
                }
            }
        }
        if(ll_jungleFreePositions.size()>0){
            Vector2d randomPosition =  ll_jungleFreePositions.get((int) (Math.random() * ll_jungleFreePositions.size()));
            placeGrass(new Grass(randomPosition));
        }
        if(ll_steppeFreePositions.size()>0){
            Vector2d randomPosition =  ll_steppeFreePositions.get((int) (Math.random() * ll_steppeFreePositions.size()));
            placeGrass(new Grass(randomPosition));
        }
    }

    //PROBA MAGICZNA
    public void magicalTry(){
        if(magical && magicCounter < 3 && numberOfAliveAnimals==5){
            magicPlacement();
            magicCounter += 1;
        }
    }

    private void magicPlacement(){
        LinkedList<Vector2d> ll =  getFreePositions();
        LinkedList<Animal> magicAnimals = new LinkedList<>();
        for(Animal animal: animalsList){
            if(ll.size()>0){
                int index = (int) (Math.random() * ll.size());
                Vector2d randomPosition = ll.get(index);
                magicAnimals.add(new Animal(this,randomPosition,startEnergy,animal.getGenes()));
                ll.remove(index);
            }
            else{
                break;
            }
        }
        for(Animal animal:magicAnimals){
            place(animal);
        }
    }

    //PLACE//ADD//REMOVE
    @Override
    public boolean place(Animal animal){
        if(animals.get(animal.getPosition())==null){
            SortedSet<Animal> ss = new ConcurrentSkipListSet<>(cmp);
            ss.add(animal);
            animals.put(animal.getPosition(),ss);
        }
        else{
            (animals.get(animal.getPosition())).add(animal);
        }
        addGenotype(animal.getGenes().getArray());
        numberOfAliveAnimals += 1;
        animalsList.add(animal);
        animal.addObserver(this);
        return true;
    }

    private void removeAnimalFromSet(Vector2d position,Animal animal){
        animals.get(position).remove(animal);
        if(animals.get(position).size()==0){
            animals.remove(position);
        }
    }

    public void placeGrass(Grass grass){
        grasses.put(grass.getPosition(),grass);
        grassesList.add(grass);
        numberOfGrasses += 1;
    }

    private void removeGrassFromSet(Vector2d position){
        grasses.remove(position);
        numberOfGrasses -= 1;
    }

    private void addGenotype(int[] array){
        String str = Arrays.toString(array);
        if(genotypes.get(str)==null){
            genotypes.put(str,1);
        }else{
            int counter = genotypes.get(str);
            genotypes.put(str,counter+1);
        }
    }

    private void removeGenotype(int[] array){
        String str = Arrays.toString(array);
        int counter = genotypes.get(str);
        if((counter - 1) ==0){
            genotypes.remove(str);
        }
        else{
            genotypes.put(str,counter-1);
        }
    }

    //INTERFACES METHODS
    @Override
    public void positionChanged(Vector2d oldPosition, Vector2d newPosition,Animal animal) {
        removeAnimalFromSet(oldPosition,animal);
        Vector2d newRightPosition = calculateRightPosition(newPosition);
        animal.setPosition(newRightPosition);
        SortedSet<Animal> newSet = animals.get(newRightPosition);
        if(newSet == null){
            SortedSet<Animal> ss = new ConcurrentSkipListSet<>(cmp);
            ss.add(animal);
            animals.put(newRightPosition,ss);
        }
        else{
            newSet.add(animal);
        }
    }

    @Override
    public boolean isOccupied(Vector2d position) {
        return objectAt(position)!=null;
    }

    @Override
    public Object objectAt(Vector2d position) {
        SortedSet<Animal> ss =  animals.get(position);
        if(ss==null || ss.size()==0){
            return grasses.get(position);
        }
        else {
            return ss.first();
        }
    }

    @Override
    public boolean canMoveTo(Vector2d position) {
        if(borderOn){
            return position.precedes(upperRightBorder) && position.follows(lowerLeftBorder);
        }
        else{
            return true;
        }
    }

    //GETTERS
    private LinkedList<Vector2d> getFreePositions() {
        LinkedList<Vector2d> freePositions = new LinkedList<>();
        for(int i = lowerLeftBorder.x;i<=upperRightBorder.x;i++){
            for(int j = lowerLeftBorder.y;j<=upperRightBorder.y;j++){
                Vector2d position = new Vector2d(i,j);
                if(objectAt(position)==null){
                    freePositions.add(new Vector2d(i,j));
                }
            }
        }
        return freePositions;
    }

    private LinkedList<Animal> getAnimalsToCopulation(SortedSet<Animal> ss, int requiredEnergy) {
        LinkedList<Animal> result = new LinkedList<>();
        for(Animal animal : ss){
            if(animal.getEnergy()==requiredEnergy){
                result.add(animal);
            }
            else{
                if(result.size()==1){
                    result.add(animal);
                }
                break;
            }
        }
        return result;
    }

    public LinkedList<IMapElement> getElements(){
        LinkedList<IMapElement> elements = new LinkedList<>();
        for(SortedSet<Animal> ss : animals.values()){
            for(Animal animal:ss){
                if(animal.getEnergy()>0){
                    elements.add(animal);
                }
            }
        }
        for(Grass grass:grasses.values()){
            elements.add(grass);
        }
        return elements;
    }

    public String getDominantOfGenotypes(){
        if(genotypes.size() == 0){
            return "";
        }
        else{
            int x = 0;
            String dominant = "";
            for (Map.Entry<String, Integer> entry : genotypes.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();
                if(value > x){
                    x = value;
                    dominant = key;
                }
            }
            return dominant;
        }
    }

    public double getAverageOfEnergy(){
        if(numberOfAliveAnimals == 0){
            return 0;
        }
        int energySum = 0;
        for(SortedSet<Animal> ss : animals.values()){
            for(Animal animal:ss){
                energySum += animal.getEnergy();
            }
        }
        return energySum/((double) numberOfAliveAnimals);
    }

    public double getAverageAgeOfDeadAnimals(){
        if(deadCounter==0){
            return 0.0;
        }
        return totalDeadAge/((double) deadCounter);
    }

    public double getAverageOfChildren(){
        if(numberOfAliveAnimals == 0){
            return 0.0;
        }
        int childrenSum = 0;
        for(SortedSet<Animal> ss : animals.values()){
            for(Animal animal:ss){
                childrenSum += animal.getNumberOfChildren();
            }
        }
        return childrenSum/((double) numberOfAliveAnimals);
    }

    public int getDay(){
        return day;
    }

    public int getNumberOfAliveAnimals(){
        return numberOfAliveAnimals;
    }

    public int getNumberOfGrasses(){
        return numberOfGrasses;
    }

    public Vector2d getLowerLeftBorder() {
        return lowerLeftBorder;
    }

    public Vector2d getUpperRightBorder() {
        return upperRightBorder;
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    //CALCULATORS
    public Vector2d calculateRightPosition(Vector2d pos){
        int x;
        int y;

        if(pos.x < lowerLeftBorder.x){
            x = upperRightBorder.x;
        }
        else if(pos.x > upperRightBorder.x){
            x = lowerLeftBorder.x;
        }
        else {
            x = pos.x;
        }
        if(pos.y < lowerLeftBorder.y){
            y = upperRightBorder.y;
        }
        else if(pos.y>upperRightBorder.y){
            y = lowerLeftBorder.y;
        }
        else {
            y = pos.y;
        }
        return new Vector2d(x,y);
    }

    private void calculateJungleBorders(){
        Vector2d lowerLeft = new Vector2d(0,0);
        Vector2d upperRight = new Vector2d(width-1,height-1);
        int widthDiff=width-widthJungle;
        int heightDiff = height-heightJungle;

        for(int i = 0;i < widthDiff;i ++){
            if(i % 2 == 0){
                lowerLeft = lowerLeft.add(new Vector2d(1,0));
            }
            else{
                upperRight = upperRight.subtract(new Vector2d(1,0));
            }
        }
        for(int i = 0;i < heightDiff;i ++){
            if(i % 2 == 0){
                lowerLeft = lowerLeft.add(new Vector2d(0,1));
            }
            else{
                upperRight = upperRight.subtract(new Vector2d(0,1));
            }
        }
        lowerLeftJungle=lowerLeft;
        upperRightJungle=upperRight;
    }

    //COMPARATOR
    private final Comparator<Animal> cmp = (o1, o2) -> {
        if(o1.getEnergy()!=o2.getEnergy()){
            return o2.getEnergy() - o1.getEnergy();
        }
        else{
            return o2.hashCode()-o1.hashCode();
        }
    };
}
