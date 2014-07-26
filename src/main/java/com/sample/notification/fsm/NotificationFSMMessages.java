package com.sample.notification.fsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rpatel on 7/17/14.
 */
public class NotificationFSMMessages {

    public static final class RemoveTarget {};
    public static final class SetTarget{}

    public static final class TimeOutTick{};


    public static final class Queue implements Serializable {
        final List<String> message = new ArrayList<String>();
        public Queue(String o) {
            this.message.add(o);
        }
        public Queue(List<String> o) {
            this.message.addAll(o);
        }
    }

    public static final class Batch implements Serializable {
        final List<String> objects;
        public Batch(List<String> objects) {
            this.objects = objects;
        }
        @Override
        public String toString(){
            return objects.get(0);
        }
    }

}
