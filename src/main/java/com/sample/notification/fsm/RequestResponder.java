package com.sample.notification.fsm;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.duration.Duration;
import static com.sample.notification.fsm.NotificationFSMMessages.*;

/**
 * Created by rpatel on 7/16/14.
 */

@Component("RequestResponder")
@Scope("prototype")
public class RequestResponder extends UntypedActor {

    public static class GetNotifications{};
    public static class UnRegistered{};

    private DeferredResult<String> result;
    private ActorRef notificationActor;

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public RequestResponder(DeferredResult<String> result, ActorRef notificationActor) {
        this.result = result;
        this.notificationActor = notificationActor;
        getContext().setReceiveTimeout(Duration.create("29 seconds"));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug("message in RequestResponder {}", message);
        if(message instanceof GetNotifications) {
            notificationActor.tell(new SetTarget(), getSelf());
        } else if (message instanceof Batch) {
            result.setResult(message.toString());
            log.debug("kill pill by {}", message);
            getContext().stop(getSelf());
        } else if(message instanceof ReceiveTimeout) {
            result.setResult("no update");
            log.debug("kill pill by {}", message);
            notificationActor.tell(new RemoveTarget(), getSelf());
            getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }
}
