package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import controller.PlayersNamesController;

public class NameValidatorTest {
	
	 

    @Test
    // valid name : +4 letters+ no space , then should return true.
    void validName_isAccepted() {
         PlayersNamesController p = new PlayersNamesController(null, null, null);
        assertTrue(p.isValidName("Sally"));
    }

    @Test
    // less than 4 letters should return false.
    void shortName_isInvalid() {
    	PlayersNamesController p = new PlayersNamesController(null, null, null);
        assertFalse(p.isValidName("Tom"));
    }

    @Test
    // name with space, should return false.
    void nameWithSpace_isInvalid() {
    	PlayersNamesController p = new PlayersNamesController(null, null, null);
    	assertFalse(p.isValidName("ab c"));
    }

    @Test
    // Null name, should return false.
    void nullName_isInvalid() {
    	PlayersNamesController p = new PlayersNamesController(null, null, null);
    	assertFalse(p.isValidName(null));
    }
}
