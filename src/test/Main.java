package test;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        File file=new File("data/data1.txt");
        System.out.println(file.getParentFile().toString());
        if(!file.getParentFile().exists()){
            System.out.println("what");
            file.getParentFile().mkdir();
        }
        if (!file.exists())
            file.createNewFile();
        FileWriter writer=new FileWriter(file);
        BufferedWriter bos=new BufferedWriter(writer);
        bos.write("Hello World");
        bos.flush();
        bos.close();
        writer.close();
    }
}
