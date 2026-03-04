package io.github.piscescup.linq;

import io.github.piscescup.interfaces.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumerableTest {

    @Test
    void aggregate_withSeedAndResultSelector() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3);
        String result = seq.aggregate(new StringBuilder(),
            (sb, i) -> sb.append(i).append(','),
            StringBuilder::toString);
        assertEquals("1,2,3,", result);
    }

    @Test
    void aggregate_withSeed() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3);
        int sum = seq.aggregate(0, (acc, i) -> acc + i);
        assertEquals(6, sum);
    }

    @Test
    void aggregate_withoutSeed() {
        Enumerable<Integer> seq = Linq.of(4, 2, 5);
        int max = seq.aggregate((a, b) -> a > b ? a : b);
        assertEquals(5, max);
        assertThrows(NoSuchElementException.class,
            () -> Linq.<Integer>empty().aggregate((a, b) -> a));
    }

    @Test
    void aggregateBy_withKeyMapping() {
        Enumerable<String> words = Linq.of("apple", "apricot", "banana", "blue");
        List<Pair<Character, Integer>> pairs = words.aggregateBy(
            s -> s.charAt(0),
            key -> 0,
            (acc, w) -> acc + w.length(),
            Comparator.naturalOrder()
        ).toList();
        assertEquals(2, pairs.size());
        assertEquals('a', pairs.get(0).getLeft());
        assertEquals(12, pairs.get(0).getRight());
        assertEquals('b', pairs.get(1).getLeft());
        assertEquals(10, pairs.get(1).getRight());
    }

    @Test
    void aggregateBy_withSeed() {
        Enumerable<String> words = Linq.of("a", "ab", "abc");
        List<Pair<Integer, Integer>> pairs = words.aggregateBySeed(
            0,
            String::length,
            (acc, w) -> acc + 1,
            Comparator.naturalOrder()
        ).toList();
        assertEquals(3, pairs.size());
    }

    @Test
    void all() {
        Enumerable<Integer> seq = Linq.of(2, 4, 6);
        assertTrue(seq.all(x -> x % 2 == 0));
        assertFalse(seq.all(x -> x > 3));
        assertTrue(Linq.<Integer>empty().all(x -> false));
    }

    @Test
    void any() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3);
        assertTrue(seq.any(x -> x > 2));
        assertFalse(seq.any(x -> x > 5));
        assertFalse(Linq.empty().any(x -> true));
    }

    @Test
    void append() {
        Enumerable<Integer> seq = Linq.of(1, 2);
        assertEquals(List.of(1, 2, 3), seq.append(3).toList());
    }

    @Test
    void prepend() {
        Enumerable<Integer> seq = Linq.of(2, 3);
        assertEquals(List.of(1, 2, 3), seq.prepend(1).toList());
    }


    @Test
    void decimalAverageNullable() {
        Enumerable<BigDecimal> seq = Linq.of(
            new BigDecimal("1.5"), null, new BigDecimal("2.5"));
        BigDecimal avg = seq.decimalAverageNullable(MathContext.DECIMAL64);
        assertEquals(new BigDecimal("2.0"), avg);
    }

    @Test
    void average_toDouble() {
        record Person(int age) {}
        Enumerable<Person> people = Linq.of(new Person(20), new Person(30));
        assertEquals(25.0, people.average(Person::age));
    }

    @Test
    void average_toInt() {
        Enumerable<Integer> nums = Linq.of(1, 2, 3);
        assertEquals(2.0, nums.average((ToIntFunction<Integer>) x -> x));
    }

    @Test
    void average_toLong() {
        Enumerable<Long> nums = Linq.of(1L, 2L, 3L);
        assertEquals(2.0, nums.average((ToLongFunction<Long>) x -> x));
    }

    @Test
    void chunk() {
        Enumerable<Integer> nums = Linq.of(1, 2, 3, 4, 5);
        nums.chunk(2, Integer.class)
            .selectToObj(Arrays::toString)
            .forEach(System.out::println);
    }

    @Test
    void chunkAsList() {
        Enumerable<Integer> nums = Linq.of(1, 2, 3, 4, 5);
        List<List<Integer>> chunks = nums.chunkAsList(2).toList();
        assertEquals(List.of(1, 2), chunks.get(0));
        assertEquals(List.of(3, 4), chunks.get(1));
        assertEquals(List.of(5), chunks.get(2));
    }

    @Test
    void concat() {
        Enumerable<Integer> a = Linq.of(1, 2);
        Enumerable<Integer> b = Linq.of(3, 4);
        assertEquals(List.of(1, 2, 3, 4), a.concat(b).toList());
    }

    @Test
    void contains() {
        Enumerable<String> seq = Linq.of("a", "b", "c");
        assertTrue(seq.contains("b"));
        assertFalse(seq.contains("z"));
    }

    @Test
    void contains_withComparator() {
        Enumerable<String> seq = Linq.of("a", "b", "c");
        assertTrue(seq.contains("A", String.CASE_INSENSITIVE_ORDER));
    }

    @Test
    void count() {
        assertEquals(3, Linq.of(1, 2, 3).count());
        assertEquals(0, Linq.empty().count());
    }

    @Test
    void count_withPredicate() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3, 4);
        assertEquals(2, seq.count(x -> x % 2 == 0));
    }

    @Test
    void countBy() {
        Enumerable<String> words = Linq.of("apple", "apricot", "banana");
        var pairs = words.countBy(s -> s.charAt(0), Comparator.naturalOrder()).toList();
        assertEquals(2, pairs.size());
        assertEquals('a', pairs.get(0).getLeft());
        assertEquals(2L, pairs.get(0).getRight());
        assertEquals('b', pairs.get(1).getLeft());
        assertEquals(1L, pairs.get(1).getRight());
    }

    @Test
    void defaultIfEmpty() {
        Enumerable<Integer> nonEmpty = Linq.of(1, 2);
        assertEquals(List.of(1, 2), nonEmpty.defaultIfEmpty().toList());

        Enumerable<Integer> empty = Linq.empty();
        assertEquals(Collections.singletonList(null), empty.defaultIfEmpty().toList());
        assertEquals(List.of(42), empty.defaultIfEmpty(42).toList());
    }

    @Test
    void distinct() {
        Enumerable<Integer> seq = Linq.of(1, 2, 2, 3, 1);
        assertEquals(List.of(1, 2, 3), seq.distinct().toList());
    }

    @Test
    void distinct_withComparator() {
        Enumerable<String> seq = Linq.of("a", "A", "b", "B");
        List<String> result = seq.distinct(String.CASE_INSENSITIVE_ORDER).toList();
        assertEquals(2, result.size());
    }

    @Test
    void distinctBy() {
        record Person(String name, int age) {}
        Enumerable<Person> people = Linq.of(
            new Person("Alice", 20),
            new Person("Bob", 20),
            new Person("Alice", 30));
        List<Person> distinctByAge = people.distinctBy(Person::age).toList();
        assertEquals(2, distinctByAge.size());
    }

    @Test
    void distinctBy_withComparator() {
        record Person(String name, int age) {}
        Enumerable<Person> people = Linq.of(
            new Person("Alice", 20),
            new Person("alice", 30),
            new Person("Bob", 40));
        List<Person> distinct = people.distinctBy(Person::name, String.CASE_INSENSITIVE_ORDER).toList();
        assertEquals(2, distinct.size());
    }

    @Test
    void elementAt() {
        Enumerable<Integer> seq = Linq.of(10, 20, 30);
        assertEquals(10, seq.elementAt(0));
        assertEquals(30, seq.elementAt(2));
        assertThrows(IndexOutOfBoundsException.class, () -> seq.elementAt(3));
    }

    @Test
    void elementAtDefault() {
        Enumerable<Integer> seq = Linq.of(10, 20);
        assertEquals(10, seq.elementAtDefault(0, -1));
        assertEquals(-1, seq.elementAtDefault(5, -1));
    }

    @Test
    void except() {
        Enumerable<Integer> a = Linq.of(1, 2, 3, 4);
        Enumerable<Integer> b = Linq.of(3, 4, 5);
        assertEquals(List.of(1, 2), a.except(b).toList());
    }

    @Test
    void except_withComparator() {
        Enumerable<String> a = Linq.of("a", "b", "c");
        Enumerable<String> b = Linq.of("A", "B");
        List<String> result = a.except(b, String.CASE_INSENSITIVE_ORDER).toList();
        assertEquals(List.of("c"), result);
    }

    @Test
    void exceptBy() {
        record Person(int id, String name) {}
        Enumerable<Person> people = Linq.of(
            new Person(1, "A"),
            new Person(2, "B"),
            new Person(3, "C"));
        Enumerable<Integer> excludeIds = Linq.of(2, 4);
        List<Person> result = people.exceptBy(excludeIds, Person::id).toList();
        assertEquals(2, result.size()); // id 1 和 3
    }

    @Test
    void exceptBy_withComparator() {
        record Person(String id, String name) {}
        Enumerable<Person> people = Linq.of(
            new Person("a1", "A"),
            new Person("b2", "B"),
            new Person("c3", "C"));
        Enumerable<String> exclude = Linq.of("A1", "D4");
        List<Person> result = people
            .exceptBy(exclude, Person::id, String.CASE_INSENSITIVE_ORDER)
            .toList();
        assertEquals(2, result.size()); // "b2", "c3"
    }

    @Test
    void first() {
        assertEquals(5, Linq.of(5, 6).first());
        assertThrows(NoSuchElementException.class, Linq.empty()::first);
    }

    @Test
    void first_withPredicate() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3);
        assertEquals(2, seq.first(x -> x % 2 == 0));
        assertThrows(NoSuchElementException.class, () -> seq.first(x -> x > 5));
    }

    @Test
    void firstOrDefault() {
        assertEquals(1, Linq.of(1, 2).firstOrDefault(-1));
        assertEquals(-1, Linq.empty().firstOrDefault(-1));
    }

    @Test
    void firstOrDefault_withPredicate() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3);
        assertEquals(2, seq.firstOrDefault(-1, x -> x % 2 == 0));
        assertEquals(-1, seq.firstOrDefault(-1, x -> x > 5));
    }

    @Test
    void groupBy_simple() {
        Enumerable<String> words = Linq.of("apple", "apricot", "banana");
        List<Groupable<Character, String>> groups = words
            .groupBy(s -> s.charAt(0))
            .toList();
        System.out.println(groups);
        assertEquals(2, groups.size());
        var groupA = groups.stream()
            .filter(g -> g.key() == 'a')
            .toList();
        System.out.println(groupA);
    }

    @Test
    void groupBy_withResultMapping() {
        Enumerable<String> words = Linq.of("apple", "apricot", "banana");
        List<String> result = words.groupResultBy(
            s -> s.charAt(0),
            (key, group) -> key + ":" + group.count()
        ).toList();
        assertTrue(result.contains("a:2"));
        assertTrue(result.contains("b:1"));
    }

    @Test
    void groupJoin() {
        record Customer(int id, String name) {}
        record Order(int cid, String product) {}
        Enumerable<Customer> customers = Linq.of(
            new Customer(1, "Alice"),
            new Customer(2, "Bob"));
        Enumerable<Order> orders = Linq.of(
            new Order(1, "Book"),
            new Order(1, "Pen"),
            new Order(3, "Desk"));
        var result = customers.groupJoin(orders,
            Customer::id,
            Order::cid,
            (c, os) -> c.name() + " has " + os.count() + " orders"
        ).toList();
        assertEquals(List.of("Alice has 2 orders", "Bob has 0 orders"), result);
    }

    @Test
    void intersect() {
        Enumerable<Integer> a = Linq.of(1, 2, 3, 4);
        Enumerable<Integer> b = Linq.of(3, 4, 5);
        assertEquals(List.of(3, 4), a.intersect(b).toList());
    }

    @Test
    void intersectBy() {
        record Person(int id) {}
        Enumerable<Person> people = Linq.of(new Person(1), new Person(2), new Person(3));
        Enumerable<Integer> ids = Linq.of(2, 4);
        List<Person> result = people.intersectBy(ids, Person::id).toList();
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).id());
    }

    @Test
    void join() {
        record Customer(int id, String name) {}
        record Order(int cid, String product) {}
        Enumerable<Customer> customers = Linq.of(
            new Customer(1, "Alice"),
            new Customer(2, "Bob"));
        Enumerable<Order> orders = Linq.of(
            new Order(1, "Book"),
            new Order(1, "Pen"),
            new Order(2, "Desk"));
        var result = customers.join(orders,
            Customer::id,
            Order::cid,
            (c, o) -> c.name() + " bought " + o.product()
        ).toList();
        assertEquals(3, result.size());
        assertTrue(result.contains("Alice bought Book"));
        assertTrue(result.contains("Alice bought Pen"));
        assertTrue(result.contains("Bob bought Desk"));
    }

    @Test
    void last() {
        assertEquals(3, Linq.of(1, 2, 3).last());
        assertThrows(NoSuchElementException.class, Linq.empty()::last);
    }

    @Test
    void last_withPredicate() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3, 4);
        assertEquals(4, seq.last(x -> x % 2 == 0));
    }

    @Test
    void lastOrDefault() {
        assertEquals(3, Linq.of(1, 2, 3).lastOrDefault(-1));
        assertEquals(-1, Linq.empty().lastOrDefault(-1));
    }

    @Test
    void leftJoin() {
        record Customer(int id, String name) {}
        record Order(int cid, String product) {}
        Enumerable<Customer> customers = Linq.of(
            new Customer(1, "Alice"),
            new Customer(2, "Bob"),
            new Customer(3, "Charlie"));
        Enumerable<Order> orders = Linq.of(
            new Order(1, "Book"),
            new Order(1, "Pen"));
        List<String> result = orders.leftJoin(
            customers,
            Order::cid,
            Customer::id,
            (o, c) -> (c == null ? "Unknown" : c.name()) + " bought " + o.product()
        ).toList();
        System.out.println(result);
        assertEquals(2, result.size()); // LEFT JOIN keeps orders only: Alice 2 rows
    }

    @Test
    void maxByInt() {
        record Person(int age) {}
        Enumerable<Person> people = Linq.of(new Person(20), new Person(30), new Person(25));
        assertEquals(30, people.maxByInt(Person::age));
    }

    @Test
    void maxBy() {
        record Person(String name, int age) {}
        Enumerable<Person> people = Linq.of(
            new Person("A", 20),
            new Person("B", 30),
            new Person("C", 25));
        Person oldest = people.maxBy(Person::age);
        assertEquals("B", oldest.name());
    }

    @Test
    void extractTo() {
        Enumerable<Object> mixed = Linq.of("s", 1, "t", 2);
        List<String> strings = mixed.extractTo(String.class).toList();
        assertEquals(List.of("s", "t"), strings);
    }

    @Test
    void order() {
        Enumerable<Integer> nums = Linq.of(3, 1, 2);
        assertEquals(List.of(1, 2, 3), nums.order().toList());
    }

    @Test
    void orderBy() {
        record Person(String name) {}
        Enumerable<Person> people = Linq.of(new Person("Bob"), new Person("Alice"));
        List<Person> sorted = people.orderBy(Person::name).toList();
        assertEquals("Alice", sorted.get(0).name());
        assertEquals("Bob", sorted.get(1).name());
    }

    @Test
    void rightJoin() {
        record Customer(int id, String name) {}
        record Order(int cid, String product) {}
        Enumerable<Customer> customers = Linq.of(
            new Customer(1, "Alice"),
            new Customer(2, "Bob"));
        Enumerable<Order> orders = Linq.of(
            new Order(1, "Book"),
            new Order(3, "Desk"));
        List<String> result = customers.rightJoin(
            orders,
            Customer::id,
            Order::cid,
            (c, o) -> (c == null ? "Unknown" : c.name()) + " - " + o.product()
        ).toList();
        System.out.println(result);
        assertEquals(2, result.size());
    }

    @Test
    void selectToObj() {
        Enumerable<Integer> nums = Linq.of(1, 2, 3);
        List<String> strings = nums.selectToObj(i -> "n" + i).toList();
        assertEquals(List.of("n1", "n2", "n3"), strings);
    }

    @Test
    void selectMany() {
        Enumerable<String> sentences = Linq.of("hello world", "java test");
        List<String> words = sentences.selectMany(s -> Linq.of(s.split(" "))).toList();
        assertEquals(List.of("hello", "world", "java", "test"), words);
    }

    @Test
    void selectMany_withResultSelector() {
        Enumerable<String> sentences = Linq.of("hello world");
        List<String> result = sentences.selectMany(
            s -> Linq.of(s.split(" ")),
            (s, w) -> s + ":" + w
        ).toList();
        assertEquals(List.of("hello world:hello", "hello world:world"), result);
    }

    @Test
    void shuffle() {
        Enumerable<Integer> nums = Linq.of(1, 2, 3, 4, 5);
        List<Integer> shuffled = nums.shuffle(x -> true).toList();
        assertEquals(5, shuffled.size());
        assertTrue(shuffled.containsAll(List.of(1,2,3,4,5)));
    }

    @Test
    void single() {
        assertEquals(42, Linq.of(42).single());
        assertThrows(NoSuchElementException.class, Linq.empty()::single);
        assertThrows(IllegalStateException.class, Linq.of(1,2)::single);
    }

    @Test
    void single_withPredicate() {
        Enumerable<Integer> seq = Linq.of(1, 2, 3, 4);
        assertEquals(2, seq.single(x -> x == 2));
        assertThrows(IllegalStateException.class, () -> seq.single(x -> x % 2 == 0));
    }

    @Test
    void singleOrDefault() {
        assertEquals(42, Linq.of(42).singleOrDefault(-1));
        assertEquals(-1, Linq.empty().singleOrDefault(-1));
        assertThrows(IllegalStateException.class, () -> Linq.of(1,2).singleOrDefault(-1));
    }

    @Test
    void skip() {
        Enumerable<Integer> seq = Linq.of(1,2,3,4,5);
        assertEquals(List.of(3,4,5), seq.skip(2).toList());
        assertEquals(Linq.of(1,2,3,4,5).toList(), seq.skip(-1).toList());
    }

    @Test
    void skipLast() {
        Enumerable<Integer> seq = Linq.of(1,2,3,4,5);
        assertEquals(List.of(1,2,3), seq.skipLast(2).toList());
    }

    @Test
    void skipWhile() {
        Enumerable<Integer> seq = Linq.of(1,2,3,4,5,1);
        assertEquals(List.of(3,4,5,1), seq.skipWhile(x -> x < 3).toList());
    }

    @Test
    void take() {
        Enumerable<Integer> seq = Linq.of(1,2,3,4,5);
        assertEquals(List.of(1,2,3), seq.take(3).toList());
    }

    @Test
    void takeLast() {
        Enumerable<Integer> seq = Linq.of(1,2,3,4,5);
        assertEquals(List.of(4,5), seq.takeLast(2).toList());
    }

    @Test
    void takeWhile() {
        Enumerable<Integer> seq = Linq.of(1,2,3,4,5,1);
        assertEquals(List.of(1,2), seq.takeWhile(x -> x < 3).toList());
    }

    @Test
    void sum() {
        Enumerable<Integer> nums = Linq.of(1,2,3);
        assertEquals(6L, nums.sumByInt(i -> i));
        assertEquals(6.0, nums.sumByDouble(i -> i));
        assertEquals(6L, nums.sumByLong( i -> i));
    }

    @Test
    void toArray() {
        Enumerable<String> seq = Linq.of("a", "b");
        assertArrayEquals(new String[]{"a", "b"}, seq.toArray());
    }

    @Test
    void toSet() {
        Enumerable<Integer> seq = Linq.of(1,2,2,3);
        assertEquals(Set.of(1,2,3), seq.toSet());
    }

    @Test
    void toList() {
        Enumerable<Integer> seq = Linq.of(1,2,3);
        assertEquals(List.of(1,2,3), seq.toList());
    }

    @Test
    void toStream() {
        Enumerable<String> seq = Linq.of("x", "y");
        List<String> list = seq.toStream().collect(Collectors.toList());
        assertEquals(List.of("x", "y"), list);
    }

    @Test
    void union() {
        Enumerable<Integer> a = Linq.of(1,2,3);
        Enumerable<Integer> b = Linq.of(3,4,5);
        List<Integer> union = a.union(b).toList();
        assertEquals(5, union.size());
        assertTrue(union.containsAll(List.of(1,2,3,4,5)));
    }

    @Test
    void unionBy() {
        record Person(int id) {}
        Enumerable<Person> a = Linq.of(new Person(1), new Person(2));
        Enumerable<Person> b = Linq.of(new Person(2), new Person(3));
        List<Person> union = a.unionBy(b, Person::id).toList();
        assertEquals(3, union.size()); // ids 1,2,3
    }

    @Test
    void where() {
        Enumerable<Integer> nums = Linq.of(1,2,3,4,5);
        assertEquals(List.of(2,4), nums.where(x -> x % 2 == 0).toList());
    }

    @Test
    void zip() {
        Enumerable<Integer> nums = Linq.of(1,2,3);
        Enumerable<String> strs = Linq.of("a","b","c");
        List<Pair<Integer,String>> zipped = nums.zip(strs).toList();
        assertEquals(3, zipped.size());
        assertEquals(1, zipped.get(0).getLeft());
        assertEquals("a", zipped.get(0).getRight());
    }

    @Test
    void zip_withFunction() {
        Enumerable<Integer> nums = Linq.of(1,2,3);
        Enumerable<String> strs = Linq.of("a","b","c");
        List<String> zipped = nums.zip(strs, (n,s) -> n + s).toList();
        assertEquals(List.of("1a", "2b", "3c"), zipped);
    }

    @Test
    void forEach() {
        Enumerable<Integer> seq = Linq.of(1,2,3);
        List<Integer> list = new ArrayList<>();
        seq.forEach(list::add);
        assertEquals(List.of(1,2,3), list);
    }

    @Test
    public void testRange() {
        List<Integer> list = Linq.range(1, 5)
            .toList();
        System.out.println(list);

        Linq.range(1, 4, 2)
            .forEach(System.out::println);
    }

    @Test
    public void testRepeat() {
        Linq.repeat("A", 10)
            .selectToObj(String::toLowerCase)
            .forEach(System.out::println);

    }


    @Test
    public void test1() {
        var a = Linq.of(1, 2, 2, 3, 4);
        var b = Linq.of(3, 4, 5);

        var distinct = a.distinct().toList();     // [1, 2, 3, 4]
        var intersect = a.intersect(b).toList();  // [3, 4]

        System.out.println(distinct);
        System.out.println(intersect);
    }
}