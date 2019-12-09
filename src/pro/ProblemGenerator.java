package pro;

import data.ToExcel;
import evo.Islands;
import evo.WWO;
import milp.ModelOri;
import milp.Parameter;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

import static milp.Parameter.OASName;
import static milp.Parameter.fileName;

public class ProblemGenerator {
    public static int MAX_ORDER_NUMBER=10;
    public static int MIN_ORDER_NUMBER=MAX_ORDER_NUMBER>>1;
    public static String EXAMPLE_NAME="la21.txt";
    public static String PROBLEM_FILENAME= OASName;
    public static Random random=new Random();
    public static String Excel_Name=Parameter.excelPath;
    /**
     *
     * @param fileName the fileName of the file which will store the whole problem
     *                 please distinguish it from the fileName of the benchmark problems
     * @return A problem with a random order number that has been generated
     */
    public Problem generateAProblem(String fileName){
        int orderNum=MIN_ORDER_NUMBER+random.nextInt(MAX_ORDER_NUMBER-MIN_ORDER_NUMBER);
        return generateAProblem(fileName,orderNum);
    }

    /**
     *
     * @param fileName the fileName of the file which will store the whole problem
     *      *           please distinguish it from the fileName of the benchmark problems
     * @param orderNum the order number of the problem
     * @return A problem with a fixed order number that has been generated
     */
    public Problem generateAProblem(String fileName, int orderNum){
        Problem problem=new Problem();
        ReadData rd=new ReadData();
        rd.read(EXAMPLE_NAME);

        // Initialize the assignMatrix and timeMatrix based on the benchmark instance.
        problem.assignMatrix=rd.getAssignMatrix();
        problem.timeMatrix=rd.getTimeMatrix();

        //Initialize the machineNum and jobNum based on the benchmark instance.
        problem.machineNum=rd.getCols();
        problem.jobNum=rd.getRows();

        //Initialize the order of the problem.
        problem.order=new int[orderNum];
        for(int i=0;i<orderNum;++i){
            problem.order[i]=random.nextInt(problem.jobNum);
            //There is a order for each job
//            problem.order[i]=i;
        }

        //Initialize the due date and the profit of the order
        //The reason of initializing them together is that they both calculated based on the total processing time of the order.
        //besides,the method also initialize the delay weighted penalty.
        problem.initDueDateAndProfitAndWeighted();

        //calculate the information of the occupied machine;
        problem.calculateOccupiedMachine();

        //Save the problem in the hard disk
        saveObject(problem,fileName);
        return problem;
    }

    public static void saveObject(Object o,String fileName){
        ObjectOutputStream oos=null;
        FileOutputStream fos=null;
        try{
            fos=new FileOutputStream(fileName);
            oos=new ObjectOutputStream(fos);
            oos.writeObject(o);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                oos.close();
                fos.close();;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Object readObject(){
        return readObject(PROBLEM_FILENAME);
    }
    public static Object readObject(String fileName){
        Object o=null;
        ObjectInputStream ois=null;
        FileInputStream fis=null;
        try{
            fis=new FileInputStream(fileName);
            ois=new ObjectInputStream(fis);
            o=ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                ois.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return o;
    }
    public static void test1(){
        //the item number originally is 5
        for (int i=8;i<10;++i){
            int index=i+20;
            PROBLEM_FILENAME="OAS0"+(index);
            Excel_Name="data/exp00"+(index)+".xls";
//            new ProblemGenerator().generateAProblem(PROBLEM_FILENAME,i);
//            ModelOri.solveOAS();
            WWO.init();
            Islands.executeTimes(5);
        }
        for (int i=10;i<=20;i+=5){
            int index=i+20;
            PROBLEM_FILENAME="OAS0"+(index);
            Excel_Name="data/exp00"+(index)+".xls";
//            new ProblemGenerator().generateAProblem(PROBLEM_FILENAME,i);
//            ModelOri.solveOAS();
            WWO.init();
            Islands.executeTimes(5);
        }
    }
    public static void test(String example){
        ToExcel.clearIndex();
        EXAMPLE_NAME=example;
        ToExcel.sheetName=example;
        ToExcel.reinit();
        ToExcel.insertString("OASName");
        ToExcel.insertString("jobNum");
        ToExcel.insertString("Model Opt");
        ToExcel.insertString("Model Gap");
        ToExcel.insertString("Time");
        for (int i=1;i<=10;++i){
            ToExcel.insertString("exp"+i);
        }
        ToExcel.toNextRow();
        for (int i=5;i<=20;){
            if (i<10){
                Islands.maxIterations=30000;
            }else{
                Islands.maxIterations=90000;
            }
            PROBLEM_FILENAME=example.substring(0,example.lastIndexOf(".txt"))+"_"+i;
            System.out.println("------------------------------------------name "+PROBLEM_FILENAME + "   " + i+"--------------------------------");
            ToExcel.insertString(PROBLEM_FILENAME);
            ToExcel.insertData(i);
            new ProblemGenerator().generateAProblem(PROBLEM_FILENAME,i);
            ModelOri.solveOAS();
            WWO.init();
            Islands.executeTimes(10);
            if (i<10)++i;
            else i+=5;
            ToExcel.toNextRow();
        }

    }
    public static void test2(){
        //the item number originally is 5
        for (int i=8;i<10;++i){
            int index=i+20;
            PROBLEM_FILENAME="OAS0"+(index);
            Excel_Name="data/exp00"+(index)+".xls";
//            new ProblemGenerator().generateAProblem(PROBLEM_FILENAME,i);
//            ModelOri.solveOAS();
            WWO.init();
            Islands.executeTimes(5);
        }
        for (int i=10;i<=20;i+=5){
            int index=i+20;
            PROBLEM_FILENAME="OAS0"+(index);
            Excel_Name="data/exp00"+(index)+".xls";
//            new ProblemGenerator().generateAProblem(PROBLEM_FILENAME,i);
//            ModelOri.solveOAS();
            WWO.init();
            Islands.executeTimes(5);
        }
    }
    /**
     * OASName orderNum
     * OAS01    5
     * OAS02    6
     * 3        7
     * 4        8
     * 5        9
     * 6        with profitByTime 8
     * 7        with profitByTime 15
     * 8        5 with profitByTime
     *
     *
     * ft20
     * 9
     * la 21
     *
     * @param args
     */
    public static void main(String [] args){
//        PROBLEM_FILENAME="OAS0"+10;
//        Excel_Name="data/exp00"+10+".xls";
//        new ProblemGenerator().generateAProblem(PROBLEM_FILENAME,10);
//        ModelOri.solveOAS();
//        WWO.init();
//        Islands.execute10Times();

//        int index=40;
        PROBLEM_FILENAME="ft10_20";
//        Excel_Name="data/exp"+(index+1)+".xls";
//        //ModelOri.solveOAS();
        WWO.init();
        Islands.executeTimes(5);
//        test1();
//        test("ft10.txt");
//        test("ft20.txt");
//        test("la21.txt");
//        test("la26.txt");
    }
}
