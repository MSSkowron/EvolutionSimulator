package agh.ics.oop;

import java.util.Arrays;

public class Genes {
    private final int[] genes;

    public Genes(){
        genes = new int[32];
        for(int i = 0;i<32;i++){
            genes[i] = (int) (Math.random() * 8);
        }
        Arrays.sort(genes);
    }

    public Genes(int[] fatherGenes,int fatherEnergy,int[] motherGenes,int motherEnergy){
        genes = new int[32];
        int energySum = fatherEnergy + motherEnergy;
        int fromFather = (int) Math.floor((fatherEnergy/((double) energySum))*32);
        int fromMother = 32 - fromFather;
        int site = (int) (Math.random()*2);

        if(site == 0 ){
            if(fatherEnergy > motherEnergy){
                for(int i=0;i<fromFather;i++){
                    genes[i]=fatherGenes[i];
                }
                for(int i=0;i<fromMother;i++){
                    genes[31-i]=motherGenes[31-i];
                }
            }
            else{
                for(int i=0;i<fromMother;i++){
                    genes[i]=motherGenes[i];
                }
                for(int i=0;i<fromFather;i++){
                    genes[31-i]=fatherGenes[31-i];
                }
            }
        }
        else{
            if(fatherEnergy > motherEnergy){
                for(int i=0;i<fromFather;i++){
                    genes[31-i]=fatherGenes[31-i];
                }
                for(int i=0;i<fromMother;i++){
                    genes[i]=motherGenes[i];
                }
            }
            else{
                for(int i=0;i<fromMother;i++){
                    genes[31-i]=motherGenes[31-i];
                }
                for(int i=0;i<fromFather;i++){
                    genes[i]=fatherGenes[i];
                }
            }
        }
        Arrays.sort(genes);
    }

    public int[] getArray(){
        return genes;
    }

    public int getRandom(){
        return genes[(int) (Math.random()*32)];
    }
}
