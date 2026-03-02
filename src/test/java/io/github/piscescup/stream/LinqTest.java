package io.github.piscescup.stream;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Linq;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class LinqTest {

    @Test
    void testSelect() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        List<Integer> result = numbers.select(x -> x * 2).toList();

        assertEquals(Arrays.asList(2, 4, 6, 8, 10), result);
    }

    @Test
    void testWhere() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5, 6);

        List<Integer> result = numbers.where(x -> x % 2 == 0).toList();

        assertEquals(Arrays.asList(2, 4, 6), result);
    }

    @Test
    void testAggregate() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        int sum = numbers.aggregate(0, Integer::sum);

        assertEquals(15, sum);
    }

    @Test
    void testOrderBy() {
        Enumerable<Integer> numbers = Linq.of(5, 3, 4, 1, 2);

        List<Integer> result = numbers
            .order(Comparator.naturalOrder())
            .toList();

        assertEquals(Arrays.asList(1, 2, 3, 4, 5), result);
    }

    // 测试 Enumerable 中的 groupBy 操作
    @Test
    void testGroupBy() {
        Enumerable<String> words = Linq.of("apple", "banana", "cherry", "avocado", "blueberry");

        List<Long> result = words
            .groupBy(
                word -> word.charAt(0),
                // (c,  es) -> es.count()
                (Character c, Enumerable<String> es) -> es.count()
            )
            .toList();

        assertEquals(Arrays.asList(
            2L, 2L, 1L
        ), result);
    }

    @Test
    void testToList() {
        Enumerable<Number> numbers = Linq.of(1, 2, 3, 4, 5);


        numbers.extractTo(Integer.class)
                .forEach(System.out::println);

    }

    @Test
    void testDistinct() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 2, 3, 3, 3, 4);

        // 测试 distinct 方法，去重
        List<Integer> result = numbers.distinct().toList();

        assertEquals(Arrays.asList(1, 2, 3, 4), result);
    }

    @Test
    void testAny() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        boolean anyGreaterThan3 = numbers.any(x -> x > 3);

        assertTrue(anyGreaterThan3);
    }

    @Test
    void testAll() {
        Enumerable<Integer> numbers = Linq.of(2, 4, 6, 8, 10);

        boolean allEven = numbers.all(x -> x % 2 == 0);

        assertTrue(allEven);
    }

    @Test
    void testFirst() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        int first = numbers.first();

        assertEquals(1, first);
    }

    @Test
    void testLast() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        int last = numbers.last();

        assertEquals(5, last);
    }

    @Test
    void testCount() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        long count = numbers.count();

        assertEquals(5, count);
    }

    @Test
    void testMax() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        int max = numbers.maxByInt(i -> i);

        assertEquals(5, max);
    }

    @Test
    void testMin() {
        Enumerable<Integer> numbers = Linq.of(1, 2, 3, 4, 5);

        int min = numbers.minByInt(i -> i * i);

        assertEquals(1, min);
    }
}

record Person(String name, int age, String address) {

    public String email() {
        return "%s%d@%s.com".formatted(name, age, address);
    }

    @Override
    public @NonNull String toString() {
        return "%s (%d years) at %s, %s\n".formatted(name, age, address, email());
    }
}
