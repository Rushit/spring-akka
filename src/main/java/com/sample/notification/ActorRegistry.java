package com.sample.notification;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.sample.notification.fsm.CommonMessages;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by rpatel on 7/25/14.
 */
@Component("ActorRegistry")
@Scope("prototype")
public class ActorRegistry extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static class Register {
        public final ActorRef actorRef;
        public Register(ActorRef actorRef){
            this.actorRef = actorRef;
        }
    }

    public static class UnRegister {
        public final ActorRef actorRef;
        public UnRegister(ActorRef actorRef){
            this.actorRef = actorRef;
        }
    }

    public static class GetActorWithName {
        public final String name;
        public GetActorWithName(String name) {
            this.name = name;
        }
    }

    ;

    private Map<String, ActorRef> registry = new HashMap<String, ActorRef>();

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug("message in ActorRegistry {}", message);

        if (message instanceof Register) {
            log.debug("{} added to registry", ((Register) message).actorRef.path().name());
            registry.put(((Register) message).actorRef.path().name(), ((Register) message).actorRef);
            getContext().watch(((Register) message).actorRef);
        } else if (message instanceof UnRegister) {
            log.debug("{} removed from registry", ((Register) message).actorRef.path().name());
            registry.remove(((UnRegister) message).actorRef.path().name());
            getContext().unwatch(((UnRegister) message).actorRef);
        } else if (message instanceof Terminated) {
            final Terminated t = (Terminated) message;
            log.debug("{} removed from registry", t.getActor().path().name());
            registry.remove(t.getActor().path().name());
            getContext().unwatch(t.getActor());
        } else if (message instanceof GetActorWithName) {
            log.info("Asking for {}", ((GetActorWithName) message).name);
            getSender().tell(registry.get(((GetActorWithName) message).name), getSelf());
        } else if (message instanceof CommonMessages.WhoAreYou) {
            log.debug("I am {}", getSelf().path());
            getSender().tell(getSelf().path(), getSelf());
        } else {
            unhandled(message);
        }

        printRegistry();
    }

    public void printRegistry(){
        log.debug("registry has {} ", registry.keySet());
    }
}
