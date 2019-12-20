package com.chongdianleme.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CharToIntService {
    private static Map<Character, Integer> charToInts = null;


    private static Map<Character,Integer> getCharToInt() {

        if (charToInts == null) {
            synchronized (CharToIntService.class) {
                if (charToInts == null) {
                    try {

                        char[] chars = FileService.getDistinctChars();
                        Map<Character,Integer> map = new HashMap<>();
                        //char[] chars = getValidCharacters();
                        for (int i = 0; i < chars.length; i++) {
                            map.put( chars[i],i);
                        }

                        charToInts = map;
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return charToInts;
    }
    public static void main(String[] args)
    {



        for (int i =0;i<100;i++) {
            System.out.println("用户"+getStringRandom(5));
        }

    }


        /*for (int i=0;i<100;i++) {
            long start = System.currentTimeMillis();
            Map<Character,Integer> temp = getCharToIntMap();

            System.out.println(temp.size()+" times："+(System.currentTimeMillis()-start));
        }*/

    //生成随机用户名，数字和字母组成,
    public static String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for(int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if( "char".equalsIgnoreCase(charOrNum) ) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char)(random.nextInt(26) + temp);
            } else if( "num".equalsIgnoreCase(charOrNum) ) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val.toUpperCase();
    }
    public static Map<Character,Integer> getCharToIntMap() {
        return  getCharToInt();
    }

}
