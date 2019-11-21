package milp;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.Comparator;


/**
 * This is the model with the max delay constraints
 * The class will rewrite the method {@code delayConstraint()};
 */
public class ModelWithMDC extends ModelOri{
    //represents the maximum complete time if want to get some profits
    double [] maxComplete=null;
    //represents the maximum allowed accepted orders
    int maxAccepted;

    double maxProfit;

    int [] orderTime=null;

    public static class SubProblem{
        int [] orderTime=null;
        double [] maxComplete=null;
        IloCplex subCplex=null;
        IloNumVar [] subComplete=null;
        IloNumVar [] subAccept=null;
        IloNumVar [] subDelay=null;
        IloNumVar [][] alpha=null;
        final int BIG_M=100000;

        public SubProblem(int [] orderTime,double [] maxComplete){
            this.orderTime=orderTime;
            this.maxComplete=maxComplete;
        }

        public void setModel() {
            try{
                subCplex=new IloCplex();
                setVariables();
                setConstraint();
                setObjective();
            }catch (IloException e){
                e.printStackTrace();
            }

        }
        public void setVariables() throws IloException {
            subComplete=subCplex.numVarArray(problem.order.length,new double[problem.order.length],maxComplete);
            subAccept=subCplex.boolVarArray(problem.order.length);
            subDelay=subCplex.numVarArray(problem.order.length,0,BIG_M);
            alpha=new IloNumVar[problem.order.length][];
            for(int i=0;i<problem.order.length;++i){
                alpha[i]=subCplex.boolVarArray(problem.order.length);
            }
        }
        public void setConstraint() throws IloException {
            for(int i=0;i<problem.order.length-1;++i){
                for(int j=i+1;j<problem.order.length;++j){
                    IloNumExpr exprL=subCplex.sum(subComplete[i],subCplex.prod(BIG_M,subCplex.diff(1,alpha[i][j])));
                    IloNumExpr exprR=subCplex.sum(subComplete[j],subCplex.prod(subAccept[i],(double)orderTime[i]/problem.machineNum));
                    subCplex.addGe(exprL,exprR);

                    //pairwise
                    exprL=subCplex.sum(subComplete[j],subCplex.prod(BIG_M,subCplex.diff(1,alpha[j][i])));
                    exprR=subCplex.sum(subComplete[i],subCplex.prod(subAccept[j],(double)orderTime[j]/problem.machineNum));
                    subCplex.addGe(exprL,exprR);

                    subCplex.addEq(subCplex.sum(alpha[i][j],alpha[j][i]),1);
                }
                subCplex.addGe(subDelay[i],subCplex.diff(subComplete[i],problem.dueDate[i]));
            }
        }
        public void setObjective() throws IloException {
//            IloNumExpr obj=subCplex.diff(subCplex.prod(subAccept[0],problem.profit[0]),subCplex.prod(problem.delayWeight[0],subDelay[0]));
//            for(int i=1;i<problem.order.length;++i){
//
//                obj=subCplex.sum(obj,subCplex.diff(subCplex.prod(subAccept[i],problem.profit[i]),subCplex.prod(problem.delayWeight[i],subDelay[i])));
//            }
//            subCplex.addMaximize(obj);
            IloNumExpr obj=subAccept[0];
            for(int i=1;i<problem.order.length;++i){
                obj=subCplex.sum(obj,subAccept[i]);
            }
            subCplex.addMaximize(obj);
        }

        public void solveSubProblem(){
            try{
                subCplex.setParam(IloCplex.DoubleParam.TimeLimit,3000);
                if(subCplex.solve()){
                    subCplex.output().println("Solution status = " + subCplex.getStatus());
                    subCplex.output().println("Solution value = " + subCplex.getObjValue());
                    double [] d=subCplex.getValues(this.subAccept);
                    double [] delay=subCplex.getValues(this.subDelay);
                    double [] complete=subCplex.getValues(this.subComplete);
                    int num=0;
                    double profit=0;
                    for(int i=0;i<d.length;++i){
                        if(d[i]>0.5)++num;
                        profit+=problem.profit[i];
                        System.out.println("accept: "+d[i]+" "+"delay: "+delay[i]+" completed "+complete[i]+" orderTime "+orderTime[i]);
                    }
                    System.out.println("total Accepted"+num);
                    System.out.println("total profit"+profit);
                }
            }catch (IloException e){
                e.printStackTrace();
            }
            subCplex.end();
        }
    }






    public void calculateOrderTime(){
        orderTime=new int[problem.order.length];

        for(int i=0;i<orderTime.length;++i){
            for(int j=0;j<problem.machineNum;++j){
                orderTime[i]+=problem.timeMatrix[problem.order[i]][j];
            }
        }
    }

    //考虑是否可以做一个解决这个问题的数学规划，这样内容可以更加饱满一点。
    public void calculateMaxAccepted(){
        calculateOrderTime();

        double maxCompleteForAll=0;
        for(double item:maxComplete){
            if(item>maxCompleteForAll)
                maxCompleteForAll=item;
        }

        Integer [] index=new Integer[problem.order.length];
        for(int i=0;i<index.length;++i){
            index[i]=i;
        }
        Arrays.sort(index, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if(maxComplete[o1]<maxComplete[o2])
                    return -1;
                else if(maxComplete[o1]==maxComplete[o2])
                    return 0;
                else
                    return 1;
            }
        });
        double time=0;
        maxAccepted=index.length;
        for(int i=0;i<index.length;++i){
            time+=((double)orderTime[index[i]]/problem.machineNum);
            if(time>maxComplete[index[i]]){
                --maxAccepted;
            }
        }

        System.out.println(maxAccepted);


    }

    public void MaximumProfit(){

        double profit=0;
        double time=0.0;
        int sum=problem.order.length;
        int machineNum=problem.machineNum;
        for(int i=0;i<problem.profitOrder.size();++i){
            int orderIndex=problem.profitOrder.get(i);
            time+=(double)orderTime[orderIndex]/machineNum;
            if(time<problem.dueDate[orderIndex]){
                profit+=problem.profit[orderIndex];
            }else if (time<maxComplete[orderIndex]){
                profit+=(problem.profit[orderIndex]-problem.delayWeight[orderIndex]*(time-problem.dueDate[orderIndex]));
            }else{
                time-=(double)orderTime[orderIndex]/machineNum;
                --sum;
            }
        }
        maxProfit=profit;
        System.out.println(sum);
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
//        calculateMaxAccepted();
//        IloNumExpr acceptNum=accept[0];
//        for(int i=1;i<accept.length;++i){
//            acceptNum=cplex.sum(acceptNum,accept[i]);
//        }
//        cplex.addLe(acceptNum,22);
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

    public static void main(String [] args) throws IloException {
//        solveOAS();
//        ModelWithMDC model=new ModelWithMDC(Parameter.OASName);
        solveOAS();
//        model.calculateOrderTime();
//        model.setModel();
//        SubProblem sp=new SubProblem(model.orderTime,model.maxComplete);
//        sp.setModel();
//        sp.solveSubProblem();
//        model.setModel();
//        model.calculateMaxAccepted();
//        model.MaximumProfit();
//        System.out.println(model.maxProfit);
//        int sum=0;
//        for(int i=0;i<problem.profit.length;++i){
//            sum+=problem.profit[i];
//        }
//        System.out.println(sum);

    }
}
