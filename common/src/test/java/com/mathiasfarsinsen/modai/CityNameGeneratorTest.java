package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.naming.CityNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CityNameGeneratorTest {

    @Test
    void generatedNamesAreUniqueWithinTheSameGenerator() {
        CityNameGenerator generator = new CityNameGenerator(1234L);
        Set<String> names = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String name = generator.generate();
            assertFalse(names.contains(name), "Duplicate name generated: " + name);
            names.add(name);
        }
        assertEquals(200, names.size());
    }

    @Test
    void reservedNamesAreNeverReturned() {
        CityNameGenerator generator = new CityNameGenerator(99L);
        String first = generator.generate();

        CityNameGenerator second = new CityNameGenerator(99L);
        second.reserve(first);
        String next = second.generate();

        assertFalse(next.equals(first));
    }

    @Test
    void generatedNameIsNonEmpty() {
        CityNameGenerator generator = new CityNameGenerator();
        String name = generator.generate();
        assertTrue(name != null && !name.isBlank());
    }
}
