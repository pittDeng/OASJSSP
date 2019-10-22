package evo;

import milp.Parameter;
import pro.Problem;
import pro.ProblemGenerator;

import java.util.*;

public class WWO extends OA{
    public Solution [] solutions;
    public Solution best;
    public int popSize;
    public int acceptNum;
    public int iterNum;
    // The following static parameter is initialized in the following static code block;
    public static Problem problem;
    public static int orderNum;
    public static int jobNum;
    public static int machineNum;
    public static Random random;
    public static int solLength;
    public static int [] MIN_PROCESS_TIME;
    public static int insertTime=5;
    public static int swapTime=5;
    public static int indexIter=0;
    static {
        problem= (Problem) ProblemGenerator.readObject(Parameter.OASName);
        orderNum=problem.order.length;
        jobNum=problem.jobNum;
        machineNum=problem.machineNum;
        random=new Random();
        solLength=orderNum*machineNum;

        MIN_PROCESS_TIME=new int[machineNum];
        for(int i=0;i<problem.assignMatrix.length;++i){
            for(int j=0;j<problem.assignMatrix[i].length;++j){
                if(MIN_PROCESS_TIME[problem.assignMatrix[i][j]]==0){
                    MIN_PROCESS_TIME[problem.assignMatrix[i][j]]=problem.timeMatrix[i][j];
                    continue;
                }
                MIN_PROCESS_TIME[problem.assignMatrix[i][j]]=
                        problem.timeMatrix[i][j]<MIN_PROCESS_TIME[problem.assignMatrix[i][j]]?problem.timeMatrix[i][j]:MIN_PROCESS_TIME[problem.assignMatrix[i][j]];
            }
        }
    }
    public WWO(int acceptNum,int popSize,int iterNum){
        this.acceptNum=acceptNum;
        this.popSize=popSize;
        this.iterNum=iterNum;
    }

    public WWO(){
        this.popSize=Parameter.popSize;
        this.iterNum=Parameter.iterNum;
    }

    public static class Solution implements Cloneable {
        public boolean [] accept;
        public int [] sol;
        public int height;
        public double value;
        private Solution(boolean [] accept,int [] sol){
            this.accept=accept;
            this.sol=sol;
            this.height=Parameter.height;
            this.value=decode(accept,sol);
        }
        public Solution(){

        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            int []tempsol=Arrays.copyOf(this.sol,this.sol.length);
            boolean []tempaccept=Arrays.copyOf(this.accept,this.accept.length);
            return new Solution(tempaccept,tempsol);
        }

        /**
         *  Initialize a total random solution based on the
         * @param numberOfAccept the number of the accepted orders;
         * @return
         */
        public static Solution initOne(int numberOfAccept){
            boolean []accept=new boolean[orderNum];
            int []sol=new int[solLength];
            for (int i=0;i<numberOfAccept;++i){
                accept[i]=true;
            }
            Operator.swap(accept,numberOfAccept);
            for(int i=0;i<solLength;++i){
                sol[i]=i%orderNum;
            }
            Operator.swap(sol,solLength);
            return new Solution(accept,sol);
        }
    }

    public void initialize(){
        solutions=new Solution[popSize];
        for(int i=0;i<popSize;++i){
            solutions[i]=Solution.initOne(acceptNum);
        }
        Arrays.sort(solutions, new Comparator<Solution>() {
            @Override
            public int compare(Solution o1, Solution o2) {
                if(o1.value<o2.value){
                    return 1;
                }else if(o1.value==o2.value){
                    return 0;
                }else
                    return -1;
            }
        });
        try{
            best=(Solution) solutions[0].clone();
        }catch (CloneNotSupportedException e){
            throw new RuntimeException();
        }

    }
    @Override
    public void OneIteration() {
        for (int i=0;i<popSize;++i){
            double ratio=random.nextDouble();
            Solution crossSolution;
            if(ratio>=0.5){
                crossSolution=best;
            }else{
                crossSolution=solutions[(int)(ratio*2*solutions.length)];
            }
            if(crossSolution!=solutions[i]){
                int [] tempc=Operator.crossover(crossSolution.sol,solutions[i].sol,orderNum);
                double tempValue;
                if(betterThan(tempValue=decode(solutions[i].accept,tempc),solutions[i].value)){
                    solutions[i].sol=tempc;
                    solutions[i].value=tempValue;
                    solutions[i].height=Parameter.height;
                }
            }

            if(propagate(solutions[i])){
                if(betterThan(solutions[i].value,best.value)) {
                    Solution temp = best;
                    best = solutions[i];
                    solutions[i] = temp;
                }
            }
            if(solutions[i].height<0){
                solutions[i]=Solution.initOne(acceptNum);
            }

        }
    }

    @Override
    public void prego() {
        this.initialize();
    }

    /**
     * You can override the method to apply this class to solve the minimization problem.
     * @param o1
     * @param o2
     * @return
     */
    public static boolean betterThan(double o1,double o2){
            return o1>o2;
    }

    public static boolean propagate(Solution solution){
        int [] sol;
        if(random.nextDouble()<0.5){
            sol=Operator.swap(solution.sol,swapTime,true);
        }
        else{
            sol=Operator.insertByTimes(solution.sol,insertTime);
        }
        double temp=decode(solution.accept,sol);
        if(betterThan(temp,solution.value)) {
            solution.sol = sol;
            solution.value = temp;
            solution.height=Parameter.height;
            return true;
        }else{
            solution.height-=1;
            return false;
        }
    }



    public static class Gap{
        int begin;
        int end;
        public Gap(int begin,int end){
            this.begin=begin;
            this.end=end;
        }
    }

    public static double decode(boolean [] accept,int []sol){
        double res=0.0;
        int [] orderTime=new int[orderNum];
        int [] machineTime=new int[machineNum];
        int [] index=new int[orderNum];
        List<List<Gap>> gaps=new ArrayList<>();

        for(int i=0;i<machineNum;++i)
            gaps.add(new ArrayList<>());

        SOL:for (int i=0;i<solLength;++i){
            if(accept[sol[i]]){
                int jobIndex=problem.order[sol[i]];
                int ordinal=(index[sol[i]]++);
                int p=problem.timeMatrix[jobIndex][ordinal];
                int machineIndex=problem.assignMatrix[jobIndex][ordinal];

                for(int j=0;j<gaps.get(machineIndex).size();++j){
                    Gap gap=gaps.get(machineIndex).get(j);
                    int before;
                    int after;
                    if((before=orderTime[sol[i]]-gap.begin)>=0&&(after=gap.end-orderTime[sol[i]]-p)>=0){
                        orderTime[sol[i]]+=p;
                        gaps.get(machineIndex).remove(j);
                        if(before>=MIN_PROCESS_TIME[machineIndex]){
                            gaps.get(machineIndex).add(new Gap(gap.begin,gap.begin+before));
                        }
                        if(after>=MIN_PROCESS_TIME[machineIndex]){
                            gaps.get(machineIndex).add(new Gap(orderTime[sol[i]],gap.end));
                        }
                        continue SOL;
                    }

                }

                if(orderTime[sol[i]]>machineTime[machineIndex]){
                    gaps.get(machineIndex).add(new Gap(machineTime[machineIndex],orderTime[sol[i]]));
                    orderTime[sol[i]]+=p;
                    machineTime[machineIndex]=orderTime[sol[i]];
                }else{
                    machineTime[machineIndex]+=p;
                    orderTime[sol[i]]=machineTime[machineIndex];
                }
            }
        }
        for(int i=0;i<accept.length;++i){
            if(accept[i]){
                res+=problem.profit[i];
                if(orderTime[i]>problem.dueDate[i]){
                    res-=(problem.delayWeight[i]*(orderTime[i]-problem.dueDate[i]));
                }
            }
        }
        return res;
    }

    /**
     * only invoked by Islands.exchangeInfo() with Reflection invoking
     * @param islands
     */
    public static void exchangeInfo(WWO[]islands, Islands.AThread[] threads){
//        Arrays.sort(islands, new Comparator<WWO>() {
//            @Override
//            public int compare(WWO o1, WWO o2) {
//                if(betterThan(o1.best.value,o2.best.value)){
//                    return 1;
//                }else return -1;
//            }
//        });
//        StringBuilder sb=new StringBuilder();
//        for(int i=0;i<islands.length;++i){
//            sb.append(islands[i].acceptNum);
//            sb.append(" ");
//            sb.append(islands[i].best.value);
//            sb.append(" ");
//        }
//        System.out.println(sb);
        //print the best value information.
        printInfo(islands,threads);

        if(islands[0].acceptNum+islands.length-1<islands[islands.length-1].acceptNum){
           findBestAcceptNum(islands,threads);
        }
        // if the difference of the maximum acceptNum and minimum acceptNum is less than or equal the islands.length-1,
        // which represent that there are two islands has the same acceptNum, just find the best acceptNum and put all the
        //acceptNum as the best acceptNum;
        else if(islands[0].acceptNum!=islands[islands.length-1].acceptNum){
            int tIndex=0;
            for(int i=1;i<islands.length;++i){
                if (betterThan(islands[i].best.value,islands[tIndex].best.value)){
                    tIndex=i;
                }
            }
            for(int i=0;i<islands.length;++i){
                if(i==tIndex)continue;
                islands[i]=new WWO();
                islands[i].acceptNum=islands[tIndex].acceptNum;
                islands[i].prego();
                threads[i].oa=islands[i];
            }
            Islands.exchangeGap=Parameter.lateExchangeGap;
        }
        else{
            immigrant(islands,threads);
        }

    }

    public static void immigrant(WWO[]islands, Islands.AThread[] threads){
        for(int i=0;i<islands.length;++i){
            int crossOverNum=random.nextInt(islands[i].popSize);
            for(int j=0;j<crossOverNum;++j){
                int bestIndex=random.nextInt(islands.length);
                int solIndex=random.nextInt(islands[i].popSize);
                Solution bestSolution=islands[bestIndex].best;
                Solution currentSolution=islands[i].solutions[solIndex];
                int [] copyc=Operator.crossover(bestSolution.sol,currentSolution.sol,orderNum);
                double betterValue;
                if(betterThan((betterValue=decode(bestSolution.accept,copyc)),currentSolution.value)){
                    currentSolution.sol=copyc;
                    currentSolution.value=betterValue;
                    currentSolution.height=Parameter.height;
                }
            }
        }
    }

    public static void findBestAcceptNum(WWO[]islands, Islands.AThread[] threads){
        if(islands.length<5){
            throw new RuntimeException();
        }
        Integer [] cardinal=new Integer[islands.length];
        for(int i=0;i<cardinal.length;++i){
            cardinal[i]=i;
        }
        Arrays.sort(cardinal, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if(betterThan(islands[o1].best.value,islands[o2].best.value))
                    return -1;
                else if(islands[o1].best.value==islands[o2].best.value)
                    return 0;
                else
                    return 1;
            }
        });
        Arrays.sort(cardinal,0,3);
        int first=cardinal[0];
        int second=cardinal[2];
        int firstNum=(islands[first].acceptNum<<1)-islands[first+1].acceptNum;
        firstNum=firstNum<0?0:firstNum;
        int secondNum=(islands[second].acceptNum<<1)-islands[second-1].acceptNum;
        secondNum=secondNum>orderNum?orderNum:secondNum;
        for(int i=0;i<islands.length;++i){
            islands[i]=new WWO();
            islands[i].acceptNum=firstNum+(int)((secondNum-firstNum)*(double)(i+1)/islands.length);
            islands[i].prego();
            threads[i].oa=islands[i];
        }

    }

    //print the best value information of all the islands.
    public static void printInfo(WWO [] islands,Islands.AThread[] threads){
        StringBuilder sb=new StringBuilder();
        sb.append(String.format("%d th iteration, the total number of the orders is %d\n",indexIter+=Islands.exchangeGap,WWO.orderNum));
        for(int i=0;i<islands.length;++i){
            sb.append(String.format("%dth thread, ",i));
            sb.append(String.format("Accepted Number is %d  ",islands[i].acceptNum));
            sb.append(String.format("the best value is %f\n",islands[i].best.value));
        }
        System.out.println(sb.toString());
    }


    public static void main(String [] args){
        WWO wwo=new WWO(9,50,10000);
        wwo.initialize();
        for(int i=0;i<wwo.iterNum;++i){
            System.out.print(String.format("%d th ",i));
            wwo.OneIteration();
            System.out.println(String.format("the best value is %f\n",wwo.best.value));
        }
    }
}
