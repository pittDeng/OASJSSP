package evo;

import milp.Parameter;

import java.lang.reflect.Array;

public class Islands {
    //store the max iteration number
    public static int maxIterations;
    //store the number of islands,i.e. the number represents how many colony?
    public static int islandsNumber;
    //store how many iteration elapsed before the colony exchange the information with others.
    public static int exchangeGap;
    //store the object that run the algorithm
    public static OA[] islands;
    //store the thread which the algorithm will run on;
    public static Thread [] threads;
    //store the array that represent whether the thread has finished a gap period
    public static Integer flags;
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
            int item=0;
            int i=0;
            do{
                do{
                    oa.go(exchangeGap,i);
                }while (++(item)<exchangeGap);
                i+=exchangeGap;
                //the thread will wait if not all thread has finished. Otherwise, the thread will exchange the information by itself for all threads
                //after the exchanging has been finished, the thread will notify all thread.
                waitAndNotify();
            }while (i<Parameter.MAX_ITERATION_NUMBER);
        }

        /**
         * if all Thread has finished,notify them all. Otherwise,the thread will wait.
         * This method can only be invoked by run
         * What is more important is that the method invoke exchangeInfo(), every thread can exchange their information;
         */
        public static void waitAndNotify(){
            synchronized (flags){
                flags+=1;
                if(flags==islandsNumber){
                    exchangeInfo();
                    flags.notifyAll();
                    flags=0;
                }
                else{
                    try {
                        flags.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



    }

    private Islands(OA [] islands,int islandsNumber,int exchangeGap,Thread [] threads){
        this.islands=islands;
        this.islandsNumber=islandsNumber;
        this.exchangeGap=exchangeGap;
        this.threads=threads;
    }
    public void go(){

    }


    public static void createIslands(Class<? extends OA> algorithmType){
        OA [] islands= (OA [])Array.newInstance(algorithmType,Parameter.islandsNumber);
        try {
            for(int i=0;i< Parameter.islandsNumber;++i)
            islands[i]=algorithmType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Thread [] threads=new Thread[Parameter.islandsNumber];
    }
    public static void exchangeInfo(){

    }
}
