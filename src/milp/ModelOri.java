package milp;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import pro.Problem;
import pro.ProblemGenerator;
import java.util.ArrayList;
import java.util.List;

public class ModelOri {
    public static Problem problem;
    public IloCplex cplex;
    public IloNumVar [][]complete=null;
    public IloNumVar []accept=null;
    public IloNumVar []delay=null;
    public List<List<PairOperation>> pairs=null;

    //This constant must be adapted later on.
    public static int CONSTANT_M=10000;

    //This constant represent that the delay cannot be excess this bound.
    public static int MAX_DELAY=10000;
    public class PairOperation{
        public Problem.Operation first;
        public Problem.Operation second;
        public IloNumVar preceding;
        public PairOperation(Problem.Operation first,Problem.Operation second) throws IloException{
            this.first=first;
            this.second=second;
            preceding=cplex.boolVar();
        }
    }
    /**
     *
     * @param fileName the file store the problem
     */
    public ModelOri(String fileName){
        //load the problem, terminate the program if some failure occurs in the procession.
        problem=(Problem) ProblemGenerator.readObject(fileName);
        assert problem!=null;
    }

    /**
     * define the model clearly
     */
    public void setModel(){
        try{
            cplex=new IloCplex();
            setVariables();
            oneJobConstraint();
            oneMachineConstraint();
            delayConstraints();
            setObjective();
        }catch (IloException e){
            e.printStackTrace();
        }
    }

    /**
     * This method add the delay constraints of the problem to the model.
     */
    public void delayConstraints()throws IloException{
        delay=cplex.numVarArray(problem.order.length,0,MAX_DELAY);
        for(int i=0;i<delay.length;++i){
            int last=complete[i].length-1;
            cplex.addGe(cplex.diff(delay[i],cplex.diff(complete[i][last],(double)problem.dueDate[i])),0);
        }
    }

    /**
     *  Set the objective as the total profit minus the delay penalty.
     * @throws IloException
     */
    public void setObjective()throws IloException{
        IloNumExpr obj=cplex.diff(cplex.prod((double)problem.profit[0],accept[0]),cplex.prod(problem.delayWeight[0],delay[0]));
        for(int i=1;i<problem.order.length;++i){
            obj=cplex.sum(obj,cplex.diff(cplex.prod((double)problem.profit[i],accept[i]),cplex.prod(problem.delayWeight[i],delay[i])));
        }
        cplex.addMaximize(obj);
    }

    /**
     * set the general variables in this method.
     * @throws IloException
     */
    public void setVariables() throws IloException{
        accept=cplex.boolVarArray(problem.order.length);
        complete=new IloNumVar[problem.order.length][problem.machineNum];
        for(int i=0;i<complete.length;++i){
            complete[i]=cplex.numVarArray(problem.machineNum,0,CONSTANT_M);
        }

    }

    /**
     * add constraints which represents the preceding order of the a single job
     * @throws IloException
     */
    public void oneJobConstraint()throws IloException{
        for(int i=0;i<problem.order.length;++i){
            // add the constraint that ci0 must be greater than pi0
            int pi0=problem.timeMatrix[problem.order[i]][0];
            cplex.addGe(cplex.diff(complete[i][0],cplex.prod(pi0,accept[i])),0);
            for(int j=1;j<problem.machineNum;++j){
                // {@code pij} represents the processing time for operation j of the order i.
                int pij=problem.timeMatrix[problem.order[i]][j];
                IloNumExpr expr=cplex.diff(complete[i][j],cplex.sum(complete[i][j-1],cplex.prod(pij,accept[i])));
                cplex.addGe(expr,0);
            }
        }
    }

    /**
     * add constraints which represent that all operation which is manipulated on an identical machine
     * cannot be processed in the same time.
     */
    public void oneMachineConstraint() throws IloException{
        pairs=new ArrayList<>();
        for(int i=0;i<problem.machineNum;++i){
            pairs.add(new ArrayList<>());
        }
        for(int maIndex=0;maIndex<problem.machineNum;++maIndex){
            for(int firstIndex=0;firstIndex<problem.occupiedMachine.get(maIndex).size()-1;++firstIndex){
                for(int secondIndex=firstIndex+1;secondIndex<problem.occupiedMachine.get(maIndex).size();++secondIndex){
                    Problem.Operation first=problem.occupiedMachine.get(maIndex).get(firstIndex);
                    Problem.Operation second=problem.occupiedMachine.get(maIndex).get(secondIndex);
                    PairOperation pair=new PairOperation(first,second);

                    //ckl represents the completion of the operation first
                    IloNumExpr ckl=complete[first.orderIndex][first.opIndex];
                    //cji represents the completion of the operation second
                    IloNumExpr cji=complete[second.orderIndex][second.opIndex];

                    // the following expression represents the left side of the inequality
                    IloNumExpr iloNumExprL=cplex.sum(cji,cplex.prod(CONSTANT_M,cplex.diff(1,pair.preceding)));
                    // pji represents the time of operation {@code second}
                    int pji=problem.timeMatrix[problem.order[second.orderIndex]][second.opIndex];
                    IloNumExpr iloNumExprR=cplex.sum(ckl,cplex.prod(pji,accept[second.orderIndex]));
                    //add the constraint to the problem
                    cplex.addGe(cplex.diff(iloNumExprL,iloNumExprR),0);

                    //in pairwise
                    int pkl=problem.timeMatrix[problem.order[first.orderIndex]][first.opIndex];
                    IloNumExpr pairWiseL=cplex.sum(ckl,cplex.prod(CONSTANT_M,pair.preceding));
                    IloNumExpr pairWiseR=cplex.sum(cji,cplex.prod(pkl,accept[first.orderIndex]));
                    cplex.addGe(cplex.diff(pairWiseL,pairWiseR),0);
                }
            }
        }
    }
    public void solveModel(){
        try{
            if(cplex.solve()){
                cplex.output().println("Solution status = " + cplex.getStatus());
                cplex.output().println("Solution value = " + cplex.getObjValue());
            }
        }catch (IloException e){
            e.printStackTrace();
        }
        cplex.end();
    }
    public static void solveOAS(){
        ModelOri model=new ModelOri("OAS01");
        model.setModel();
        model.solveModel();
    }
    public static void testJSSP() throws IloException{
        ModelOri model=new ModelOri("p02");
        model.setModel();
        IloNumExpr makespan=model.complete[0][model.problem.machineNum-1];
        for(int i=0;i<model.problem.order.length;++i){
            makespan= model.cplex.max(makespan,model.complete[i][model.problem.machineNum-1]);
            model.cplex.addGe(model.accept[i],1);
        }
        model.cplex.addMinimize(makespan);
        IloCplex cplex=model.cplex;
        if (model.cplex.solve()) {
            System.out.println("---------------------------------------TotalTime-------------------------------");
            System.out.println(cplex.getCplexTime());
            model.cplex.output().println("Solution status = " + model.cplex.getStatus());
            cplex.output().println("Solution value = " + cplex.getObjValue());
            double[][] completeTime = new double[model.problem.jobNum][model.problem.machineNum];
            double [] accept=cplex.getValues(model.accept);
            for(int i=0;i<accept.length;++i){
                cplex.output().println(String.format("acc%d %f",i,accept[i]));
            }
            for (int i = 0; i < completeTime.length; ++i) {
                completeTime[i] = cplex.getValues(model.complete[i]);
                for (int j = 0; j < completeTime[i].length; ++j)
                    cplex.output().println(String.format("c%d%d:%f", i, j, completeTime[i][j]));
            }
        }
        cplex.end();
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
    public static void main(String [] args) throws IloException{
//        Runnable runnable=new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(50000);
//                    System.out.println("50s is over!");
//                    System.exit(-1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        Thread t=new Thread(runnable);
//        t.start();
//        testJSSP();
    solveOAS();
    }
}
