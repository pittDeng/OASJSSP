package data;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ToExcelPrototype {
    HSSFWorkbook workbook;
    HSSFSheet sheet;
    String path;
    public static int rowIndex=0;
    public static int columnIndex=0;
    public ToExcelPrototype(String path,String sheetName){
        this.path=path;
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
        sheet=workbook.getSheet(sheetName);
        if(sheet==null){
            sheet=workbook.createSheet(sheetName);
        }
    }
    public void insertData(int rowIndex,int columnIndex,int data){
        HSSFRow row=sheet.getRow(rowIndex);
        if(row==null){
            row=sheet.createRow(rowIndex);
        }
        HSSFCell cell=row.createCell(columnIndex);
        cell.setCellValue(data);
    }
    public void insertData(int data){
        insertData(rowIndex,columnIndex,data);
    }
    public void insertDataAfterRow(int data){
        HSSFRow row;
        int i=-1;
        while((row=sheet.getRow(++i))!=null);
        sheet.createRow(i);
        sheet.getRow(i).createCell(0).setCellValue(data);
    }
    public void insertString(int rowIndex,int columnIndex,String data){
        HSSFRow row=sheet.getRow(rowIndex);
        if(row==null){
            row=sheet.createRow(rowIndex);
        }
        HSSFCell cell=row.createCell(columnIndex);
        cell.setCellValue(data);
    }
    public void save(){
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
    public static void main(String [] args){
//        ToExcel toExcel=new ToExcel("God1.xls","expData");
//        for(int i=0;i<100;++i)
//            for(int j=i;j<100;++j){
//                toExcel.insertData(i,j,1);
//            }
//        toExcel.save();
        ToExcelPrototype toExcel=new ToExcelPrototype("God.xls","hello");
        for(int i=0;i<10;++i){
            toExcel.insertString(i+1,6,i+"Hello");
        }
        toExcel.save();
    }
}
