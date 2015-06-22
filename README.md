# jtom
Java Tarantool Object Mapper

Requirements:
- Java 1.8

Uses:

Connections:
Init your connection pool:
Example:
```
ConnectionPool.init(Arrays.asList(
             new ConnectionInfo(TarantoolConnection.class, "tarantool_connection_1", host1, port1, login1, password1),
             new ConnectionInfo(TarantoolConnection.class, "tarantool_connection_2", host2, port2, login2, password2)));
```

ConnectionInfo contain information about connection type, name, host, port, login and password, use it to init pool


Entities:
Mark @Entity your entities to link it to Tarantool spaces.
space - tarantool space name
connection - tarantool connection name registred in ConnectionPool

Add @Indexes to make class know about indexes.
As value it uses array of @Index

@Index indexType - type of Tarantool index, name - name of index

Add @Key for key fields. position - key position, index - index by order in @Indexes value

Add @Field for fields. position - field position. If you skip some positions, data will stored by null value

Example:
```
@Entity (space="space_entity" connection="tarantool_connection_1")
@Indexes(value = {@Index(indexType = IndexType.INDEX_TYPE_HASH, name = "primary")})
public class Foo {
    @Key(position = 1, index = 0)
    @Field(position = 0)
    String id;

    @Field(position = 1)
    Long field1;

    @Field(position = 2)
    String field2;

   ...
   //getters and setters
   ...
}
```

Usage example:
```
DAO<Foo> dao = TarantoolImpl.getByClass(Foo.class);
Foo entity = new Foo();
entity.setId("some key value")
      .setField1(2134819234L)
      .setField2(348912348L);
//save/insert entity:
dao.save(entity);

//select/get entity:
entity = dao.get(0, Collections.singletonList("some key value")).getObjectIterator().next();
or
entity = dao.getById("some key value").getObjectIterator().next();

//remove/drop entity:
dao.drop(entity);
```
