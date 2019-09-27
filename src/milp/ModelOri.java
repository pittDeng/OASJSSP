package milp;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import pro.Problem;
import pro.ProblemGenerator;

import javax.swing.*;

public class ModelOri {
    public static Problem problem;
    public IloCplex cplex;
    /**
     *
     * @param fileName the file store the problem
     */
    public ModelOri(String fileName){
        //load the problem, terminate the program if some failure occurs in the procession.
        problem=(Problem) ProblemGenerator.readObject(fileName);
        assert problem!=null;
    }
    public void setModel(){
        cplex=new IloCplex();
    }
    public void setVariables() throws IloException{
        IloNumVar []accept=cplex.boolVarArray(problem.order.length);
        IloNumVar [][]complete=new IloNumVar[problem.order.length][problem.machineNum];
        IloNumVar [][]preceed=new
    }
    public static void exampleTest(){
        try{
            IloCplex cplex=new IloCplex();
            IloNumVar[]x=cplex.numVarArray(2,0,1);
            double [] coeff={1.0,1.0};
            cplex.addMaximize(cplex.scalProd(coeff,x));
            double []row1={1.0,2.0};
            double []row2={2.0,1.0};
            cplex.addLe(cplex.scalProd(row1,x),2);
            cplex.addLe(cplex.scalProd(row2,x),2);

            if (cplex.solve()) {
                cplex.output().println("Solution status = " + cplex.getStatus());
                cplex.output().println("Solution value = " + cplex.getObjValue());
                double[] val = cplex.getValues(x);
                for (int j = 0; j < val.length; j++)
                    cplex.output().println("x" + (j+1) + "  = " + val[j]);
            }
            cplex.end();
        }catch (IloException e){
            e.printStackTrace();
        }
    }
    public static void main(String [] args){


    }
}
