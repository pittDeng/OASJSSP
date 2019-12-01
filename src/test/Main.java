package test;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static <T> T[] hello(Class<T> klass) {
        return (T [])Array.newInstance(klass,3);
    }
//    public static void main(String[] args) throws IOException {
//        File file=new File("data/data1.txt");
//        System.out.println(file.getParentFile().toString());
//        if(!file.getParentFile().exists()){
//            System.out.println("what");
//            file.getParentFile().mkdir();
//        }
//        if (!file.exists())
//            file.createNewFile();
//        FileWriter writer=new FileWriter(file);
//        BufferedWriter bos=new BufferedWriter(writer);
//        bos.write("Hello World");
//        bos.flush();
//        bos.close();
//        writer.close();
//    }
public static void main(String[] args) throws InstantiationException, IllegalAccessException {
    System.out.println(hello(Object.class));
    ArrayList<Integer> a=new ArrayList<>();
    for (int i=0;i<100;++i){
        a.add(i);
    }
    Iterator<Integer> iterator = a.iterator();
    //a.add(2);
    iterator.next();
}
}
