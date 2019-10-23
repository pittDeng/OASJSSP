package milp;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;


/**
 * This is the model with the max delay constraints
 * The class will rewrite the method {@code delayConstraint()};
 */
public class ModelWithMDC extends ModelOri{
    //represents the maximum complete time if want to get some profits
    double [] maxComplete=null;
    //represents the maximum allowed accepted orders
    int maxAccepted;

    /**
     * This method is for computing the max number of the accepted orders
     * This method only change the instance value {@code maxAccepted},
     * only invoked in the {@code setVariables} method after the {@code maxComplete}
     * was computed because the {@code maxComplete} will be read in this method.
     */
    public void calculateMaxAccepted(){
        //store every job's consuming time
        int []orderTime=new int[problem.order.length];

        for(int i=0;i<orderTime.length;++i){
            for(int j=0;j<problem.machineNum;++j){
                orderTime[i]+=problem.timeMatrix[problem.order[i]][j];
            }
        }
        //Just for testing the accuracy of the program.
//        int []copyOrderTime= Arrays.copyOf(orderTime,orderTime.length);
        int []machineTime=new int [problem.machineNum];
        maxAccepted=problem.order.length;
        for(int i=0;i<problem.profitOrder.size();++i){
            int oIndex=problem.profitOrder.get(i);
            int jobIndex=problem.order[oIndex];
            for(int opIndex=0;opIndex<problem.assignMatrix[jobIndex].length;++opIndex){
                int machineIndex=problem.assignMatrix[jobIndex][opIndex];
                machineTime[machineIndex]+=problem.timeMatrix[jobIndex][opIndex];
                orderTime[oIndex]-=problem.timeMatrix[jobIndex][opIndex];
                //If find the order must be rejected, delete the time already added in machineTime[machineIndex]
                if(machineTime[machineIndex]+orderTime[oIndex]>maxComplete[oIndex]){
                    for(;opIndex>=0;--opIndex){
                        machineIndex=problem.assignMatrix[jobIndex][opIndex];
                        machineTime[machineIndex]-=problem.timeMatrix[jobIndex][opIndex];
                        orderTime[oIndex]+=problem.timeMatrix[jobIndex][opIndex];
                    }
                    --maxAccepted;
                    if(Math.abs(orderTime[oIndex]*1.5-problem.dueDate[oIndex])>1){
                        System.out.println(orderTime[oIndex]);
                        System.out.println(problem.dueDate[oIndex]);
                        throw new RuntimeException();
                    }
                    break;
                }

            }
        }

        //Test the accuracy of the program, if the program has some error, a RunTimeException() will be thrown.
//        for(int i=0;i<copyOrderTime.length;++i){
//            if(orderTime[i]==0||copyOrderTime[i]==orderTime[i]){
//                continue;
//            }else{
//                throw new RuntimeException();
//            }
//        }
    }

    public ModelWithMDC(String fileName){
        super(fileName);
    }

    @Override
    public void setVariables() throws IloException {
        accept=cplex.boolVarArray(problem.order.length);
        complete=new IloNumVar[problem.order.length][problem.machineNum];
        maxComplete=new double[problem.order.length];
        CONSTANT_M=0;
        for(int i=0;i<complete.length;++i){
            maxComplete[i]=(double)problem.dueDate[i]+problem.profit[i]/problem.delayWeight[i];
            CONSTANT_M=Math.max(CONSTANT_M,(int)Math.ceil(maxComplete[i]));
        }
        for(int i=0;i<complete.length;++i){
            complete[i]=cplex.numVarArray(problem.machineNum,0,CONSTANT_M);
            cplex.addLe(complete[i][complete[i].length-1],maxComplete[i]);
        }

        //make the order priority of the same job based on the profit.
        // make sure that the order with higher profit can be accepted with highest priority.
        for(int i=0;i<problem.sameJobOrder.size();++i){
            for(int j=0;j<problem.sameJobOrder.get(i).size()-1;++j){
                for(int k=j+1;k<problem.sameJobOrder.get(i).size();++k){
                    int firstOrder=problem.sameJobOrder.get(i).get(j);
                    int secondOrder=problem.sameJobOrder.get(i).get(k);
                    cplex.addGe(accept[firstOrder],accept[secondOrder]);
                }
            }
        }
        /*
        the following code is for add a constraint for the max number of the accepted orders.
         */
        calculateMaxAccepted();
        IloNumExpr acceptNum=accept[0];
        for(int i=1;i<accept.length;++i){
            acceptNum=cplex.sum(acceptNum,accept[i]);
        }
        cplex.addLe(acceptNum,maxAccepted);
    }

    /**
     * create a model and solve it
     * just invoked by the {@code main(String [] args)}
     */
    public static void solveOAS(){
        ModelWithMDC model=new ModelWithMDC(Parameter.OASName);
        model.setModel();
        model.solveModel(null);
//        model.solveModel();
    }

    public static void main(String [] args){
        solveOAS();

    }
}
