package com.chongdianleme.job;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class FileService {
    private static char[] chars = null;
    private static String rfPath = null;

    private static char[] getChars() {

        if (chars == null) {
            synchronized (FileService.class) {
                if (chars == null) {
                    try {


                        //File file = new File("D:\\ai\\doc\\语言模型\\samples.txt");
                        File file = new File("/home/hadoop/sparktask/lmlib/samples.txt");
                        BufferedReader in = new BufferedReader(new FileReader(file.getPath()));
                        String t;
                        List<String> lines = new ArrayList<String>();
                        while ((t = in.readLine()) != null) {
                            lines.add(t);
                        }
                        in.close();

                        List<Character> validChars = new LinkedList<>();
                        for(String line : lines)
                        {
                            char[] arr = line.replace(" ","").toCharArray();
                            for(char c : arr) {
                                if (!validChars.contains(c))
                                    validChars.add(c);
                            }
                        }
                        char[] out = new char[validChars.size()];
                        int i=0;
                        for( Character c : validChars ) out[i++] = c;
                        chars = out;
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return chars;
    }
    public static void main(String[] args) throws Exception
    {
        String solrCore = "vod";
        if (solrCore == "vod")
            System.out.println(solrCore == "vod");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm",new Locale("zh", "CN"));

        long endDT=sdf.parse("2088-08-08").getTime();
        System.out.println( "endDateTime:"+endDT);


/*        for (int i=0;i<100;i++) {
            long start = System.currentTimeMillis();
            char[] temp = getDistinctChars();

            System.out.println(temp.length+" times："+(System.currentTimeMillis()-start));
        }*/
    }

    public static char[] getDistinctChars() {
        char[] chars = getChars();
        return  chars;
    }

}
