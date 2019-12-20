package com.chongdianleme.job;
import java.util.*;

public class IntToCharService {
    private static  Map<Integer, Character> intToChars = null;

    private static Map<Integer, Character> getIntToChars() {

        if (intToChars == null) {
            synchronized (IntToCharService.class) {
                if (intToChars == null) {
                    try {

                        char[] chars = FileService.getDistinctChars();
                        Map<Integer, Character> map = new HashMap<>();
                        //char[] chars = getValidCharacters();
                        for (int i = 0; i < chars.length; i++) {
                            map.put(i, chars[i]);
                        }

                        intToChars = map;
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return intToChars;
    }
    public static void main(String[] args)
    {
        for (int i=0;i<100;i++) {
           long start = System.currentTimeMillis();

            Map<Integer, Character> temp = getIntToCharsMap();

            System.out.println(temp.size()+" times："+(System.currentTimeMillis()-start));
        }
    }

    public static Map<Integer, Character> getIntToCharsMap() {
        return  getIntToChars();
    }

}
