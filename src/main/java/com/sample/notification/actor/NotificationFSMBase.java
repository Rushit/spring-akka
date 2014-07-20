package com.sample.notification.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rpatel on 7/16/14.
 */
public abstract class NotificationFSMBase extends UntypedActor {

    /*
   * This is the mutable state of this state machine.
   */
    private State         state   = State.START;
    private Set<ActorRef> targets = new HashSet<ActorRef>();
    private List<String>  queue   = new ArrayList<String>();

    protected void addTarget(ActorRef target) {
        this.targets.add(target);
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

    protected Collection<ActorRef> getTargets() {
        return targets;
    }

    protected boolean isTargetAvailable() {
        return (this.targets != null || this.targets.size() <=0);
    }

    protected boolean isMessageAvailable(){
        return (this.queue != null || this.queue.size() > 0);
    }

    protected enum State {
        START, WAITING, WAITING_FOR_DATA, WAITING_FOR_TARGET;
    }



}
