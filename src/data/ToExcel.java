package data;

import milp.Parameter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import pro.ProblemGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.apache.poi.hssf.record.aggregates.RowRecordsAggregate.createRow;

public class ToExcel {
    static HSSFWorkbook  workbook;
    static HSSFSheet sheet;
    static String  path;
    public static String sheetName;
    public static int rowIndex=0;
    public static int columnIndex=0;
    static{
        path= Parameter.excelPath;
        InputStream is=null;
        workbook=null;
        try{
            is=new FileInputStream(path);
            workbook=new HSSFWorkbook(is);
        }catch (Exception e){
            FileOutputStream fos=null;
            workbook=new HSSFWorkbook();
            try{
                fos=new FileOutputStream(path);
                workbook.write(fos);
                fos.flush();
            }catch (Exception exc){
                exc.printStackTrace();
            }finally {
                try{
                    fos.close();
                }catch (Exception exc){
                    e.printStackTrace();
                }

            }
        }
    }
    public static void reinit(){
        sheet=workbook.getSheet(sheetName);
        if(sheet==null){
            sheet=workbook.createSheet(sheetName);
        }
    }
    public static void insertData(int rowIndex,int columnIndex,int data){
        HSSFRow row=sheet.getRow(rowIndex);
        if(row==null){
            row=sheet.createRow(rowIndex);
        }
        HSSFCell cell=row.createCell(columnIndex);
        cell.setCellValue(data);
    }
    public static void insertData(int data){
        insertData(rowIndex,columnIndex,data);
        columnIndex++;
    }
    public static void insertDoubleData(int rowIndex,int columnIndex,double data){
        HSSFRow row=sheet.getRow(rowIndex);
        if(row==null){
            row=sheet.createRow(rowIndex);
        }
        HSSFCell cell=row.createCell(columnIndex);
        cell.setCellValue(data);
    }
    public static void insertDoubleData(double data){
        insertDoubleData(rowIndex,columnIndex,data);
        columnIndex++;
    }

    public static void insertDataAfterRow(int data){
        HSSFRow row;
        int i=-1;
        while((row=sheet.getRow(++i))!=null);
        sheet.createRow(i);
        sheet.getRow(i).createCell(0).setCellValue(data);
    }
    public static void insertString(String data){
        insertString(rowIndex,columnIndex,data);
        columnIndex++;
    }
    public static void insertString(int rowIndex,int columnIndex,String data){
        HSSFRow row=sheet.getRow(rowIndex);
        if(row==null){
            row=sheet.createRow(rowIndex);
        }
        HSSFCell cell=row.createCell(columnIndex);
        cell.setCellValue(data);
    }
    public static void save(){
        FileOutputStream fos=null;
        try{
            fos=new FileOutputStream(path);
            workbook.write(fos);
            fos.flush();
        }catch (Exception e){

        }finally {
            try{
                fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public static void toNextRow(){
        rowIndex++;
        columnIndex=0;
    }
    public static void clearIndex(){
        rowIndex=0;
        columnIndex=0;
    }
    public static void main(String [] args){
//        ToExcel toExcel=new ToExcel("God1.xls","expData");
//        for(int i=0;i<100;++i)
//            for(int j=i;j<100;++j){
//                toExcel.insertData(i,j,1);
//            }
//        toExcel.save();
        for(int i=0;i<10;++i){
            ToExcel.insertString(i+1,6,i+"gosh");
        }
        ToExcel.save();
        for(int i=0;i<10;++i){
            ToExcel.insertDoubleData(i+1,7,2.3);
        }
        ToExcel.save();
    }
}