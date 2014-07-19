package com.sample.notification;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Created by rpatel on 7/16/14.
 */

@Component("RequestResponder")
@Scope("prototype")
public class RequestResponder extends UntypedActor {

    public static class GetNotifications{};

    private DeferredResult<String> result;
    private ActorRef notificationActor;

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public RequestResponder(DeferredResult<String> result, ActorRef notificationActor) {
        this.result = result;
        this.notificationActor = notificationActor;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("message in RequestResponder {}", message);
        if(message instanceof GetNotifications) {
            notificationActor.tell(new NotificationFSMMessages.SetTarget(getSelf()), getSelf());
        }if (message instanceof NotificationFSMMessages.Batch) {
            result.setResult(message.toString());
            getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }
}
