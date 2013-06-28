package org.hivesoft.confluence.macros.vote.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BallotTest {

    private static final String SOME_BALLOT = "SOME_BALLOT";

    @Before
    public void setup() {
    }

    @Test
    public void test_getVoteForExistingUser_success() {
        Choice someChoice = new Choice("someChoice");
        someChoice.voteFor("someExistingUser");

        Ballot classUnderTest = new Ballot(SOME_BALLOT);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote("someExistingUser");

        assertEquals(someChoice, result);
    }

    @Test
    public void test_getVoteForNotExistingUser_success() {
        Choice someChoice = new Choice("someChoice");
        someChoice.voteFor("someExistingUser");

        Ballot classUnderTest = new Ballot(SOME_BALLOT);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote("someDifferentNotExistingUser");

        assertTrue(null == result);
    }
}