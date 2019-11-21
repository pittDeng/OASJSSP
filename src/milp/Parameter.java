package milp;

public interface Parameter {
    String OASName="OAS14";
    int islandsNumber=10;
    int MAX_ITERATION_NUMBER=60000;
    int earlyExchangeGap=2000;
    int middleExchangeGap=1000;
    int lateExchangeGap=100;
    String fileName="OAS08";
    int height=5;
    int popSize=50;
    int iterNum=1000;
    double immiRatio=0.2;
    int MILPTime=3600;
    double worseAcceptRatio=0.2;
    double bestImmiRatio=0.2;
    String excelPath="data/exp2.xls";
    String sheetName="sheet1";
    boolean isToExcel=true;
    boolean isPrint=false;

}