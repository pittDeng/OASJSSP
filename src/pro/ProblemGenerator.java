package pro;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

public class ProblemGenerator {
    public static int MAX_ORDER_NUMBER=10;
    public static int MIN_ORDER_NUMBER=MAX_ORDER_NUMBER>>1;
    public static String EXAMPLE_NAME="ft20.txt";
    public static String PROBLEM_FILENAME="p01";
    public static Random random=new Random();

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
//            problem.order[i]=random.nextInt(problem.jobNum);
            problem.order[i]=i;
        }

        //Initialize the due date of the order
        problem.initDueDate();

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
    public static void main(String [] args){
        new ProblemGenerator().generateAProblem("p02",20);
        System.out.println("pause");
    }
}
