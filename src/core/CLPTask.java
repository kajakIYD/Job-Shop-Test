/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;
import org.jacop.search.SmallestMin;
import core.Tuple;
import org.jacop.constraints.And;
import org.jacop.constraints.Cumulative;
import org.jacop.constraints.Diff2;
import org.jacop.constraints.IfThen;
import org.jacop.constraints.Sum;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.XeqY;
import org.jacop.constraints.XlteqY;

/**
 *
 * @author PF
 */
public class CLPTask {
    
    public Store store;
    protected Search search;
    
    private IntVar one;
    
    private int numMachines;
    int Inf = 10000;
    private int numTasksKinds;

    private IntVar[][] varsX_i_type;
    private IntVar[][] taskDurationList_i_type;    
    private static IntVar[] tasksDuration;
    private static int[] numTasks; //Number of tasks of different kinds
    private static IntVar[] tasksDurationsList;
    private int[] tasksDurationsListInt;
    
    IntVar[] varsX;
    IntVar[] varsY;
    IntVar[] size;

    private IntVar t1;
    private IntVar t2;
    private IntVar t3;
    private IntVar t4;
    private IntVar t5;
    private static IntVar energySum;
    private IntVar[] cumulativeLimit;
    private IntVar[][] materialUsageList; //As much stores as numTasksKinds (first dimension) As much items as tasks of each type

    private boolean energyOptimizationSelected;
    
    private static int maxXCoord;
    
    public CLPTask(int numTasksKinds, int[] tasksDurationInt, 
            int[] numTasksQuantity, int numMachines, int[] cumulativeLimit,
            int isEnergyOptimizationSelected) 
    {
        store = new Store(); 
        
        //Durations of tasks of different kinds
        t1 = new IntVar(store, tasksDurationInt[0], tasksDurationInt[0]);
        t2 = new IntVar(store, tasksDurationInt[1], tasksDurationInt[1]);
        t3 = new IntVar(store, tasksDurationInt[2], tasksDurationInt[2]);
        t4 = new IntVar(store, tasksDurationInt[3], tasksDurationInt[3]);
        t5 = new IntVar(store, tasksDurationInt[4], tasksDurationInt[4]);
        
        IntVar[] taskDurationArrayTemplate = new IntVar[]{t1, t2, t3, t4, t5};
        
        one = new IntVar(store, 1, 1);
        this.cumulativeLimit = new IntVar[numTasksKinds];
        for(int i = 0; i < numTasksKinds; ++i)
        {
            this.cumulativeLimit[i] = new IntVar(store, cumulativeLimit[i], 
                                        cumulativeLimit[i]);
        }
        
        tasksDuration = new IntVar[numTasksKinds];
        for (int i = 0; i < numTasksKinds; ++i)
        {
            tasksDuration[i] = taskDurationArrayTemplate[i];
        }
        
        int counter = 0;
        int numAllTasksForProblem = 0;
        for( int num : numTasksQuantity) 
        {
            numAllTasksForProblem = numAllTasksForProblem+num;
        }
        tasksDurationsListInt = new int[numAllTasksForProblem];
        
        for (int i = 0; i < numTasksQuantity.length; ++i)
        {
            for (int j = 0; j < numTasksQuantity[i]; ++i)
            {
                tasksDurationsListInt[counter] = tasksDurationInt[i];
                ++counter;
            }            
        }   
        
        tasksDurationsList = new IntVar[numAllTasksForProblem];
        
        numTasks = numTasksQuantity;
        this.numMachines = numMachines;
        this.numTasksKinds = numTasksKinds;
        
        energyOptimizationSelected = (isEnergyOptimizationSelected == 1) ? true : false;
    }    
    
    private IntVar[][] makeTasks()
    {
        System.out.println("core.CLPTask.makeTasks()");
        
        int noRectangles = numTasks[0] + numTasks[1] + numTasks[2] + numTasks[3] + numTasks[4];	
        //int noRectanglesRearming = numTasks[4]; //Simplyfy task and do not use it
        
        int counter = 0;
        //int counterRearming = 0; //Przezbrajanie
        //final int rearmingDuration = 15; //Trwanie przezbrojenia
        
        varsX = new IntVar[noRectangles];
        varsY = new IntVar[noRectangles];
        
        varsX_i_type = new IntVar[5][];
        taskDurationList_i_type = new IntVar[5][];
        
        materialUsageList = new IntVar[numTasksKinds][];
        for (int i = 0; i < numTasksKinds; i++) 
        {
            materialUsageList[i] = new IntVar[numTasks[i]];
        }
        
        size = new IntVar[noRectangles];

        IntVar[][] rectangles = new IntVar[noRectangles][4];
        //IntVar[][] rectanglesRearming = new IntVar[noRectanglesRearming][4]; //Simplyfy task

        System.out.print("Constraint model based on Diff2 constraint");
        
            
        for (int i = 0; i < numTasks.length; i++) 
        {
            int counterMaterialUsage = 0;
            
            varsX_i_type[i] = new IntVar[numTasks[i]];
            taskDurationList_i_type[i] = new IntVar[numTasks[i]];
            
            for(int j = 0; j < numTasks[i]; j++)
            {
                IntVar X = new IntVar(store, "x"+counter, 0, Inf);

                IntVar Y = new IntVar(store, "y"+counter, 0, numMachines - 1);
                //IntVar YRearming = new IntVar(store, "yRearming"+counter, 1, 1); //Simplyfy

                IntVar[] jthRectangle = {X, Y, tasksDuration[i], one};
                
                
                rectangles[counter] = jthRectangle;
                //Add rearming
    //            if(numTasks[i] == rearmingDuration)
    //            {
    //                IntVar[] jthRectangleRearming = {X, YRearming, tasksDuration[i], one};
    //                rectanglesRearming[counterRearming] = jthRectangleRearming;
    //                counterRearming++;
    //            }

                varsX[counter] = X; 
                varsY[counter] = Y;
                for(int k = 0; k < materialUsageList[i].length; ++k)
                {
//                    materialUsageList[i][k] = new IntVar(store, counterMaterialUsage, 
//                                                counterMaterialUsage);
                    materialUsageList[i][k] = new IntVar(store, 1, 1);
//                    ++counterMaterialUsage;
                }
                
                varsX_i_type[i][j] = X;
                taskDurationList_i_type[i][j] = tasksDuration[i];
                
                tasksDurationsList[counter] = new IntVar(store, 
                                                tasksDurationsListInt[i],
                                                tasksDurationsListInt[i]);
                ++counter;
            }
        }
        return rectangles;
    }

    public void takeFromMagazineConstraint()
    {
        //No i jeszcze mozna dolozyc cos w rodzaju jesli ktorykolwiek z x-ów jest 
        //równy 300(a w zasadzie wielokrotności tej liczby, a liczba jest z czapy
        // bo chodzi o to zeby to byl jeden dzien) to dołóż
        //do magazynu wejściowego oraz do magazynu wyjściowego(to co się udało 
        //wyprodukować) no i oczywiście zabierz z magazynu wyjściowego
        int dayDuration = 100;
        IntVar endOfTheDay = new IntVar(store, dayDuration, dayDuration);
        IntVar magazineSet = new IntVar(store, 0, 1);
        IntVar zero = new IntVar(store, 0, 0);
        XeqY magazineSetFalse = new XeqY(magazineSet, zero);

        for (int i = 0; i < varsX.length; ++i)
        {
            IntVar valX = varsX[i];
            IntVar taskType;
            for (int j = 0; j < tasksDuration.length; ++j)
            {
                if(tasksDurationsList[i].value() == tasksDuration[j].value())
                {
                    taskType = new IntVar(store, j, j);
                    break;
                }                
            }

            XlteqY lteqCstr = new XlteqY(endOfTheDay, valX);
            XeqY magazineSetTrue = new XeqY(magazineSet, one);

            And andCstr_if = new And(lteqCstr, lteqCstr);
            And andCstr_then = new And(lteqCstr, magazineSetTrue);
            IfThen ifCstr = new IfThen(andCstr_if, andCstr_then);
        }
    }
    
    public void model()
    {
        System.out.println("core.CLPTask.model()");
        IntVar[][] rectangles = makeTasks();
        
        //Wskaznik energetyzcny = suma(y) [czyli najlepiej na zerową 
        //maszynę wsadzać:p]
        energySum = new IntVar(store, "sum", 0, Inf); 
        SumInt energy = new SumInt(store, varsY, "==", energySum); 
        System.out.println("\nImpose constraint energy...");
        store.impose(energy);
        
        Diff2 ctrDiff = new Diff2(rectangles);
        System.out.println("\nImpose constraint diff2...");
        store.impose(ctrDiff);
        
        int counter = 0;
        System.out.println("\nImpose constraint cumConstraint...");
        for (int i = 0; i < numTasksKinds; ++i)
        {
            //Dla kazdego rodzaju zadan naloz ograniczenie cumulative, zeby powiazac 
            //magazyn wejsciowy z liczba wykonanych zadan
            Cumulative cumConstraint = new Cumulative(varsX_i_type[i], 
                                                taskDurationList_i_type[i],
                                                materialUsageList[i], cumulativeLimit[i]);
            //store.impose(cumConstraint);
        }
        
        takeFromMagazineConstraint();
    }
    
    public Tuple<int[][], Store, String> search()
    {
        System.out.println("core.CLPTask.search()");
        boolean result = store.consistency();
        //SelectChoicePoint select = new SimpleSelect(vars, new SmallestDomain(), new IndomainMin());
        Search labelSlave = new DepthFirstSearch();
        SelectChoicePoint selectSlave = 
                new SimpleSelect(varsY, new SmallestMin(), new SmallestDomain(), new IndomainMin());
        labelSlave.setSelectChoicePoint(selectSlave);
        
        Search labelMaster = new DepthFirstSearch();
        SelectChoicePoint selectMaster = 
                new SimpleSelect(varsX, new SmallestMin(), new SmallestDomain(), new IndomainMin());
        labelMaster.setSelectChoicePoint(selectMaster);
        labelMaster.addChildSearch(labelSlave);
        
        if (energyOptimizationSelected)
        {
            //Branch and Bound
            labelMaster.setTimeOut(100);
            result = labelMaster.labeling(store, selectMaster, energySum);
        }
        else
        {
            result = labelMaster.labeling(store, selectMaster);            
        }
        
        int[][] Tab = new int[4][varsX.length];
        
        if (result) {
            //Print to console, but also find max x coordinate
            System.out.print("Positions of rectangles : (");
            
            maxXCoord = 0;
            for (int i = 0; i < varsX.length; i++) {
                if (maxXCoord < varsX[i].value())
                {
                    maxXCoord = varsX[i].value();
                }
                
                if (i < varsX.length -1)
                        System.out.print("(" + varsX[i] + ", " + varsY[i] + "), ");
                else
                        System.out.print("(" + varsX[i] + ", " + varsY[i] + ")");

                Tab[0][i] = varsX[i].value();
                Tab[1][i] = varsY[i].value();
            }
            System.out.println(")");
            
            int cc = 0;
            for(int i=0; i < 5; ++i)
            {
                for(int j=0; j < numTasks[i]; ++j)
                {
                    Tab[2][cc] = tasksDuration[i].value();
                    Tab[3][cc] = i;
                    cc++;
                }
            }
            /*Tab[2] = numTasks;
            for (int i=1; i < tasksDuration.length; ++i)
            {
                Tab[3][i] = tasksDuration[i].value();
            }*/
            
            
            Tuple resultTuple = new Tuple(Tab, store, labelMaster.toString());
            return resultTuple;
        }
        Tuple resultTuple = new Tuple(null, store, null);
        return resultTuple;
    }
    
    
    public static String buildResultString(Tuple2<int[][], Store> resultTuple)
    {
        int[][] result = resultTuple.x;
        Store store = resultTuple.y;
        
        int[] varsX = new int[result[0].length];
        int[] varsY = new int[result[0].length];
        for (int i = 0; i < varsX.length; i++)
        {
            varsX[i] = result[0][i];
            varsY[i] = result[1][i];
        }
        String text = "Positions of rectangles : (";

        for (int i = 0; i < varsX.length; i++) {
                if (i < varsX.length -1)
                        text += "(" + String.valueOf(varsX[i]) + ", " + String.valueOf(varsY[i]) + "), ";
                else
                        text += "(" + String.valueOf(varsX[i]) + ", " + String.valueOf(varsY[i]) + ")";
        }
        text += "\n" + store.toString();
        text += "\nEnergySum = " + String.valueOf(energySum.value());
        text += "\nMax X coord = " + String.valueOf(maxXCoord + 
                tasksDurationsList[tasksDurationsList.length-1].value());
        
        return text;
    }
}
