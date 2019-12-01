package evo;

import data.ToExcel;
import milp.Parameter;
import pro.ProblemGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Islands {
    //store the max iteration number
    public static int maxIterations=Parameter.MAX_ITERATION_NUMBER;
    //store the number of islands,i.e. the number represents how many colony?
    public static int islandsNumber=Parameter.islandsNumber;
    //store how many iteration elapsed before the colony exchange the information with others.
    public static int exchangeGap=Parameter.earlyExchangeGap;
    //store the object that run the algorithm
    public static OA[] islands;
    //store the thread which the algorithm will run on;
    public static AThread [] threads;
    //store the array that represent whether the thread has finished a gap period
    public static int  flags=0;
    //store the lock
    public static Object obj=new Object();
    //store the computing time
    public static long time;
    //define a class to extends the Thread
    public static class AThread extends Thread{
        public OA oa;
        //This variable store the index of the Thread, no greater than threads.length;
        public int index;
        public AThread(OA oa,int index){
            this.oa=oa;
            this.index=index;
        }
        @Override
        public void run() {
            oa.prego();
            int i=0;
            do{
                oa.go(exchangeGap,i);
                i+=exchangeGap;
                //the thread will wait if not all thread has finished. Otherwise, the thread will exchange the information by itself for all threads
                //after the exchanging has been finished, the thread will notify all thread.
                waitAndNotify();
            }while (i<maxIterations);
        }

        /**
         * if all Thread has finished,notify them all. Otherwise,the thread will wait.
         * This method can only be invoked by run
         * What is more important is that the method invoke exchangeInfo(), every thread can exchange their information;
         */
        public static void waitAndNotify(){
            synchronized (obj){
                flags+=1;
                if(flags==islandsNumber){
                    exchangeInfo();
                    obj.notifyAll();
                    flags=0;
                }
                else{
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



    }

    private Islands(OA [] islands,int islandsNumber,int exchangeGap,AThread [] threads){
        this.islands=islands;
        this.islandsNumber=islandsNumber;
        this.exchangeGap=exchangeGap;
        this.threads=threads;
    }

    public void go(){

    }

    public static boolean betterThan(double o1,double o2){
        return o1>o2;
    }


    /**
     * create the islands using the algorithmType
     * @param algorithmType the algorithm class
     */
    public static void createIslands(Class<? extends OA> algorithmType){
        islands= (OA [])Array.newInstance(algorithmType,Parameter.islandsNumber);
        try {
            for(int i=0;i<islandsNumber;++i)
            islands[i]=algorithmType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Field fd1;
        Field fd2;
        Field fd3;
        Field bestfd;
        Field bestValue;
        try {
             fd1=islands[0].getClass().getField("acceptNum");
             fd2=islands[0].getClass().getField("orderNum");
             fd3=islands[0].getClass().getField("machineNum");
             bestfd=islands[0].getClass().getField("best");
             bestValue=bestfd.getType().getField("value");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        try {
            Integer orderNum=(Integer) fd2.get(null);
            Integer machineNum=(Integer)fd3.get(null);
            // if the number of orders is less than number of the islands, let the algorithm start with middleExchangeGap, which is larger than earlyExchangeGap
            if(orderNum<=Parameter.islandsNumber)
                exchangeGap=Parameter.middleExchangeGap;
            int maxOrder=3*machineNum>orderNum?orderNum:3*machineNum;
            int minOrder=0.5*machineNum>orderNum?orderNum:(int)(0.5*machineNum);
            for(int i=0;i<islandsNumber;){
                fd1.set(islands[i],(int)(minOrder+(maxOrder-minOrder)*(double)(++i)/islandsNumber));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        threads=new AThread[islandsNumber];
        for(int i=0;i<islandsNumber;++i){
            threads[i]=new AThread(islands[i],i);
            threads[i].start();
        }
        try {
            for(int i=0;i<islandsNumber;++i){
                threads[i].join();
            }
            double opt=bestValue.getDouble(bestfd.get(islands[0]));
            for(int i=1;i<islands.length;++i){
                double temp;
                if(betterThan(temp=bestValue.getDouble(bestfd.get(islands[i])),opt)){
                    opt=temp;
                }
            }
            System.out.println("The best value is " + opt);
            //save the result of the experiment
            if(Parameter.isToExcel){
                saveResult((int)opt);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * The method is to save the experiment result to excel file
     * @param opt the optimization value the algorithm finds.
     */
    public static void saveResult(int opt){
        //ensure that the file exist. Create the file otherwise.
        try{
            File file=new File(Parameter.excelPath);
            if (!file.getParentFile().exists()){
                file.mkdir();
            }
            if (!file.exists()){
                file.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("这里出错了");
        }
        ToExcel toExcel=new ToExcel(ProblemGenerator.Excel_Name,Parameter.sheetName);
        toExcel.insertDataAfterRow(opt);
        toExcel.save();
    }

    public static void exchangeInfo(){
        Class klass=null;
        if(islands.length>0){
            klass=islands[0].getClass();
            try{
                Method method=klass.getMethod("exchangeInfo",islands.getClass(),AThread[].class);
                method.invoke(null,islands,threads);
            }catch (NoSuchMethodException e){
                e.printStackTrace();
                throw new RuntimeException();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

        }
        else{
            System.out.println("The length of islands is below 1 ");
            System.exit(-1);
        }
    }

    public static void execute5Times(){
        for(int i=0;i<5;++i){
            System.out.println(i + " times elapsed");
            time=System.currentTimeMillis();
            createIslands(WWO.class);
            System.out.println(System.currentTimeMillis()-time);
        }
    }
    public static void main(String [] args) throws InterruptedException {
        //execute the algorithm ten times
        execute5Times();
    }
}
