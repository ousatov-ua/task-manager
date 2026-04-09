# task-manager

[![Build](https://github.com/ousatov-ua/task-manager/actions/workflows/maven.yml/badge.svg)](https://github.com/ousatov-ua/task-manager/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ousatov-ua/task-manager)](https://central.sonatype.com/artifact/io.github.ousatov-ua/task-manager)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Visitors](https://visitor-badge.laobi.icu/badge?page_id=ousatov-ua.task-manager)](https://github.com/ousatov-ua/task-manager)
[![GitHub commits](https://img.shields.io/github/commit-activity/t/ousatov-ua/task-manager)](https://github.com/ousatov-ua/task-manager/commits/main)
[![GitHub last commit](https://img.shields.io/github/last-commit/ousatov-ua/task-manager)](https://github.com/ousatov-ua/task-manager/commits/main)

A lightweight Java 21 (use task-manager < 3.0.0) /25 (use task-manager >= 3.0.0) library for **parallel task processing with built-in back-pressure**.

It uses two bounded blocking queues — one for submitted work units and one for the thread pool — so producers never outrun consumers and your application stays safe from `OutOfMemoryError` under load.

---

## How it works

```
Your code
   │ submit(workUnit)   ← blocks when queue is full (back-pressure)
   ▼
workUnitsDeque  ──►  dispatch thread  ──►  tasksDeque  ──►  thread pool
                                                               │
                                              Function<T, R> ─┘
```

Two configurable queue sizes + a fixed thread pool = predictable memory usage at any throughput.

---

## Installation

```xml
<dependency>
    <groupId>io.github.ousatov-ua</groupId>
    <artifactId>task-manager</artifactId>
    <version><!-- see latest on Maven Central --></version>
</dependency>
```

---

## Quick start

### 1. Implement `WorkUnit`

```java
@Builder
public record MyTask(String id, String payload) implements WorkUnit {

    public static final MyTask LAST = new MyTask("__last__", null);

    @Override
    public WorkUnit getLastUnit() { return LAST; }

    @Override
    public String getType() { return "my-task"; }
}
```

### 2. Subclass `TaskManager` (optional — only if you need error tracking)

```java
public class MyTaskManager extends TaskManager<MyTask, Boolean> {

    public MyTaskManager(Config config, Function<MyTask, Boolean> fn) {
        super(config, fn);
    }

    @Override
    protected boolean isInError(Boolean result) { return !result; }
}
```

### 3. Run it

```java
var config = Config.builder()
    .eventProcessingParallelism(10)   // thread pool size
    .workUnitsDequeSize(500)          // max pending tasks
    .tasksDequeSize(500)
    .build();

try (var manager = new MyTaskManager(config, task -> process(task))) {

    for (var item : myItems) {
        manager.submit(new MyTask(item.id(), item.payload()));
    }

    // Signal end-of-stream and wait for all tasks to finish
    manager.waitForCompletion(MyTask.LAST);
    manager.logStatistics();
}
```

## Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `eventProcessingParallelism` | 20 | Thread pool size |
| `workUnitsDequeSize` | 200 | Max pending `WorkUnit`s before `submit()` blocks |
| `tasksDequeSize` | 200 | Max queued tasks inside the thread pool |
| `logForRecordCount` | 100 | Log progress every N processed units (per type) |
| `waitTimeForAllTasksFinishedMinute` | 30 | `close()` timeout in minutes |
| `waitTimeForCheckingFinishedSeconds` | 10 | Polling interval in `waitForCompletion()` |

---

## License

[MIT](https://opensource.org/licenses/MIT) © Oleksii Usatov
