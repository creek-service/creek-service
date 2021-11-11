package org.creek.other;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class LibraryTest {
    @Test
    void someLibraryMethodReturnsTrue() {
        final Library classUnderTest = new Library();
        assertThat(classUnderTest.someLibraryMethod(), is(true));
    }
}
