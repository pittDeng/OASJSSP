package milp;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.CpxAndGoal;
import ilog.cplex.CpxBranchAsCplex;
import ilog.cplex.CpxSolutionGoal;
import ilog.cplex.IloCplex;
import pro.Problem;

import java.awt.*;

/**
 * This is the model with the max delay constraints
 * The class will rewrite the method {@code delayConstraint()};
 */
public class ModelWithMDC extends ModelOri{
    double [] maxComplete=null;
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
