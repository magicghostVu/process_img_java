package testGetListFile;

import junit.framework.TestCase;
import org.junit.Test;
import pack.Main;
import utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Created by magic_000 on 11/05/2017.
 */
public class TestGetFiles extends TestCase {
    @Test
    public void testGetListFile(){
        List<File> allFile= FileUtils.getAllFileFromFolder("C:\\Users\\magic_000\\Desktop\\csv");
        assertEquals( 100,allFile.size());
    }


    public void testGetAllLine(){
        File csv= new File("C:\\Users\\magic_000\\Desktop\\csv\\IMAGERY.TIF_0_2048.csv");
        assertEquals(1024, Main.readTextFile(csv).size());
    }

    public void testSubArr(){
        Integer[][] arr= new Integer[10][10];

        int count=0;

        for (int i = 0; i <10 ; i++) {
            for (int j = 0; j <10 ; j++) {
                arr[i][j]= count;
                count++;
            }
        }

        Integer[] res= Main.calSubArrFromArr2D(3, 8,8, arr);

        System.out.println("OK");


    }

}
