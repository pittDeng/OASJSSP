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
public static void main(String[] args) throws  IOException {
//        FileReader fr=new FileReader("temp2.txt");
//        BufferedReader br=new BufferedReader(fr);
//        String temp="";
//        StringBuilder sb=new StringBuilder();
//        while((temp=br.readLine())!=null){
//            System.out.println(temp);
//            sb.append(temp+",");
//        }
//        FileWriter fw=new FileWriter("temp14.txt");
//        fw.write(sb.toString());
//        fw.close();
//        fr.close();
    double a=1.0;
    int b=2;
    System.out.println(String.format("hello%.0f%d",a,b));
}
}
