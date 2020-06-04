package com.wbrawner.budgetserver.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CategoryTest {
    private final Category category = new Category("Test Category", null, null, false);

    @BeforeEach
    public void setup() {
        category.setAmount(815L, 8, 2015);
        category.setAmount(310L, 3, 2010);
        category.setAmount(920L, 9, 2020);
        category.setAmount(1117L, 11, 2017);
        category.setAmount(217L, 2, 2017);
        category.setAmount(412L, 4, 2012);
    }

    @ParameterizedTest(name = "{index} amount: {0} month: {1}, year: {2}")
    @ArgumentsSource(AmountArgumentsSource.class)
    void getAmount(long expected, int month, int year) {
        assertEquals(expected, category.getAmount(month, year));
    }

    @Test
    void getMostRecentAmount() {
        assertEquals(920L, category.getMostRecentAmount());
    }

    public static class AmountArgumentsSource implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    arguments(0L, 1, 2014),
                    arguments(412L, 5, 2013),
                    arguments(310L, 3, 2010),
                    arguments(920L, 10, 2021)
            );
        }
    }
}