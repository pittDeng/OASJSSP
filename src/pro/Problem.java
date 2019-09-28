package pro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The class Problem can represent the OAS problem and it is serializable, which can be imported from a file.
 */
public class Problem implements Serializable {
    public Random random=new Random();
    // This kind of definition has been abandoned.
   // public final static double DUE_DATE_RATIO=1.5;
    public int []order;
    public int [] profit;
    public double [] delayWeight;
    public int machineNum;
    public int jobNum;
    public int []dueDate;
    public int [][]assignMatrix;
    public int [][]timeMatrix;
    public List<List<Operation>>occupiedMachine;
    /**
     * This class can represent a individual operation in the problem.
     * The class contains two fields which can represent the operation.
     */
    public class Operation implements Serializable{
        //{@code orderIndex}represents the job, {@code opIndex}represents the index for the oeration
        public int orderIndex;
        public int opIndex;
        
        public Operation(int orderIndex,int opIndex){
            this.orderIndex=orderIndex;
            this.opIndex=opIndex;
        }
    }

    /**
     * This function calculate the due date of the order based on the total processing time of this order
     * We suppose that the due date is the total processing time multiply 2
     */
    public void initDueDateAndProfitAndWeighted(){
        //In the beginning, I define the DUE_DATE_RATIO as a constant for all problem, but I reckon that
        //maybe this new method will increase the profit.
        double DUE_DATE_RATIO=(double)order.length/machineNum;
        dueDate=new int[order.length];
        profit=new int [order.length];
        delayWeight=new double[order.length];
        for(int i=0;i<order.length;++i){
            for(int j=0;j<timeMatrix[order[i]].length;++j){
                dueDate[i]+=timeMatrix[order[i]][j];
            }

        //Initialize the profit of the order i as following method to let it locate between 0.5*dueDate[i] and 1.5*dueDate[i]
            profit[i]=dueDate[i]/2+random.nextInt(dueDate[i]);
        //Initialize the dueDate as follows.
            dueDate[i]*=DUE_DATE_RATIO;
            //Initialize the weighted delay penalty coefficients.
            delayWeight[i]=0.1;
        }
    }
    public void calculateOccupiedMachine(){
        //Initialize the variable occupiedMachine;
        occupiedMachine=new ArrayList<>(machineNum);
        for(int i=0;i<machineNum;++i)
            occupiedMachine.add(new ArrayList<>());
        
        //add item to the arrayList
        for(int orderIndex=0;orderIndex<order.length;++orderIndex){
            for(int opIndex=0;opIndex<assignMatrix[order[orderIndex]].length;++opIndex){
                int maIndex=assignMatrix[order[orderIndex]][opIndex];
                occupiedMachine.get(maIndex).add(new Operation(orderIndex,opIndex));
            }
        }
    }
}
