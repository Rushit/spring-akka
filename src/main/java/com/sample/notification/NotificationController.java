package com.sample.notification;

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
import static com.sample.notification.NotificationManager.*;

@Controller
public class NotificationController {

    Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    ActorSystem actorSystem;

    @RequestMapping("/blocking")
    @ResponseBody
    public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        return "Hello";
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<String> getResult() {
        final DeferredResult<String> deferredResult = new DeferredResult<String>();
        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                deferredResult.setResult("nothing");
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
