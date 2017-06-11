package pack;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by magic_000 on 11/05/2017.
 */
public class Main {

    private static double calculateStandardDeviation(Integer[] arr) {
        double sum = 0.0D;
        for (Integer n : arr) {
            sum = sum + n;
        }
        double avg = sum / arr.length;
        Double[] arrTmp = new Double[arr.length];
        for (int i = 0; i < arrTmp.length; i++) {
            arrTmp[i] = (avg - arr[i]) * (avg - arr[i]);
        }
        List<Double> devs = Arrays.asList(arrTmp);
        return Math.sqrt(devs.stream().reduce(0.0D, (a, b) -> a + b) / (arr.length - 1));
    }

    public static Integer[] calSubArrFromArr2D(int windowSize, int rowIndex, int columnIndex, Integer[][] arr) {

        int numCellAround = windowSize / 2;

        int rowIndexStart = rowIndex;
        int columnIndexStart = columnIndex;
        //var rowIndexEnd= rowIndexEnd
        int i = 0;
        while (i < numCellAround && rowIndexStart > 0) {
            rowIndexStart -= 1;
            i += 1;
        }
        i = 0;
        while (i < numCellAround && columnIndexStart > 0) {
            columnIndexStart -= 1;
            i += 1;
        }
        i = 0;
        int rowIndexEnd = rowIndex;
        int columnIndexEnd = columnIndex;
        while (i < numCellAround && columnIndexEnd < arr[0].length - 1) {
            columnIndexEnd += 1;
            i += 1;
        }
        i = 0;
        while (i < numCellAround && rowIndexEnd < arr.length - 1) {
            rowIndexEnd += 1;
            i += 1;
        }
        List<Integer> tmpResult = new ArrayList<>();

        for (int j = rowIndexStart; j < rowIndexEnd; ++j) {
            for (int k = columnIndexStart; k < columnIndexEnd; ++k) {
                tmpResult.add(arr[j][k]);
            }
        }
        return tmpResult.toArray(new Integer[tmpResult.size()]);
    }


    public static Double[] processData(Integer[] allNumber) {

        double threshold_1 = 0.90D;
        double threshold_2 = 0.99D;

        int winSize=3;

        ConcurrentHashMap<Integer, Integer> histogramMap = new ConcurrentHashMap<>();
        if (allNumber.length != 1024 * 1024) {
            return null;
        } else {
            Stream<Integer> allNumberStream = Arrays.stream(allNumber).parallel();
            allNumberStream.forEach(number -> {
                if (histogramMap.containsKey(number)) {
                    Integer currentVal = histogramMap.get(number);
                    currentVal++;
                    histogramMap.put(number, currentVal);
                } else {
                    histogramMap.put(number, 1);
                }
            });

            List<Integer> allKeys= new ArrayList<>(histogramMap.keySet());
            Collections.sort(allKeys);
            Collections.reverse(allKeys);

            int m = 0;
            int totalPixel_1 = 0;
            // m at 0.90
            while (m < allKeys.size() - 1 && totalPixel_1 <= threshold_1 * 1024 * 1024) {
                totalPixel_1 += histogramMap.get(allKeys.get(m));
                m += 1;
            }


            int totalPixel_2 = 0;
            int n = 0;
            // n at 0.99
            while (n < allKeys.size() - 1 && totalPixel_2 <= threshold_2 * 1024 * 1024) {
                totalPixel_2 += histogramMap.get(allKeys.get(n));
                n += 1;
            }


            double cd= (m+1.0)/(n+1.0);


            Stream<Double> intensityStream= Arrays.stream(allNumber).parallel().map(number->{
                if(histogramMap.containsKey(number)){
                    return histogramMap.get(number)/(1024*1024.0);
                }else {
                    return 0D;
                }
            });

            List<Double> intensityList= intensityStream.collect(Collectors.toList());


            Integer[][] arr2D= new Integer[1024][1024];

            for(int i=0;i<1024; ++i){
                for (int j = 0; j <1024 ; j++) {
                    arr2D[i][j]= allNumber[j*1024+i];
                }
            }

            List<Double> filterList= new ArrayList<>();
            for (int i = 0; i <1024 ; i++) {
                for (int j = 0; j < 1024; j++) {
                    Integer[] surroundCell= calSubArrFromArr2D(winSize, i, j, arr2D);
                    double stdDeviation= calculateStandardDeviation(surroundCell);
                    if (stdDeviation==0.0D){
                        filterList.add(0.0D);
                    }else{

                        //this line is wrong, it must be avg (surroundCell)/stdDeviation
                        filterList.add(arr2D[i][j]/stdDeviation);
                    }
                }
            }

            List<Pair> mergeIntensityAndFilter= new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                Pair<Double, Double> tmpPair= new Pair<>(intensityList.get(i), filterList.get(i));
                mergeIntensityAndFilter.add(tmpPair);
            }
            Stream<Double> resStream=mergeIntensityAndFilter.stream().parallel().map(pair ->
                    (Double) pair.getFirst()*cd+ (1-cd)* (Double)pair.getSecond()
            );
            return resStream.collect(Collectors.toList()).toArray(new Double[1024*1024]);

        }
    }


    public static List<String> readTextFile(File file){
        try {
            List<String> result= new ArrayList<>();
            FileReader fileReader= new FileReader(file);
            BufferedReader bfr= new BufferedReader(fileReader);
            while (true){
                String line= bfr.readLine();
                if(line==null){
                    break;
                }else{
                    result.add(line);
                }
            }
            bfr.close();
            fileReader.close();
            return result;
        }catch (IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }


    public static void processAFile(File csvFile){
        List<String> allLine= readTextFile(csvFile);

        List<Integer> listAllNumber= allLine.stream().flatMap(line->
             Arrays.stream( line.split(","))
        ).map(token-> Integer.parseInt(token)).collect(Collectors.toList());


        Integer[] allNumber= new Integer[listAllNumber.size()];
        listAllNumber.toArray(allNumber);
        Double[] result= processData(allNumber);

        List<String> resString= new ArrayList<>();
        for (Double t: result){
            resString.add(t.toString());
        }

        String[] dataToFile= new String[resString.size()];
        resString.toArray(dataToFile);

        try {
            writeToFile(dataToFile, csvFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            Thread.sleep(10000);
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }

        System.out.println(result.length);
    }

    public static void writeToFile(String[] allString, String name) throws IOException{
        String currentFolder= System.getProperty("user.dir");
        File result= new File(currentFolder+ File.separator +name+".res");
        if(result.exists()){
            result.delete();
            result.createNewFile();
        }else{
            result.createNewFile();
            FileWriter fw= new FileWriter(result);
            StringBuilder builder= new StringBuilder();
            for (int i = 0; i < allString.length; i++) {
                if(allString[i].equals("ReadFile.jar")){
                    continue;
                }
                if(i==allString.length-1){
                    builder.append(allString[i]);
                }else{
                    builder.append(allString[i]+"\n");
                }
            }
            String source= builder.toString();
            fw.write(source);
            fw.flush();
            fw.close();
        }

    }

    public static void main(String[] args) throws Exception{
        System.out.println("Main run");

        System.out.println(System.currentTimeMillis()/1000);

        String[] listFileName= new String[]{"IMAGERY.TIF_0_1024.csv",
                "IMAGERY.TIF_0_2048.csv","IMAGERY.TIF_0_3072.csv",
                "IMAGERY.TIF_0_4096.csv", "IMAGERY.TIF_0_5120.csv",
                "IMAGERY.TIF_0_6144.csv", "IMAGERY.TIF_0_8192.csv",
                "IMAGERY.TIF_1024_0.csv", "IMAGERY.TIF_1024_1024.csv", "IMAGERY.TIF_1024_2048.csv"};



        //String prefixPath= "C:\\Users\\magic_000\\Desktop\\csv";
        String prefixPath= "/home/magicghost_vu/Desktop/csv";

        //File csvFile= new File(prefixPath+File.separator+listFileName[0]);

        //processAFile(csvFile);

        for(String fileName: listFileName){
            File csv= new File(prefixPath+File.separator+fileName);
            System.out.println("Processing file "+ fileName);
            processAFile(csv);
        }
        System.out.println(System.currentTimeMillis()/1000);

    }
}
