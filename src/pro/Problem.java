package pro;

import java.io.Serializable;
import java.util.*;

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
    public List<List<Integer>>sameJobOrder;
    //the profitByTime as the profit of one unit time for this order
    public Double[] profitByTime;
    //the rank of the order based on profit in one unit time
    public List<Integer>profitOrder;
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
        //double DUE_DATE_RATIO=(double)order.length/machineNum;
        double DUE_DATE_RATIO=1.5;
        dueDate=new int[order.length];
        profit=new int [order.length];
        delayWeight=new double[order.length];
        sameJobOrder=new ArrayList<>(jobNum);

        //The array will be useful in setting goals in the optimization.
        profitByTime=new Double[order.length];
        //Just record the index of the order;
        profitOrder=new ArrayList<>();
        for(int i=0;i<jobNum;++i){
            sameJobOrder.add(new ArrayList<>());
        }
        for(int i=0;i<order.length;++i){
            for(int j=0;j<timeMatrix[order[i]].length;++j){
                dueDate[i]+=timeMatrix[order[i]][j];
            }
        //Initialize the profit of the order i as following method to let it locate between 0.5*dueDate[i] and 1.5*dueDate[i]
            profit[i]=dueDate[i]/2+random.nextInt(dueDate[i]);
            //Initialize the profitByTime as the profit of one unit time for this order.
            profitByTime[i]=(double)profit[i]/dueDate[i];
            //Just record the index of the order;
            profitOrder.add(i);
            //Initialize the sameJobOrder
            sameJobOrder.get(order[i]).add(i);
        //Initialize the dueDate as follows.
            dueDate[i]*=DUE_DATE_RATIO;
            //Initialize the weighted delay penalty coefficients.
            delayWeight[i]=1;
        }



        //Sort the {@code sameJobOrder} for each job
        for(int i=0;i<sameJobOrder.size();++i){
            sameJobOrder.get(i).sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return profit[o2]-profit[o1];
                }
            });
        }



        //Sort the {@code profitOrder} based on their profitByTime
        profitOrder.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return (int)Math.floor(profitByTime[o2]-profitByTime[o1]);
            }
        });
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
