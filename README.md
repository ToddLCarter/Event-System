# Event-System [![](https://jitpack.io/v/ToddLCarter/Event-System.svg)](https://jitpack.io/#ToddLCarter/Event-System)
A Thread Safe Event System

## Installation
Maven:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.ToddLCarter</groupId>
    <artifactId>Event-System</artifactId>
    <version>1.0.1</version>
</dependency>
```
Gradle:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ToddLCarter:Event-System:1.0.1'
}
```

## Building
You can build the project by running `./gradlew build`

# Usage

## Creating a new Event
Firstly, your class must extend Event
```
import me.toddcarter.event.Event;

public class ExampleEvent extends Event {
    
}
```

You must then add the following methods to your class:
```
import me.toddcarter.event.Event;
import me.toddcarter.event.HandlerList;

public class ExampleEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
```
Now you can Start adding your own functions to the event
```
import me.toddcarter.event.Event;
import me.toddcarter.event.HandlerList;

public class ExampleEvent extends Event {

    private String string;

    public ExampleEvent(String string) {
        this.string = string;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getString() {
        return string;
    }
}
```

### Making your event cancellable
You can simply make your event cancellable by implementing `Cancellable` into your event class
```
import me.toddcarter.event.Cancellable;
import me.toddcarter.event.Event;

public class ExampleEvent extends Event implements Cancellable {

    private boolean cancelled;
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
```

## Calling your event
### Calling your event synchronously 
```
ExampleEvent event = new ExampleEvent("Event Call");
Events.callSync(event);
```
### Calling your event Asynchronously
```
ExampleEvent event = new ExampleEvent("Async Event call");
Events.callAsync(event);
```

## Creating your own Listener
Creating your listener is as easy as:
```
Events.subscribe(ExampleEvent.class).handler(e ->  System.out.println(e.getString()));
```
However, Event-System exposes multiple other quality-of-life functions for creating listeners:
```
Events.subscribe(ExampleEvent.class)
    .expireAfter(1)
    .expireAfter(10, TimeUnit.SECONDS)
    .filter(e -> e.getString().equalsIgnoreCase("Event Call"))
    .onError((e, throwable) -> System.out.println("Error handling event: " + e.getClass().getSimpleName()))
    .handler(e ->  System.out.println(e.getString()));
```

### Listener Filters
Filters will prevent the listener from being executed unless all filters are met

You can apply an indefinite amount of filters to your listeners using lambda expressions with the `filter` method
```
Events.subscribe(ExampleEvent.class)
    .filter(e -> e.getString().equalsIgnoreCase("Event Call"))
    .filter(e -> e.getOwnerName().equalsIgnoreCase("Lora"))
    .handler(e ->  System.out.println(e.getString()));
```

However, Event-System also contains some pre-built filters:

#### Ignore Cancelled Events
The following listener will not execute if the event has already been cancelled by a previous listener
```
Events.subscribe(ExampleEvent.class)
    .ignoreCancelled()
    .handler(e ->  System.out.println(e.getString()));
```

### Listener Expiration
Expirations can limit the amount of times a listener can be executed

You can apply expiration conditions to your listeners using lambda expressions with the `expireIf` method
```
Events.subscribe(ExampleEvent.class)
    .expireIf(s -> s.getCallCounter() > 10)
    .handler(e ->  System.out.println(e.getString()));
```

However, Event-System also contains some pre-built expiration conditions:

#### Expire after n calls
The following code will expire the listener after it has been executed 3 times
```
Events.subscribe(ExampleEvent.class)
    .expireAfter(3)
    .handler(e ->  System.out.println(e.getString()));
```

#### Expire after n time
The following code will expire the listener after 30 seconds of being registered
```
Events.subscribe(ExampleEvent.class)
    .expireAfter(30, TimeUnit.SECONDS)
    .handler(e ->  System.out.println(e.getString()));
```

## Error Handling
You can create a custom expression for if an exception is thrown whilst executing the listener
```
Events.subscribe(ExampleEvent.class)
    .onError((e, throwable) -> throwable.printStackTrace())
    .handler(e ->  System.out.println(e.getString()));
```
