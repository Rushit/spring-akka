package com.sample.notification;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import static com.sample.notification.NotificationFSMMessages.*;
/**
 * Created by rpatel on 7/17/14.
 */
@Component("NotificationActor")
@Scope("prototype")
public class NotificationFSM extends NotificationFSMBase{

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public NotificationFSM() {}

    @Override
    public void onReceive(Object object) {

        log.info("message in FSM {}, when state is {}", object, getState().toString());
        if (getState() == State.START) {
            if (object instanceof SetTarget) {
                setTarget(((SetTarget) object).ref);
                // todo : db call and set state to waiting
                setState(State.WAITING_FOR_DATA);
            } else {
                log.warning("received unknown message {} in state {}", object, getState());
                unhandled(object);
            }

        } else if (getState() == State.WAITING) {
            if (object instanceof SetTarget) {
                setTarget(((SetTarget) object).ref);
                setState(State.WAITING_FOR_DATA);
            } else if (object instanceof Queue) {
                enqueue(((Queue) object).message);
                setState(State.WAITING_FOR_TARGET);
            } else {
                log.warning("received unknown message {} in state {}", object, getState());
                unhandled(object);
            }
        } else if (getState() == State.WAITING_FOR_DATA) {
            if (object instanceof Queue) {
                enqueue(((Queue) object).message);
                if (isTargetAvailable()) {
                    sendDataToTarget();
                    setState(State.WAITING);
                }else {
                    setState(State.WAITING_FOR_TARGET);
                }
            } else {
                log.warning("received unknown message {} in state {}", object, getState());
                unhandled(object);
            }

        } else if (getState() == State.WAITING_FOR_TARGET) {
            if (object instanceof SetTarget) {
                if (isMessageAvailable()) {
                    sendDataToTarget();
                    setState(State.WAITING);
                } else {
                    setState(State.WAITING_FOR_DATA);
                }
            }else {
                log.warning("received unknown message {} in state {}", object, getState());
                unhandled(object);
            }

        }

        log.info("new state {}", getState().toString());
    }

    private void sendDataToTarget() {
        getTarget().tell(new Batch(drainQueue()), getSelf());
        setTarget(null);
    }
}
