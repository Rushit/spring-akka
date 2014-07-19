package com.sample.notification;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by rpatel on 7/16/14.
 */
@Component("NotificationActorB")
@Scope("prototype")
public class NotificationActor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static class Count {}

    public static class Get {}

    // the service that will be automatically injected
    @Autowired
    NotificationService notificationService;

    private int count = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("message - {}", message);
        if (message instanceof Count) {
            count = notificationService.increment(count);
        } else if (message instanceof Get) {
            getSender().tell(count, getSelf());
        } else {
            unhandled(message);
        }
    }
}