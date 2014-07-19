package com.sample.notification;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import static com.sample.config.SpringExtension.SpringExtProvider;

/**
 * Created by rpatel on 7/18/14.
 */
@Component("NotificationManager")
@Scope("prototype")
public class NotificationManager extends UntypedActor {

    public static class NotificationRequest{
        public DeferredResult<String> result;
        public NotificationRequest(DeferredResult<String> result){
            this.result = result;
        }
    }

    public static class PushNewMessage{
        public String message;
        public PushNewMessage(String message){
            this.message = message;
        }
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {

        log.info("Message - {}", message);
        if( message instanceof NotificationRequest) {
            final ActorRef notifcationActor = getNotificationActor(true);
            String waterName = "watcher" + System.currentTimeMillis();
            final ActorRef watcher = getContext().actorOf(
                    SpringExtProvider.get(getContext().system()).props("RequestResponder",
                    ((NotificationRequest) message).result, notifcationActor), waterName);
            watcher.tell(new RequestResponder.GetNotifications(), getSelf());

        } else if ( message instanceof PushNewMessage) {
            final ActorRef notifcationActor = getNotificationActor(true);
            if (notifcationActor != null){
                log.info("actor found in push");
                notifcationActor.tell(new NotificationFSMMessages.Queue(((PushNewMessage) message).message), getSelf());
            }else{
                log.info("actor not found in push");
                //do nothing
            }
        } else if (message instanceof CommonMessages.WhoAreYou){
            log.info("I am {}", getSelf().path());
        } else {
            unhandled(message);
        }
    }

    private ActorRef getNotificationActor(boolean createIfNE){
        ActorSelection actorSelection = getContext().actorSelection("user1");
        Timeout timeout = new Timeout(Duration.create(1, "seconds"));
        scala.concurrent.Future<ActorRef> futreActorRef = actorSelection.resolveOne(Timeout.durationToTimeout(
                new FiniteDuration(1, TimeUnit.SECONDS)));

        ActorRef actorRef = null;
        try {
            actorRef = Await.result(futreActorRef, timeout.duration());
        } catch (Exception ignore) {
            log.warning("failed search for actor", ignore);
        }

        log.info("actor found? {}", !(actorRef ==null));
        if(actorRef==null && createIfNE) {
            actorRef = getContext().actorOf(SpringExtProvider.
                    get(getContext().system()).props("NotificationActor"), "user1");
            log.info("Actor created");
            log.info("actor path {}", actorRef.path().name());
        }
        return actorRef;
    }

}
