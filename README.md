# Promise for Java 8
Write a clean and intuitive asynchronous code in Java without a pain, the same way as [ECMAScript 6.0](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise) does:

```java
getJSON("http://github.com").
        then(System.out::println).
        catchVoid(Throwable::printStackTrace);
}
```

which is an asynchronous alternative to the following piece of blocking code:

```java
try {
    json = getJSON("http://github.com");
    System.out.println(json);
} catch (Throwable e) {
    e.printStackTrace();
}
```

[![Build status](https://travis-ci.org/jacekolszak/promise.svg?branch=master)](https://travis-ci.org/jacekolszak/promise)
[![Gitter](https://badges.gitter.im/jacekolszak/promises.svg)](https://gitter.im/jacekolszak/promises?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

## What is a Promise?
* **placeholder** for a value (or an exception) returned from an **asynchronous** operation
* internally a promise has three states: pending, resolved, rejected
* after a promise is resolved or rejected its state and value can never be changed
* asynchronous code can be written by chaining promises together
* exception thrown by a promise is propagated through promise chains

## Why would I use it?
* because it is better suited for writing asynchronous code than Java 8's CompletableFuture
    * API is much cleaner and easier to understand - just 2 instance method types (_thenXXX_, _catchXXX_) and 4 static factory methods (_all_, _race_, _resolve_, _reject_) solving most of the problems
    * much better handling of exceptions
        * exceptions thrown are not wrapped
        * then/catch callbacks could throw checked exceptions
        * Promise's executor code could throw an exception which will be handled by the Promise (no need to manually try/catch)
        * therefore exceptions can be caught and processed similar way as they are normally handled in a blocking code
    * Promise API enforces programmer to use an asynchronous programming, no way to run get() and block the current thread
    * code written using Promises is a little bit less verbose and easier to understand
* well tested (100% code covered by unit tests)

## When it should not be used?
* Promise is for one-shot operations, that is, you can execute some method and get a self-contained response (or error), i.e. get some REST resource
* When you need to monitor progress of the execution or process a stream of events then use something like [RxJava](https://github.com/ReactiveX/RxJava) instead

## Examples

```java
public void loadFakeJSON() {
    getJSON("http://github.com").
            thenReturn(json -> json.get("someProperty")).
            then(System.out::println).
            catchVoid(Throwable::printStackTrace);
}

public void loadJSONSequentially() {
    getJSON("http://github.com").
            thenPromise(json -> getJSON(json.get("otherURL"))).
            then(System.out::println).
            catchVoid(Throwable::printStackTrace);
}

public void catchFallback() {
    getJSON("http://unreliable-url.com").
            catchReturn(json -> {
                Map<String, String> fallbackJSON = new HashMap<>();
                fallbackJSON.put("someProperty", "defaultValue");
                fallbackJSON.put("otherURL", "http://default-url.com");
                return fallbackJSON;
            }).
            then(System.out::println).
            catchVoid(Throwable::printStackTrace);
}

public void all() {
    Promise.all(
            getJSON("https://fake-url.com/resources/1"),
            getJSON("https://fake-url.com/resources/2"),
            getJSON("https://fake-url.com/resources/3")
    ).then(jsons -> {
        System.out.println(jsons[0]);
        System.out.println(jsons[1]);
        System.out.println(jsons[2]);
    });
}

public void race() {
    Promise.race(
            getJSON("https://fake-url.com/resources/1"),
            getJSON("https://fake-url.com/resources/2"),
            getJSON("https://fake-url.com/resources/3")
    ).then(System.out::println);
}

public void timers() {
    timeout(getJSON("http://github.com"), 100).
            then(System.out::println).
            catchVoid(Throwable::printStackTrace);

    delay(100).then(v -> getJSON("http://github.com"));
}

private Promise<Map<String, String>> getJSON(String url) {
    return new Promise<>(p -> {
        // execute HTTP request asynchronously here (some Netty based client etc.)
        Map<String, String> json = new HashMap<>();
        json.put("someProperty", "value");
        json.put("otherURL", "http://google.com");
        p.resolve(json);
    });
}
```

Source code: [PromiseSamples.java](src/test/java/com/github/jacekolszak/promises/samples/PromiseSamples.java)

## How to use with Maven

Add repository:

```xml
<repositories>
    <repository>
        <id>jacekolszak</id>
        <url>https://dl.bintray.com/jacekolszak/maven</url>
    </repository>
</repositories>
```

And use the artifact like this:

```xml
<dependency>
	    <groupId>com.github.jacekolszak</groupId>
	    <artifactId>promises</artifactId>
	    <version>1.0</version>
</dependency>
```

## How to use with Gradle

Add repository to your build.gradle file:

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/jacekolszak/maven" 
    }    
}
```

And use the artifact like this:

```groovy
dependencies {
    compile  "com.github.jacekolszak:promises:1.0"
}
```

## Project goals
* Make API looking and behaving exactly the same as ECMAScript 6.0 Promises
* Use all bleeding edge features of Java 8
* API should allow to write code which is concise and easy to reason about 
* API could be used with any other libraries and frameworks which executes code asynchronously (i.e. [Netty](https://github.com/netty/netty))
* Promise created using API has to be thread safe
