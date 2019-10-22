package evo;

import milp.Parameter;

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
        try {
             fd1=islands[0].getClass().getField("acceptNum");
             fd2=islands[0].getClass().getField("orderNum");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        try {
            Integer orderNum=(Integer) fd2.get(null);
            for(int i=0;i<islandsNumber;){
                fd1.set(islands[i],(int)(orderNum*(double)(++i)/islandsNumber));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        threads=new AThread[islandsNumber];
        for(int i=0;i<islandsNumber;++i){
            threads[i]=new AThread(islands[i],i);
            threads[i].start();
        }

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


    public static void main(String [] args) throws InterruptedException {
        time=System.currentTimeMillis();
        createIslands(WWO.class);
        for(int i=0;i<threads.length;++i){
            threads[i].join();
        }
        System.out.println(System.currentTimeMillis()-time);
    }
}
