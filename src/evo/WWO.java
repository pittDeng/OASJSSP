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
    public boolean isLSOnBest=true;
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
            init();
    }
    public static void init(){
        problem= (Problem) ProblemGenerator.readObject();
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
        public boolean isMaxHeight=false;
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

        /**
         * The algorithm before re-init the whole wwo and discard something useful in the old wwo which has a bad acceptNum.
         * So the method helps the algorithm perform better with utilizing the information of the wwo with a bad acceptNum.
         * @param diff
         */
        public void changeAcceptNum(int diff){
            int temp;
            if (diff>0){
                for (;diff>0;--diff){
                    while (accept[temp=random.nextInt(accept.length)]);
                    accept[temp]=true;
                }
            }else{
                for (;diff<0;++diff){
                    while (!accept[temp=random.nextInt(accept.length)]);
                    accept[temp]=false;
                }
            }
            this.value=decode(this.accept,this.sol);

        }
    }
    public void initialize(WWO wwo){
        solutions=new Solution[popSize];
        int temp=random.nextInt((int)(0.5*popSize));
        int i=0;
        for(;i<temp;++i){
            int pos=random.nextInt(popSize);
            try {
                solutions[i]=(Solution)wwo.solutions[pos].clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        for (;i<popSize;++i){
            solutions[i]=Solution.initOne(acceptNum);
        }
        sortTheSolutionAndAssignBest();
    }

    /**
     * function just as the method name
     */
    public void sortTheSolutionAndAssignBest(){
        comparator(solutions);
        try{
            best=(Solution) solutions[0].clone();
        }catch (CloneNotSupportedException e){
            throw new RuntimeException();
        }
    }

    public void initialize(){
        solutions=new Solution[popSize];
        for(int i=0;i<popSize;++i){
            solutions[i]=Solution.initOne(acceptNum);
        }
        sortTheSolutionAndAssignBest();
    }

    /**
     * This method modify the acceptNum of the whole wwo
     * @param acceptNum
     */
    public void changeAcceptNum(int acceptNum){
        int diff=acceptNum-this.acceptNum;
        for (int i=0;i<solutions.length;++i){
            solutions[i].changeAcceptNum(diff);
        }
        this.acceptNum=acceptNum;
    }

    @Override
    public void OneIteration() {
        if (isLSOnBest){
            Operator.Ls(best);
            isLSOnBest=false;
        }
        propagate(best,20);
        comparator(solutions);
        for (int i=0;i<popSize;++i){
            double ratio=random.nextDouble();
            Solution crossSolution;
            if(ratio>=0.8){
                crossSolution=best;
            }else{
                crossSolution=solutions[(int)(ratio*1.25*solutions.length)];
            }
            if(crossSolution!=solutions[i]){
                int [] tempc=Operator.crossover(crossSolution.sol,solutions[i].sol,orderNum);
                boolean []tempacc=Operator.crossOverSameAcceptNum(crossSolution.accept,solutions[i].accept,acceptNum);
                double tempValue;
                if(betterThan(tempValue=decode(tempacc,tempc),solutions[i].value)){
                    solutions[i].sol=tempc;
                    solutions[i].accept=tempacc;
                    solutions[i].value=tempValue;
                    reHeight(solutions[i]);
                    if (betterThan(tempValue,best.value)){
                        Solution temp=best;
                        best=solutions[i];
                        solutions[i]=temp;
                        isLSOnBest=true;
                    }
                }
            }


            if(propagate(solutions[i])&&betterThan(solutions[i].value,best.value)) {
                Solution temp = best;
                best = solutions[i];
                solutions[i] = temp;
                isLSOnBest=true;
            }
            if(solutions[i].height<0){
                solutions[i]=Solution.initOne(acceptNum);
            }

        }
    }
    static int maxHeight=1000;
    public static void comparator(Solution [] solutions) {
        Arrays.sort(solutions, new Comparator<Solution>() {
            @Override
            public int compare(Solution o1, Solution o2) {
                if(o1.value<o2.value){
                    return 1;
                }else if(o1.value==o2.value){
                    return 0;
                }else{
                    return -1;
                }
            }
        });
        int maxHeightSize=Parameter.maxHeightSize;
        List<Integer> list=new ArrayList<>(maxHeightSize);
        for (int i=0,index=0;index<maxHeightSize&&i<solutions.length;++i){

            if (list.indexOf((int)solutions[i].value)<0&&!solutions[i].isMaxHeight){
                solutions[i].isMaxHeight=true;
                solutions[i].height=maxHeight;
                ++index;
                list.add((int)solutions[i].value);
            }
        }
    }


    @Override
    public void prego() {
        this.initialize();
    }

    public void prego(WWO wwo){
        this.initialize(wwo);
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
        return propagate(solution,1);
    }

    public static boolean propagate(Solution solution,int times){
        for (int i=0;i<times;++i){
            int [] sol;
            if(random.nextDouble()<0.5){
                sol=Operator.swap(solution.sol,random.nextInt(swapTime),true);
            }
            else{
                sol=Operator.insertByTimes(solution.sol,random.nextInt(insertTime));
            }
            double temp=decode(solution.accept,sol);
            if(betterThan(temp,solution.value)) {
                solution.sol = sol;
                solution.value = temp;
                reHeight(solution);
                return true;
            }
            boolean [] tempacc= Operator.swap(solution.accept,1,true);
            temp=decode(tempacc,solution.sol);
            if (betterThan(temp,solution.value)){
                solution.accept=tempacc;
                solution.value=temp;
                reHeight(solution);
                return true;
            }
        }
        solution.height-=1;
        return false;
    }



    public static class Gap{
        int begin;
        int end;
        public Gap(int begin,int end){
            this.begin=begin;
            this.end=end;
        }
    }
    public static double decode(Solution solution){
        return decode(solution.accept,solution.sol);
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
    static int discardCounter=0;
    //50 before
    static int MAX_LOOP=20;
    /**
     * only invoked by Islands.exchangeInfo() with Reflection invoking
     * @param islands
     */
    public static void exchangeInfo(WWO[]islands, Islands.AThread[] threads) {
        //print the best value information.
        if(Parameter.isPrint){
            printInfo(islands,threads);
        }


        if(islands[0].acceptNum+islands.length-1<islands[islands.length-1].acceptNum){
           findBestAcceptNum(islands,threads);
        }
        // if the difference of the maximum acceptNum and minimum acceptNum is less than or equal the islands.length-1,
        // which represent that there are two islands has the same acceptNum, just find the best acceptNum and put all the
        //acceptNum as the best acceptNum;
        else if(islands[0].acceptNum+2<islands[islands.length-1].acceptNum){
            Islands.exchangeGap=Parameter.middleExchangeGap;
            final int last;
            last=islands.length-1;
            if ((++discardCounter)==MAX_LOOP&&(discardCounter=0)==0){
                // sort the populations order by their best value
                Arrays.sort(islands, new Comparator<WWO>() {
                    @Override
                    public int compare(WWO o1, WWO o2) {
                        if (betterThan(o1.best.value,o2.best.value)){
                            return -1;
                        }else if (o1.best.value==o2.best.value)
                            return 0;
                        else
                            return 1;
                    }
                });
                System.out.println("Pause");
                //This will discard the last 10 percent islands
                for (int i=last;i>(int)((double)last*0.9);--i){
                    int k=i;
                    while(k>0&&(islands[k].acceptNum>islands[0].acceptNum&&islands[k].acceptNum<=islands[0].acceptNum+2))--k;
                    if(k>(int)((double)last*0.5)){
                        islands[k]=new WWO();
                        int randAcceptIndex=random.nextInt((int)((double)last*0.5));
                        islands[k].acceptNum=islands[randAcceptIndex].acceptNum;
                        islands[k].prego(islands[randAcceptIndex]);
                    }
                    i=k;

                }
                // sort the populations order by their acceptNum;
                Arrays.sort(islands, new Comparator<WWO>() {
                    @Override
                    public int compare(WWO o1, WWO o2) {
                        return o1.acceptNum-o2.acceptNum;
                    }
                });
                //assign the population with the new threads in case of two threads processing the same population;
                for (int i=0;i<islands.length;++i){
                    threads[i].oa=islands[i];
                }
            }
            immigrantAmongDiffAcc(islands,threads);
            //checkCorrect(islands);

            int low=0;
            int high=1;
            while(high<islands.length){
                while(high<islands.length&&islands[high].acceptNum==islands[low].acceptNum)++high;
                if(low<high-1){
                    immigrant(islands,threads,low,high);
                }
                low=high;
            }
            //checkCorrect(islands);
//            if (islands[0].acceptNum==islands[last].acceptNum){
//                Islands.exchangeGap=Parameter.lateExchangeGap;
//            }


        }
        else if(islands[0].acceptNum!=islands[islands.length-1].acceptNum){
            immigrantAmongDiffAcc(islands,threads);
            //checkCorrect(islands);

            int low=0;
            int high=1;
            while(high<islands.length){
                while(high<islands.length&&islands[high].acceptNum==islands[low].acceptNum)++high;
                if(low<high-1){
                    immigrant(islands,threads,low,high);
                }
                low=high;
            }
        }
        else{
            immigrant(islands,threads);
        }

    }
    public static void checkCorrect(WWO [] islands){
        for (int i=0;i<islands.length;++i){
            Solution [] solutions=islands[i].solutions;
            for (int j=0;j<solutions.length;++j){
                int temp=0;
                for (int k=0;k<solutions[j].accept.length;++k){
                    if (solutions[j].accept[k])++temp;
                }
                if (temp!=islands[i].acceptNum)
                    throw new RuntimeException();

            }
        }
    }
    /**
     * only invoked by {@code exchangeInfo(WWO[]islands, Islands.AThread[] threads)};
     * @param islands
     * @param threads
     */
    public static void immigrantAmongDiffAcc(WWO [] islands,Islands.AThread[] threads) {
        for (int i = 0; i < islands.length; ++i) {
                int nextIsland;
                if (i == 0)
                    nextIsland = 1;
                else if (i == islands.length - 1)
                    nextIsland = islands.length - 2;
                else
                    nextIsland = random.nextDouble() < 0.5 ? i - 1 : i + 1;
                int nextPos = random.nextInt(islands[nextIsland].solutions.length);
                System.arraycopy(islands[i].best.sol,0,islands[nextIsland].solutions[nextPos].sol,0,islands[i].best.sol.length);
                islands[nextIsland].solutions[nextPos].accept=Operator.copyAcceptBetweenDiffAccNum(islands[i].best.accept,islands[i].acceptNum,islands[nextIsland].acceptNum);
                reHeight(islands[nextIsland].solutions[nextPos]);
                islands[nextIsland].solutions[nextPos].value=decode(islands[nextIsland].solutions[nextPos]);

                int immiNum=random.nextInt((int)(0.2*islands[i].solutions.length));
                for(int j=0;j<immiNum;++j){
                    while((nextIsland=random.nextInt(islands.length))==i);
                    nextPos=random.nextInt(islands[nextIsland].solutions.length);
                    if (betterThan(islands[i].solutions[j].value,islands[nextIsland].solutions[nextPos].value)){
                        System.arraycopy(islands[i].solutions[j].sol,0,islands[nextIsland].solutions[nextPos].sol,0,islands[i].best.sol.length);
                        reHeight(islands[nextIsland].solutions[nextPos]);
                        islands[nextIsland].solutions[nextPos].value=decode(islands[nextIsland].solutions[nextPos]);
                    }
                    while((nextIsland=random.nextInt(islands.length))==i);
                    nextPos=random.nextInt(islands[nextIsland].solutions.length);
                    if (betterThan(islands[i].solutions[j].value,islands[nextIsland].solutions[nextPos].value)){
                        islands[nextIsland].solutions[nextPos].accept=Operator.copyAcceptBetweenDiffAccNum(islands[i].solutions[j].accept,islands[i].acceptNum,islands[nextIsland].acceptNum);
                        reHeight(islands[nextIsland].solutions[nextPos]);
                        islands[nextIsland].solutions[nextPos].value=decode(islands[nextIsland].solutions[nextPos]);
                    }
            }
        }
    }

    public static void reHeight(Solution solution){
        solution.height=Parameter.height;
        solution.isMaxHeight=false;
    }

    public static void immigrant(WWO [] islands,Islands.AThread [] threads){
        immigrant(islands,threads,0,islands.length);
    }



    public static void immigrant(WWO[]islands, Islands.AThread[] threads,int low,int high){
        /*
            Best Solution move to other thread
         */
        for(int i=low;i<high;++i){
            if(random.nextDouble()<Parameter.bestImmiRatio){
                int nextIsland;
                if(i==low)
                    nextIsland=low+1;
                else if(i==high-1)
                    nextIsland=high-2;
                else
                    nextIsland=random.nextDouble()<0.5?i-1:i+1;
                int nextPos=random.nextInt(islands[nextIsland].solutions.length);
                try {
                    islands[nextIsland].solutions[nextPos]=(Solution) islands[i].best.clone();
                    reHeight(islands[nextIsland].solutions[nextPos]);
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException();
                }
            }
            int immiNum=random.nextInt((int)(0.2*islands[i].solutions.length));
            int nextIsland;
            for(int j=0;j<immiNum;++j){
                while((nextIsland=low+random.nextInt(high-low))==i);
                Solution tempSol=islands[i].solutions[j];
                islands[i].solutions[j]=islands[nextIsland].solutions[j];
                islands[nextIsland].solutions[j]=tempSol;
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
        final int temp;
        //temp is 3 originally
        Arrays.sort(cardinal,0,temp=(int)(0.5*(double)islands.length));
        int first=cardinal[0];
        int second=cardinal[temp-1];
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
        if(islands[0].acceptNum+islands.length-1>=islands[islands.length-1].acceptNum){
            Islands.exchangeGap=Parameter.middleExchangeGap;
        }
        // if the all the acceptNum is the same, the program are going to be in late phase.
        else if(islands[islands.length-1].acceptNum==islands[0].acceptNum)
            Islands.exchangeGap=Parameter.lateExchangeGap;
    }

    //print the best value information of all the islands.
    public static void printInfo(WWO [] islands,Islands.AThread[] threads){
        StringBuilder sb=new StringBuilder();
        sb.append(String.format("%d th iteration, the total number of the orders is %d\n",indexIter+=Islands.exchangeGap,WWO.orderNum));
        for(int i=0;i<islands.length;++i){
            sb.append(String.format("%dth thread, ",i));
            sb.append(String.format("Accepted Number is %d  ",islands[i].acceptNum));
            sb.append(String.format("the best value is %f",islands[i].best.value));
            comparator(islands[i].solutions);
            for (int j=0;j<islands[i].popSize;++j){
                sb.append("        "+islands[i].solutions[j].value);
            }
            sb.append("\n");
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
