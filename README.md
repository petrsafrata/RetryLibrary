# Retry Library for Java 📖
![Java](https://img.shields.io/badge/Java-17%2B-green)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Open Source](https://img.shields.io/badge/Open%20Source-Yes-brightgreen)
![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-Active-brightgreen)
![Build](https://github.com/petrsafrata/RetryLibrary/actions/workflows/maven-release.yml/badge.svg)


A small, dependency-free Java library providing simple and readable retry logic for unstable or intermittent operations such as API calls, file access, database operations, remote service communication, OCR, AI model requests, and more.

The goal is to keep the API clean, fluent, and lightweight — without Spring, annotations, or heavy configuration.

---

## ✨ Features

- 🔁 Retry execution of failing operations
- 🧩 Fluent, expressive API
- ⚙️ Configurable attempts, delay, and retry conditions
- ⏳ Pluggable `DelayStrategy` support (fixed delay, exponential backoff, custom implementations)
- 📣 Listener callbacks (`onRetry`, `onSuccess`, `onFailure`)
- 📦 No dependencies — works in any Java project
- 🚀 Supports `Supplier<T>` and `Runnable` style operations
- 🛡️ Custom `RetryException` on final failure
- 📄 Provides a `RetryContext` object for advanced use cases

---

## 📂 Project Structure

```
src/main/java/cz/jpmad/retry/
├── Retry.java              # Entry-point factory for building retry operations
├── RetryConfig.java        # Fluent configuration for retry behavior
├── RetryExecutor.java      # Core retry loop engine
├── RetryException.java     # Exception thrown on final failure
├── RetryContext.java       # Immutable snapshot of retry attempt state
├── DelayStrategy.java      # Strategy interface for computing delay between attempts
│
├── functions/
│ ├── CheckedSupplier.java  # Supplier supporting checked exceptions
│ └── CheckedRunnable.java  # Runnable supporting checked exceptions
│
└── listeners/
└── RetryListener.java      # Callback interface for retry events
```

---

## 🚀 Installation

This library is published to **GitHub Packages**.  
To use it in your project, you need to add the GitHub Packages repository and the dependency.

### Maven

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/petrsafrata/RetryLibrary</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>cz.jpmad</groupId>
        <artifactId>retry-library</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

### **Gradle**

```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/petrsafrata/RetryLibrary")
    }
}

dependencies {
    implementation("cz.jpmad:retry-library:1.1.0")
}
```

## 🧠 Basic Usage

Retry a function that returns a value:

```java
String result = Retry
    .run(() -> callExternalService())
    .setMaxAttempts(5)
    .setDelayMillis(200)
    .setRetryOn(ex -> ex instanceof IOException)
    .setListener(new RetryListener() {
        @Override
        public void onRetry(RetryContext ctx) {
            System.out.println("Retry " + ctx.attempt());
        }

        @Override
        public void onFailure(RetryContext ctx) {
            System.out.println("Final failure: " + ctx.lastException());
        }

        @Override
        public void onSuccess(RetryContext ctx) {
            System.out.println("Success on attempt " + ctx.attempt());
        }
    })
    .execute();
```

Retry a void operation (Runnable):

```java
Retry
    .runVoid(() -> sendMessage())
    .setMaxAttempts(3)
    .setDelayMillis(100)
    .execute();
```

You can provide your own delay strategy implementation:

```java
DelayStrategy exponential = attempt -> 100L * (1L << (attempt - 1));

String result = Retry
        .run(() -> callExternalService())
        .setMaxAttempts(5)
        .setDelayStrategy(exponential)
        .execute();
```

## 🔧 RetryContext Example

The listener receives a RetryContext describing the current state:

```java
public record RetryContext(
    int attempt,
    int maxAttempts,
    long delayMillis,
    Exception lastException,
    boolean lastAttempt
) {}
```
Example:

```java
listener.onRetry(ctx -> {
    System.out.println("Attempt " + ctx.attempt() + " failed: " + ctx.lastException());
});
```

## 🧪 Testing

The library includes full unit tests covering:
- success on first attempt
- retry until success
- failure after max attempts
- retry condition predicates
- retrying void operations
- listener event ordering

You can run tests with:

```bash
mvn test
```

## 🤝 Contributing

Contributions are welcome!
Feel free to open issues or pull requests to improve the API, add strategies (exponential backoff, jitter), or expand documentation. 
Please read the [CONTRIBUTING.md](CONTRIBUTING.md) file for details.

## 📜 Licence

This project is open-source and released under the Apache License 2.0.
You are free to use, modify, distribute, and use it commercially under the terms of the Apache 2.0 license.
See the [LICENSE](LICENSE) file for full details.
```
Apache-2.0 – Copyright (c) 2025 Petr Šafrata
```