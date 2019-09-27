package pro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Problem implements Serializable {
    public int []order;
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
    public class Operation{
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
    public void initDueDate(){
        for(int i=0;i<order.length;++i){
            for(int j=0;j<timeMatrix[order[i]].length;++j){
                dueDate[i]+=timeMatrix[order[i]][j];
            }
            dueDate[i]*=2;
        }
    }
    public void calculateOccupiedMachine(){
        //Initialize the variable occupiedMachine;
        occupiedMachine=new ArrayList<>(machineNum);
        for(int i=0;i<machineNum;++i)
            occupiedMachine.set(i,new ArrayList<>());
        
        //add item to the arrayList
        for(int orderIndex=0;orderIndex<order.length;++orderIndex){
            for(int opIndex=0;opIndex<assignMatrix[order[orderIndex]].length;++opIndex){
                int maIndex=assignMatrix[order[orderIndex]][opIndex];
                occupiedMachine.get(maIndex).add(new Operation(orderIndex,opIndex));
            }
        }
    }
}
