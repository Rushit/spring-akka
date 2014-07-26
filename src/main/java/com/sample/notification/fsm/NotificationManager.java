package com.sample.notification.fsm;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import static com.sample.config.ActorConfig.REGISTRY_MANAGER;
import static com.sample.config.SpringExtension.SpringExtProvider;
import static com.sample.notification.fsm.CommonMessages.*;
import static com.sample.notification.fsm.NotificationFSMMessages.*;
import static com.sample.notification.ActorRegistry.*;
import  scala.concurrent.Future;
/**
 * Created by rpatel on 7/18/14.
 */
@Component("NotificationManager")
@Scope("prototype")
public class NotificationManager extends UntypedActor {

    public NotificationManager(){
        setRegistry();
    }

    private ActorRef registryManager;
    public static class NotificationRequest {

        public DeferredResult<String> result;

        public NotificationRequest(DeferredResult<String> result) {
            this.result = result;
        }
    }

    public static class PushNewMessage {

        public String message;

        public PushNewMessage(String message) {
            this.message = message;
        }
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {

        log.debug("Message - {}", message);
        if (message instanceof NotificationRequest) {
            final ActorRef notifcationActor = getNotificationActor("user1");
            String waterName = "watcher" + System.currentTimeMillis();
            final ActorRef watcher = getContext().actorOf(SpringExtProvider.get(getContext().system()).props(
                            "RequestResponder", ((NotificationRequest) message).result, notifcationActor), waterName
            );
            watcher.tell(new RequestResponder.GetNotifications(), getSelf());

        } else if (message instanceof PushNewMessage) {
            getContext().actorSelection("user1").tell(new Queue(((PushNewMessage) message).message), getSelf());
        } else if (message instanceof WhoAreYou) {
            log.debug("I am {}", getSelf().path());
            getSender().tell(getSelf().path(), getSelf());
        } else {
            unhandled(message);
        }
    }

    /**
     * Todo: need to cache it
     *
     * @return
     */
    private ActorRef getNotificationActor(String actorName) {

        if(registryManager == null){
            setRegistry();
        }
        ActorRef actorRef = null;

        Timeout timeout = new Timeout(Duration.create(1, "seconds"));
        Future<Object> futureActorRef = Patterns.ask(registryManager, new GetActorWithName(actorName), timeout);
        try {
            actorRef = (ActorRef) Await.result(futureActorRef, timeout.duration());
        } catch (Exception ignore) {
            log.warning("failed search for actor", ignore);
        }

        log.info("actor found? {}", !(actorRef == null));
        if (actorRef == null ) {
            actorRef = getContext().actorOf(SpringExtProvider.
                    get(getContext().system()).props("NotificationActor"), "user1");
            getContext().actorSelection(REGISTRY_MANAGER).tell(new Register(actorRef), getSelf());
            log.debug("Actor created");
            log.debug("actor path {}", actorRef.path().name());
        }
        return actorRef;
    }

    private void setRegistry(){
        ActorSelection actorSelection = getContext().actorSelection(REGISTRY_MANAGER);
        Timeout timeout = new Timeout(Duration.create(1, "seconds"));
        scala.concurrent.Future<ActorRef> futureActorRef = actorSelection.resolveOne(Timeout.durationToTimeout(
                new FiniteDuration(1, TimeUnit.SECONDS)));

        ActorRef actorRef = null;
        try {
            actorRef = Await.result(futureActorRef, timeout.duration());
            this.registryManager = actorRef;
            log.debug("found registry");
        } catch (Exception ignore) {
            log.debug("failed search for RegistryManager", ignore);
            throw new RuntimeException(ignore);
        }

    }

}
