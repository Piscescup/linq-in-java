# `LINQ` in Java Project

## Introduction: `LINQ`

[Language Integrated Query](https://learn.microsoft.com/en-us/dotnet/csharp/linq/) (`LINQ`) is a powerful feature in `.NET`
that allows developers to write queries directly in their programming language.

It provides a consistent and intuitive way to query various data sources,
including collections, databases, XML, and more.<br>  
`LINQ` enables developers to express complex queries in a concise and readable manner,
improving code maintainability and reducing the likelihood of errors.

### `LINQ` Expressions

In `C#`, there is 2 syntax to write `LINQ` expression: **Query Syntax** and **Method Syntax**.<br>
In **Query Syntax**, take the following code as an example:

```csharp
// Specify the data source.
int[] scores = [97, 92, 81, 60];

// Define the query expression.
IEnumerable<int> scoreQuery =
    from score in scores
    where score > 80
    select score;

// Execute the query.
foreach (var i in scoreQuery)
{
    Console.Write(i + " ");
}

// Output: 97 92 81
```

In **Method Syntax**, the same query can be written as:

```csharp
// Specify the data source.
int[] scores = [97, 92, 81, 60];

// Define the query expression.
IEnumerable<int> scoreQuery = scores
    .Where(score => score > 80);

// Execute the query.
foreach (var i in scoreQuery)
{
    Console.Write(i + " ");
}
// Output: 97 92 81
```

### Stream API in Java

In Java, the `Stream API` provides a similar functionality to `LINQ` in C#.
It allows developers to perform functional-style operations on collections of data,
such as filtering, mapping, and reducing.

The `Stream API` is part of the `java.util.stream` package and was introduced in Java 8.
Here is an example of how to use the `Stream API` in Java to filter a list

```java
import java.util.stream.Stream;

void main() {
    Stream.of(97, 92, 81, 60)
        .filter(score -> score > 80)
        .forEach(score -> System.out.print(score + " "));
}
```

But the `Stream API` is not as powerful and concise as `LINQ` in C#.<br>
Take group people as an example, in C#, we can write:
```csharp
record Person(string Name, int Age, string Address) {
    public String Email() {
        return $"{Name.ToLower()}{Age}@{Name.ToLower()}.com";
    }
}

var people = new List<Person> {
    new Person("Alice", 30, "123 Main St"),
    new Person("Bob", 25, "456 Elm St"),
    new Person("Charlie", 35, "789 Oak St"),
    new Person("David", 30, "123 Main St"),
    new Person("Eve", 25, "456 Elm St"),
    new Person("Frank", 35, "789 Oak St"),
};

var groupedPeople = people
    .OrderBy(p => p.Age)
    .ThenBy(p => p.Name)
    .ThenBy(p => p.Address)
    .Select(p => p.Email())
    .ToList();
  
foreach (var email in groupedPeople)
{
    Console.WriteLine(email);
}

// Output:
// bob25@bob.com
// eve25@eve.com
// alice30@alice.com
// david30@david.com
// charlie35@charlie.com
// frank35@frank.com
```

In Java, we have to write more code to achieve the same result:

```java
import java.util.*;

void main() {
    record Person(String name, int age, String address) {
        public String Email() {
            return String.format("%s%d@%s.com", name.toLowerCase(), age, Name.toLowerCase());
        }
    }

    List<Person> people = List.of(
        new Person("Alice", 30, "123 Main St"),
        new Person("Bob", 25, "456 Elm St"),
        new Person("Charlie", 35, "789 Oak St"),
        new Person("David", 30, "123 Main St"),
        new Person("Eve", 25, "456 Elm St"),
        new Person("Frank", 35, "789 Oak St")
    );

    List<String> groupedPeople = people.stream()
        .sorted(Comparator.comparing(Person::age)
            .thenComparing(Person::Name)
            .thenComparing(Person::address))
        .map(Person::Email)
        .toList();

    groupedPeople.forEach(System.out::println);
}
// Output:
// bob25@bob.com
// eve25@eve.com
// alice30@alice.com
// david30@david.com
// charlie35@charlie.com
// frank35@frank.com
```
---

## `LINQ` in Java

This project aims to implement a `LINQ` library in `Java`, 
providing a more concise and powerful way to query collections of data.<br>

The library will include features such as filtering, mapping, grouping, and sorting,
similar to the capabilities of `LINQ` in `C#`. <br>

The goal is to make it easier for Java developers to write expressive and maintainable code 
when working with collections of data.<br>

---

## Features

`LINQ in Java` provides a set of composable, lazy-evaluated query operations inspired by `C# LINQ`.

### Core Design

- **Lazy Evaluation** – All operations are deferred until terminal operations.
- **Composable Pipelines** – Query operators can be chained fluently.
- **Fresh Enumerator Model** – Each enumeration produces an independent iterator.


### API
`Enumerable` API includes a lot of methods, such as:
- **Filtering API** – Filter elements based on predicates (`where`, `extractTo`, `distinct`).
- **Mapping API** – Transform elements into new forms (`select`, `selectMany`).
- **Partitioning API** – Control sequence slicing (`skip`, `take`, `skipWhile`, `takeWhile`).
- **Sorting API** – Order elements by keys or comparators (`orderBy`, `thenBy`, `orderDescending`).
- **Grouping API** – Group elements by keys (`groupBy`, `groupResultBy`).
- **Join API** – Correlate two sequences (`join`, `leftJoin`, `rightJoin`).
- **Set API** – Perform set-based operations (`union`, `intersect`, `except`).
- **Aggregation API** – Reduce sequences to scalar values (`aggregate`, `sum`, `min`, `max`, `average`, `count`).
- **Quantifier API** – Evaluate logical conditions (`any`, `all`, `contains`).
- **Element API** – Retrieve specific elements (`first`, `last`, `single`, `elementAt`).
- **Conversion API** – Materialize or transform sequences (`toList`, `toSet`, `toMap`).

Factory method are provided to create `Enumerable` in `Linq.java`:
- `Linq.of(T... items)` – Create an `Enumerable` from a varargs array.
- `Linq.fromIterable(Iterable<T> iterable)` – Create an `Enumerable` from an `Iterable` Object.
- `Linq.fromIterator(Iterator<T> iterator)` – Create an `Enumerable` from an `Iterator` Object.
- `Linq.fromArray(T[] array)` – Create an `Enumerable` from an array.
- `Linq.fromEnumerable(Enumerable<T> source)` – Create an `Enumerable` from an array.
- `Linq.range(int start, int count)` – Create an `Enumerable` that generates a sequence of integers.
- `Linq.range(int start, int count, int step)` – Create an `Enumerable` that generates a sequence of integers with a specified step.
- `Linq.repeat(T element, int count)` – Create an `Enumerable` that generates a sequence that contains repeated values.
- `Linq.empty()` – Create an empty `Enumerable` Object.

---

## Installation
To use `LINQ in Java`, you can add the dependency to your project .

Using Maven, add the following to your `pom.xml`:
```xml
<dependency>
    <groupId>io.github.piscescup</groupId>
    <artifactId>linq-in-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

Using Gradle, add the following to your `build.gradle`:
```groovy
dependencies {
    implementation 'io.github.piscescup:linq-in-java:1.0.0'
}
```

---

## Usage

Here are some examples demonstrating common LINQ-style operations.

---

### Filtering and Mapping

Filter even numbers and square them:

```java
import io.github.piscescup.linq.*;

void main() {
    var numbers = Linq.of(1, 2, 3, 4, 5, 6);

    var evenSquares = numbers
        .where(n -> n % 2 == 0)
        .selectToObj(n -> n * n)
        .toList();

    System.out.println(evenSquares); // [4, 16, 36]
}
````

### Aggregation

Compute the sum and average:

```java
void main() {
    var numbers = Linq.range(1, 5); // 1..5

    int sum = numbers.sumByInt(i -> i);
    double avg = numbers.intAverageNullable();

    System.out.println(sum); // 15
    System.out.println(avg); // 3.0
}
```

### Grouping

Group words by their first character:

```java
void main() {
    var words = Linq.of("apple", "ant", "banana", "blue");

    var groups = words.groupBy(w -> w.charAt(0));

    for (var group : groups) {
        System.out.println(group.key() + " -> " + group.elements());
    }
}

// Output:
// a -> [apple, ant]
// b -> [banana, blue]
```


### Sorting

Sort objects by multiple keys:

```java
record Person(String name, int age) {}

void main() {
    var people = Linq.of(
        new Person("Alice", 30),
        new Person("Bob", 25),
        new Person("Charlie", 25)
    );

    var sorted = people
        .orderBy(Person::age)
        .thenBy(Person::name)
        .toList();

    System.out.println(sorted);
}
```

### Joining

Join two sequences:

```java
record Customer(int id, String name) {}
record Order(int cid, String product) {}

void main() {
    // Left Join
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

    // Output:
    // [Alice bought Book, Alice bought Pen]
    
    
    // Right Join
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
    
    // Output:
    // [Alice - Book, Unknown - Desk]
}
```

### Set Operations

Remove duplicates and compute intersection:

```java
void main() {
    var a = Linq.of(1, 2, 2, 3, 4);
    var b = Linq.of(3, 4, 5);

    var distinct = a.distinct().toList();     // [1, 2, 3, 4]
    var intersect = a.intersect(b).toList();  // [3, 4]

    System.out.println(distinct);
    System.out.println(intersect);
}
```

### Quantifiers

Check conditions:

```java
void main() {
    var numbers = Linq.range(1, 5);

    boolean anyEven = numbers.any(n -> n % 2 == 0);
    boolean allPositive = numbers.all(n -> n > 0);

    System.out.println(anyEven);    // true
    System.out.println(allPositive); // true
}
```

### Sequence Generation

Generate ranges and repeated values:

```java
void main() {
    var range = Linq.range(10, 5).toList();        // [10, 11, 12, 13, 14]
    var stepped = Linq.range(10, 4, 2).toList();   // [10, 12, 14, 16]
    var repeat = Linq.repeat("A", 3).toList();     // ["A", "A", "A"]

    System.out.println(range);
    System.out.println(stepped);
    System.out.println(repeat);
}
```

---

## Contract
- GitHub: [linq-in-java Issue](https://github.com/Piscescup/linq-in-java/issues)
- Email: piscescup@outlook.com