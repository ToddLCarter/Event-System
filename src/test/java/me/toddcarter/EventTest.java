package me.toddcarter;

import me.toddcarter.event.EventPriority;
import me.toddcarter.subscription.Subscription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class EventTest {

    @Test
    public void testRegister() {
        Events.subscribe(TestEvent.class).handler( event -> {
            event.test = true;
        });
        TestEvent event = new TestEvent();
        Events.callSync(event);
        Assert.assertTrue(event.test);
    }

    @Test
    public void testUnregister() {
        Subscription<TestEvent> listener = Events.subscribe(TestEvent.class).handler( event -> {
            Assert.fail("Event subscriber wasn't unregistered");
        });
        Events.unregisterListener(listener);

        TestEvent event = new TestEvent();
        Events.callSync(event);
    }

    @Test
    public void testCallSync() {
        TestEvent event = new TestEvent();
        Events.callSync(event);
    }

    @Test
    public void testCallAsync() {
        TestEvent event = new TestEvent();
        Events.callAsync(event);
    }

    @Test
    public void testIgnoreCancelled() {

        Events.subscribe(TestCancellableEvent.class)
                .ignoreCancelled()
                .handler( event -> {
                    Assert.fail("Subscriber did not ignore cancelled event");
                });

        TestCancellableEvent event = new TestCancellableEvent();
        event.setCancelled(true);
        Events.callSync(event);
    }

    @Test
    public void testExpireAfter() {
        Events.subscribe(TestEvent.class)
                .expireAfter(2)
                .handler( event -> {
                    event.expire++;
                });

        TestEvent event = new TestEvent();

        Events.callSync(event);
        Events.callSync(event);
        Events.callSync(event);

        Assert.assertEquals(2, event.expire);
    }

    @Test
    public void testFilter() {
        Events.subscribe(TestEvent.class)
                .filter(event -> event.filter > 0)
                .handler(event -> {
                    Assert.assertEquals(1, event.filter);
                });

        TestEvent event = new TestEvent();
        event.filter = 0;
        Events.callSync(event);
    }

    @Test
    public void testPriority() {
        //LOWEST PRIORITY
        Events.subscribe(TestEvent.class, EventPriority.LOWEST).handler(event -> {
            Assert.assertEquals(0, event.priority++);
        });

        //LOW PRIORITY
        Events.subscribe(TestEvent.class, EventPriority.LOW).handler(event -> {
            Assert.assertEquals(1, event.priority++);
        });

        //NORMAL PRIORITY
        Events.subscribe(TestEvent.class, EventPriority.NORMAL).handler(event -> {
            Assert.assertEquals(2, event.priority++);
        });

        //HIGH PRIORITY
        Events.subscribe(TestEvent.class, EventPriority.HIGH).handler(event -> {
            Assert.assertEquals(3, event.priority++);
        });

        //HIGHEST PRIORITY
        Events.subscribe(TestEvent.class, EventPriority.HIGHEST).handler(event -> {
            Assert.assertEquals(4, event.priority++);
        });

        //MONITOR PRIORITY
        Events.subscribe(TestEvent.class, EventPriority.MONITOR).handler(event -> {
            Assert.assertEquals(5, event.priority++);
        });


        TestEvent event = new TestEvent();
        Events.callSync(event);
        Assert.assertEquals("Test Event should have been fired 6 times", 6, event.priority);
    }



}
