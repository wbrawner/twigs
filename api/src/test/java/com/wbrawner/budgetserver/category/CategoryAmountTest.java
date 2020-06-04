package com.wbrawner.budgetserver.category;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryAmountTest {

    @Test
    public void sortTest() {
        var first = new CategoryAmount(null, 0L, 4, 2020);
        var second = new CategoryAmount(null, 0L, 2, 2020);
        var third = new CategoryAmount(null, 0L, 2, 2019);
        var categoryAmounts = Arrays.asList(
                third,
                first,
                second
        );
        Collections.sort(categoryAmounts);
        assertEquals(first, categoryAmounts.get(0));
        assertEquals(second, categoryAmounts.get(1));
        assertEquals(third, categoryAmounts.get(2));
    }
}