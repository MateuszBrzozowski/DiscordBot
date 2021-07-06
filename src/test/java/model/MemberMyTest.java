package model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemberMyTest {

    @Test
    public void getUserNameWithoutRangers_BrzozaaaR_OnlyNick(){
        //given
        MemberMy m = new MemberMy("0000","Brzozaaa<RangersPL>");
        //when
        String mS = m.getUserNameWithoutRangers();
        //then
        Assert.assertEquals(mS,"Brzozaaa");
    }
    @Test
    public void getUserNameWithoutRangers_BrzozaaaRr_OnlyNick(){
        //given
        MemberMy m = new MemberMy("0000","Brzozaaa<rRangersPL>");
        //when
        String mS = m.getUserNameWithoutRangers();
        //then
        Assert.assertEquals(mS,"Brzozaaa");
    }

}