package com.sample.notification.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rpatel on 7/16/14.
 */
public abstract class NotificationFSMBase extends UntypedActor {

    /*
   * This is the mutable state of this state machine.
   */
    private State state = State.START;
    private ActorRef     target = null;
    private List<String> queue = new ArrayList<String>();

    protected void setTarget(ActorRef target) {
        this.target = target;
    }

    protected void setState(State s) {
        if (state != s) {
            state = s;
        }
    }

    protected void enqueue(List<String> o) {
        if (queue != null) {
            queue = new ArrayList<String>();
        }
        queue.addAll(o);
    }

    protected List<String> drainQueue() {
        final List<String> q = queue;
        if (q == null)
            throw new IllegalStateException("drainQueue(): not yet initialized");
        queue = new ArrayList<String>();
        return q;
    }

    protected State getState() {
        return state;
    }

    protected ActorRef getTarget() {
        if (target == null)
            throw new IllegalStateException("getTarget(): not yet initialized");
        return target;
    }

    protected boolean isTargetAvailable() {
        return (this.target != null);
    }

    protected boolean isMessageAvailable(){
        return (this.queue != null || this.queue.size() > 0);
    }

    protected enum State {
        START, WAITING, WAITING_FOR_DATA, WAITING_FOR_TARGET;
    }



}
