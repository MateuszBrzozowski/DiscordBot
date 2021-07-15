package helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Validation {

    protected static final Logger logger = LoggerFactory.getLogger(Validation.class.getName());
    private static final String datePattern = "dd.MM.yyyy";

    public static boolean isDateFormat(String s) {
        SimpleDateFormat df = new SimpleDateFormat(datePattern);
        df.setLenient(false);
        try {
            Date javaDate = df.parse(s);
            df.parse(s);
        } catch (ParseException e) {
//            rangerLogger.info(String.format("Nieprawidłowa data %s. Format daty: \"%s\"",s,pattern));
            return false;
        }
        return true;
    }

    public static boolean isTimeFormat(String s) {
        if (s.length()==5){
            if (s.substring(2,3).equalsIgnoreCase(":")){
                if (isDecimal(s.substring(0,1)) && isDecimal(s.substring(1,2)) && isDecimal(s.substring(3,4)) && isDecimal(s.substring(4,5))){
                    int hour = Integer.parseInt(s.substring(0,2));
                    int min = Integer.parseInt(s.substring(3,5));
                    if (hour>=0 && hour<=23 && min>=0 && min<=59){
                        return true;
                    }
                    else logger.info("Zly format - godzina lub czas wyszły za zakres");
                }
                else logger.info("Zly format - to nie jest liczba");
            }
            else logger.info("Zly format :");
        }
        return false;
    }

    private static boolean isDecimal(String s) {
        for (int i = 0; i < 10; i++) {
            if (s.equals(String.valueOf(i))){
                return true;
            }
        }
        return false;
    }
}
