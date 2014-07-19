package com.sample.notification.controller;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import static com.sample.config.ActorConfig.*;
import static com.sample.notification.actor.NotificationManager.*;

@Controller
public class NotificationController {

    Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    ActorSystem actorSystem;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<String> getResult() {
        long timeOut = 30*1000;
        final DeferredResult<String> deferredResult = new DeferredResult<String>(timeOut);
        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                deferredResult.setResult("");
            }
        });
        actorSystem.actorSelection(NOTIFICATION_MANAGER).tell(new NotificationRequest(deferredResult), null);
        return deferredResult;
    }

    @RequestMapping(value = "/push", method = RequestMethod.GET)
    @ResponseBody
    public void pushMessage(@RequestParam String message) {
        actorSystem.actorSelection(NOTIFICATION_MANAGER).tell(new PushNewMessage(message), null);
    }

}
