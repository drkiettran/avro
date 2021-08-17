# Sample project using Apache Avro
This is a Spring Boot application. An example of how to use Avro to serialize
and deserialize an object in Java. 

It is a RESTful application that has the following REST APIs:

- Post users (`/users`) - no duplication

```shellscript
curl --location --request POST 'localhost:8080/users' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--data-raw '
    { 
        "users" : [{
            "name": "Last, First",
            "favorite_number": 12345,
            "favorite_color": "Red, White and Blue"
        },
        {
            "name": "Doe, John",
            "favorite_number": 23456,
            "favorite_color": "Red, White and Blue"
        }]
    }'
```

- Get users (`/users`) - get all users in memory

```shellscript
curl --location --request GET 'localhost:8080/users' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json'
```

- Get user (`/user`) - user matching pattern on user name

```shellscript
curl --location --request GET 'localhost:8080/user?user=Last,%20First_' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json'
```

Required headers:

```
Content-type : application/json
Accept : application/json
```

The application requires the following schema stored in a file `user.avsc`.

```json
{"namespace": "example.avro",
 "type": "record",
 "name": "User",
 "fields": [
     {"name": "name", "type": "string"},
     {"name": "favorite_number",  "type": ["int", "null"]},
     {"name": "favorite_color", "type": ["string", "null"]}
 ]
}
```

## Build & run
This application requires Java 11. Therefore, in my case of using Ubuntu 18.04, I am setting 
the Java environment using the following commands:

```shell
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/
sudo update-alternatives --config java
```

Run this command to build:

```shellscript
mvn clean package
```

To run the program:

```shell
mvn clean spring-boot:run
```

You can also run after the build command `mvn clean package` as follows:

```shell
java -jar target/avro-0.0.1-SNAPSHOT.jar
```

## Override 
Use `-Dserver.port=xxxx` to override port number. So, run like this:

```shell
java -Dserver.port=9090 -jar target/avro-0.0.1-SNAPSHOT.jar
```