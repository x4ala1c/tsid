# TSID (Time-Sorted ID)

- Implementation of [Snowflake ID](https://developer.twitter.com/en/docs/twitter-ids).
- Migrated from my [previous project](https://github.com/vincentdaogithub/tsid).
- Inspired by:
    - [Vlad Mihalcea](https://github.com/vladmihalcea) and his posts:
        - [The best way to generate a TSID entity identifier with JPA and Hibernate.](https://vladmihalcea.com/tsid-identifier-jpa-hibernate/)
        - [The best UUID type for a database Primary Key.](https://vladmihalcea.com/uuid-database-primary-key/)
    - [f4b6a3](https://github.com/f4b6a3) and their series of UUID implementations, particularly
      their [TSID implementation.](https://github.com/f4b6a3/tsid-creator)
    - [Snowflake ID from X (formerly known as Twitter)](https://en.wikipedia.org/wiki/Snowflake_ID)
    - [Discord's Snowflake implementation](https://discord.com/developers/docs/reference#snowflakes)

## Introduction

TSID (Time-Sorted ID) is a type of ID that balances well between the look of the UUID and its ability to support
indexing for the database.

Its value is a `long` value (64-bit signed integer) and consists of three parts, in order:

1. 42-bit of timestamp (one sign bit + 41 timestamp bits), which is the difference between the epoch (customizable)
   and the ID's creation time.
   The timestamp is in milliseconds.
2. 10-bit representation of the node or machine that generates the ID. This reduces the ID's collision across the
   multi-node system (i.e., microservices, etc.).
3. 12-bit sequence for cases when multiple IDs are generated in the same millisecond.
   The starting of the sequence is securely randomized.

The String form of the Tsid is in [Crockford's Base32.](https://www.crockford.com/base32.html)

## Example

### Generate

```java
import io.x4ala1c.tsid.Tsid;
import io.x4ala1c.tsid.TsidConfiguration;
import io.x4ala1c.tsid.TsidGenerator;

public static void main(String[] args) {

    final TsidGenerator generator = TsidGenerator.globalGenerator();
    final Tsid simpleId = generator.generate();

    // Or, alternatively, generate straight from TsidGenerator.
    final Tsid quickId = TsidGenerator.globalGenerate();

    // Customized configuration.
    final TsidConfiguration configuration = TsidConfiguration.builder()
            .node(69)
            .epoch(69420)
            .build();
    final TsidGenerator customGenerator = TsidGenerator.generator(configuration);
    final Tsid customId = customGenerator.generate();

    // Quick generator from the thread.
    final TsidGenerator threadGenerator = TsidGenerator.threadGenerator();
    final Tsid threadId = threadGenerator.generate();
}
```

### Use `Tsid`

```java
import io.x4ala1c.tsid.Tsid;
import io.x4ala1c.tsid.TsidGenerator;

import java.util.Objects;

public static void main(String[] args) {

    final Tsid id = TsidGenerator.globalGenerate();

    // As long value.
    final long longValue = id.asLong();

    // As Crockford's Base32 String.
    final String stringValue = id.asString();

    // By default, toString() returns the same as asString();
    final boolean toStringIsTheSame = Objects.equals(stringValue, id.toString());     // true
}
```

### Spring/Spring Boot

Due to incompatibility between `@Id` and `@Convert`, we have to implement a workaround.

#### Entity

```java
import io.x4ala1c.tsid.Tsid;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public final class ExampleEntity {

    @EmbeddedId
    private ExampleId id;

    public ExampleEntity() {
    }

    public ExampleEntity(ExampleId id) {
        this.id = id;
    }

    @Embeddable
    public static final class ExampleId {

        @Convert(converter = TsidConverter.class)
        private Tsid id;

        public ExampleId() {
        }

        public ExampleId(Tsid id) {
            this.id = id;
        }

        // Getters/Setters...
    }

    // Getters/Setters...
}
```

#### Converter

```java
import io.x4ala1c.tsid.Tsid;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TsidConverter implements AttributeConverter<Tsid, Long> {

    @Override
    public Long convertToDatabaseColumn(Tsid attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.asLong();
    }

    @Override
    public Tsid convertToEntityAttribute(Long dbData) {
        if (dbData == null) {
            return null;
        }
        return Tsid.fromLong(dbData);
    }
}
```

#### References:

- [The workaround solution.](https://stackoverflow.com/questions/44069361/convert-on-id-field)
- [The issue in JPA project](https://github.com/jakartaee/persistence/issues/207). Hopefully, this becomes a reality.
- [The documentation on `@Convert`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/persistence/convert).
  Currently, it is not allowed to be accompanied by `@Id`.

## Story

The first project in my journey to find meaning.

I was browsing [Vlad's](https://github.com/vladmihalcea) blogs (and he posts very often on LinkedIn) when I encountered
myself in his talk about using UUID in a database.

I was, and still am, a big fan of said ID. I kinda hate the notion of non-fixed-length integer-based IDs.

And before I noticed, I have dived deeper into the rabbit hole of database IDs. Cool.

So, inspired by Vlad's implementation of TSID, and later on the series from [f4b6a3](https://github.com/f4b6a3), I
created my own implementation.

At first, I hosted this project on my main GitHub account ([here](https://github.com/vincentdaogithub/tsid)).

But things go awry in my personal life.

So yeah, I migrated the previous project into `x4ala1c` collection.

I also plan to migrate two other projects into this collection, so stay tuned, I guess.

2024-04-27 - Vincent Dao

## Credits

- I just found out `f4b6a3` added my previous project as one of the implementations, so I want to send my happy
  thank-you to them.
- And Vlad, for dumping out tons of crazy Java blogs.

## License

- This project is licensed under [MIT License](LICENSE).
- The license also included the one from [f4b6a3](https://github.com/f4b6a3/tsid-creator/blob/master/LICENSE), as they
  inspire this project.
