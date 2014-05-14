package edu.neu.rrc.wifisigstrdiag;


import android.test.*;

public class UtilsTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMatchesWildcard() throws Exception {
        assertTrue(Utils.matchesWildcard("*test*cat*", "testingcat"));
        assertTrue(Utils.matchesWildcard("*test*cat*", "testingcats"));
        assertFalse(Utils.matchesWildcard("*test*cat", "testingcats"));
        assertTrue(Utils.matchesWildcard("*test*cat*", "stilltestingcats"));
        assertTrue(Utils.matchesWildcard("test*cat*", "testingcats"));
        assertFalse(Utils.matchesWildcard("test*cat*", "atestingcats"));
        assertFalse(Utils.matchesWildcard("test*cat*", "testingcows"));
        assertTrue(Utils.matchesWildcard("test*cat*", "testcat"));
    }

}
