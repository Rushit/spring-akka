package com.sample.notification.actor;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;
import static com.sample.notification.actor.NotificationFSMMessages.*;

/**
 * Created by rpatel on 7/17/14.
 */
@Component("NotificationActor")
@Scope("prototype")
public class NotificationFSM extends NotificationFSMBase {

    private final LoggingAdapter log  = Logging.getLogger(getContext().system(), this);
    private       Cancellable    tick = null;

    @Override
    public void onReceive(Object object) {

        log.info("message in FSM {}, when state is {}", object, getState().toString());

        if (object instanceof UnRegisterTarget) {
            if (isTargetAvailable()) {
                getTargets().remove(getSender());
                if (!isTargetAvailable() && getState() == State.WAITING_FOR_DATA) {
                    setState(State.WAITING);
                }
            }
        } else {

            // capturing immutable data
            if (object instanceof SetTarget) {
                addTarget(((SetTarget) object).ref);
            } else if (object instanceof Queue) {
                enqueue(((Queue) object).message);
            }

            // state automata changes
            if (getState() == State.START) {
                handlStart(object);
            } else if (getState() == State.WAITING) {
                handleWaiting(object);
            } else if (getState() == State.WAITING_FOR_DATA) {
                handleWaitingForData(object);
            } else if (getState() == State.WAITING_FOR_TARGET) {
                handleWaitingForTarget(object);
            }
        }
        renewTimeOutTick();
        log.info("new state {}", getState().toString());
    }

    public void handlStart(Object object) {
        if (object instanceof SetTarget) {
            // todo : db call and set state to waiting
            setState(State.WAITING_FOR_DATA);
        } else {
            unhandled(object);
        }

    }

    public void handleWaiting(Object object) {
        if (object instanceof SetTarget) {
            setState(State.WAITING_FOR_DATA);
        } else if (object instanceof Queue) {
            setState(State.WAITING_FOR_TARGET);
        } else if (object instanceof TimeOutTick) {
            log.info("kill pill {}", getSelf().path());
            getContext().stop(getSelf());
        } else {
            unhandled(object);
        }
    }

    public void handleWaitingForTarget(Object object) {
        if (object instanceof SetTarget) {
            if (isMessageAvailable()) {
                sendDataToTarget();
                setState(State.WAITING);
            } else {
                setState(State.WAITING_FOR_DATA);
            }
        } else {
            unhandled(object);
        }
    }

    public void handleWaitingForData(Object object) {
        if (object instanceof Queue) {
            if (isTargetAvailable()) {
                sendDataToTarget();
                setState(State.WAITING);
            } else {
                setState(State.WAITING_FOR_TARGET);
            }
        } else {
            unhandled(object);
        }
    }

    private void renewTimeOutTick() {
        if (tick != null) {
            tick.cancel();
        }
        tick = getContext().system().scheduler().scheduleOnce(Duration.create(5, TimeUnit.MINUTES), getSelf(),
                new TimeOutTick(), getContext().dispatcher(), getSelf());
    }

    private void sendDataToTarget() {
        final Batch messages = new Batch(drainQueue());
        Iterator<ActorRef> itr = getTargets().iterator();
        while(itr.hasNext()){
            ActorRef target = itr.next();
            target.tell(messages, getSelf());
            itr.remove();
        }

    }

    @Override
    public void preStart() {
        renewTimeOutTick();
    }

    @Override
    public void postStop() {
        if (tick != null) {
            tick.cancel();
        }
    }

    @Override
    public void unhandled(Object object) {
        log.warning("received unknown message {} in state {}", object, getState());
        super.unhandled(object);

    }

}
