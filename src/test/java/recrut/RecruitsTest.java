package recrut;

import org.junit.Assert;
import org.junit.Test;

public class RecruitsTest {

    @Test
    public void isNicknameRNGSuffix_True() {
        Recruits recruits = new Recruits();
        String nickname = "Nick<rRangersPL>";
        Assert.assertTrue(recruits.isNicknameRNGSuffix(nickname));
    }

    @Test
    public void isNicknameRNGSuffix_EmptyChar_True() {
        Recruits recruits = new Recruits();
        String nickname = "Nick<rRangersPL> ";
        Assert.assertTrue(recruits.isNicknameRNGSuffix(nickname));
    }

    @Test
    public void isNicknameRNGSuffix_EmptyCharTwo_True() {
        Recruits recruits = new Recruits();
        String nickname = "Nick <rRangersPL>";
        Assert.assertTrue(recruits.isNicknameRNGSuffix(nickname));
    }

    @Test
    public void isNicknameRNGSuffix_WrongTag_False() {
        Recruits recruits = new Recruits();
        String nickname = "Nick<rRengersPL>";
        Assert.assertFalse(recruits.isNicknameRNGSuffix(nickname));
    }

    @Test
    public void isNicknameRNGSuffix_NoTag_False() {
        Recruits recruits = new Recruits();
        String nickname = "Nick";
        Assert.assertFalse(recruits.isNicknameRNGSuffix(nickname));
    }
}