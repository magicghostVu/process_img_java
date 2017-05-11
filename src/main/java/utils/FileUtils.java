package utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by magic_000 on 11/05/2017.
 */
public class FileUtils {
    public static List<File> getAllFileFromFolder(String folderPath){

        File folder= new File(folderPath);

        if(!folder.isDirectory()){
            throw new IllegalArgumentException("is not a folder");
        }
        if(!folder.exists()){
            throw new IllegalArgumentException("folder is not exist");
        }
        Stream<File> strFile= Arrays.stream(folder.listFiles());

        List<File> res= strFile.filter(file->
            !file.isDirectory()
        ).collect(Collectors.toList());
        return res;

    }

}
