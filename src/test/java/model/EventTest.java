package model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import helpers.Validation;
import org.junit.Assert;
import org.junit.Test;

public class EventTest {

    private Event event = new Event();
    private String patternDate = "dd.MM.yyyy";
    private String patternTime = "hh:mm";

    @Test
    public void getName_ValueOK_GetNameProperty(){
        //given
        String[] msg = {"!zapisy","-name","Event","testowy","-date","20.03.2000r","-time","20:00"};
        //when
        String name = event.getEventName(msg);
        //then
        Assert.assertEquals(name,"Event testowy ");
    }
    @Test
    public void getName_ValueOKOneWord_GetNameProperty(){
        //given
        String[] msg = {"!zapisy","-name","Event","-date","20.03.2000r","-time","20:00"};
        //when
        String name = event.getEventName(msg);
        //then
        Assert.assertEquals(name,"Event ");
    }
    @Test
    public void getName_ValueLast_GetNameProperty(){
        //given
        String[] msg = {"!zapisy","-date","20.03.2000r","-time","20:00","-name","Event","testowy"};
        //when
        String name = event.getEventName(msg);
        //then
        Assert.assertEquals(name,"Event testowy ");
    }
    @Test
    public void getName_ValueBeforeTime_GetNameProperty(){
        //given
        String[] msg = {"!zapisy","-date","20.03.2000r","-name","Event","testowy","-time","20:00"};
        //when
        String name = event.getEventName(msg);
        //then
        Assert.assertEquals(name,"Event testowy ");
    }
    @Test
    public void getName_ValueBeforeO_GetNameProperty(){
        //given
        String[] msg = {"!zapisy","-date","20.03.2000r","-name","Event","testowy","-o","opis","-time","20:00"};
        //when
        String name = event.getEventName(msg);
        //then
        Assert.assertEquals(name,"Event testowy ");
    }
    @Test
    public void getName_ValueNull_null(){
        //given
        String[] msg = {"!zapisy","-date","20.03.2000r","-name","-o","opis","-time","20:00"};
        //when
        String name = event.getEventName(msg);
        //then
        Assert.assertEquals(name,null);
    }
    @Test
    public void checkMessage_NameDateTime_True(){
        //given
        String[] msg = {"!zapisy","-name","Event","testowy","-date","20.03.2000r","-time","20:00"};
        //when
        Boolean b = event.checkMessage(msg);
        //then
        Assert.assertEquals(b,true);
    }
    @Test
    public void checkMessage_NameDateTimeOp_True(){
        //given
        String[] msg = {"!zapisy","-name","Event","testowy","-date","20.03.2000r","-time","20:00","-o","opis"};
        //when
        Boolean b = event.checkMessage(msg);
        //then
        Assert.assertEquals(b,true);
    }
    @Test
    public void checkMessage_NameDate_False(){
        //given
        String[] msg = {"!zapisy","-name","Event","testowy","-date","20.03.2000r","-o","opis"};
        //when
        Boolean b = event.checkMessage(msg);
        //then
        Assert.assertEquals(b,false);
    }
    @Test
    public void checkMessage_NameTime_False(){
        //given
        String[] msg = {"!zapisy","-name","Event","testowy","-time","20:00","-o","opis"};
        //when
        Boolean b = event.checkMessage(msg);
        //then
        Assert.assertEquals(b,false);
    }
    @Test
    public void checkMessage_DateTimeOp_False(){
        //given
        String[] msg = {"!zapisy","-date","20.03.2000r","-time","20:00","-o","opis"};
        //when
        Boolean b = event.checkMessage(msg);
        //then
        Assert.assertEquals(b,false);
    }
    @Test
    public void isDateformat_DateOKDayLast_True(){
        //given
        String date = "31.01.2000";
        //when
        Boolean b = Validation.isDateFormat(date);
        //then
        Assert.assertEquals(b,true);
    }
    @Test
    public void isDateformat_DateOKDayFirst_True(){
        //given
        String date = "1.01.2000";
        //when
        Boolean b = Validation.isDateFormat(date);
        //then
        Assert.assertEquals(b,true);
    }
    @Test
    public void getDescription_DescriptionOKMid(){
        //given
        String[] msg = {"!zapisy","-date","20.03.2000r","-name","Event","testowy","-o","opis","wydarzenia","testowego","-time","20:00"};
        //when
        String descirption = event.getDescription(msg);
        //then
        Assert.assertEquals(descirption,"opis wydarzenia testowego ");
    }
    @Test
    public void getDescription_DescriptionOKEnd(){
        //given
        String[] msg = {"!zapisy","-o","opis","wydarzenia","testowego"};
        //when
        String descirption = event.getDescription(msg);
        //then
        Assert.assertEquals(descirption,"opis wydarzenia testowego ");
    }
    @Test
    public void getDescription_DescriptionNullMid_Null(){
        //given
        String[] msg = {"!zapisy","-o","-time"};
        //when
        String descirption = event.getDescription(msg);
        //then
        Assert.assertEquals(descirption,null);
    }
    @Test
    public void getDescription_DescriptionNullEnd_Null(){
        //given
        String[] msg = {"!zapisy","-o"};
        //when
        String descirption = event.getDescription(msg);
        //then
        Assert.assertEquals(descirption,null);
    }
    @Test
    public void getDescription_NoDescription_Null(){
        //given
        String[] msg = {"!zapisy","-time","20:00"};
        //when
        String descirption = event.getDescription(msg);
        //then
        Assert.assertEquals(descirption,null);
    }
    @Test
    public void isTimeFormat_TimeOK_True(){
        //given
        String time = "19:59";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,true);
    }
    @Test
    public void isTimeFormat_TimeLast_True(){
        //given
        String time = "23:59";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,true);
    }
    @Test
    public void isTimeFormat_TimeOKFirst_True(){
        //given
        String time = "00:00";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,true);
    }
    @Test
    public void isTimeFormat_TimeLastNoOk_False(){
        //given
        String time = "24:05";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,false);
    }
    @Test
    public void isTimeFormat_MinutyZaZakresem_False(){
        //given
        String time = "19:60";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,false);
    }
    @Test
    public void isTimeFormat_Format_False(){
        //given
        String time = "19k05";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,false);
    }
    @Test
    public void isTimeFormat_FormatDwa_False(){
        //given
        String time = "9:05";
        //when
        boolean isTime = Validation.isTimeFormat(time);
        //then
        Assert.assertEquals(isTime,false);
    }
}