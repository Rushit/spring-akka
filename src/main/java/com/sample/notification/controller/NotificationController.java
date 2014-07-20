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


    public static final long LONG_POLLING_TIMEOUT = 30*1000;
    Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    ActorSystem actorSystem;

    @RequestMapping(value = "/blocking-ping", method = RequestMethod.GET)
    @ResponseBody
    public String pingpong1() {
        return "Pong";
    }

    @RequestMapping(value = "/aync-ping", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<String> pingpong() {
        final DeferredResult<String> deferredResult = new DeferredResult<String>(LONG_POLLING_TIMEOUT);
        (new Thread(){
            public void run (){
                deferredResult.setResult("Pong");
            }
        }).start();
        return deferredResult;
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<String> getResult() {
        final long tic = System.currentTimeMillis();
        logger.info("Id:{}  started @{} ", tic, tic );
        final DeferredResult<String> deferredResult = new DeferredResult<String>(LONG_POLLING_TIMEOUT);
        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                deferredResult.setResult("");
            }
        });
        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                long tic2 = System.currentTimeMillis();
                logger.info("id:{} finished @{} with diff {}sec", tic, tic2, (tic2-tic)/1000);

            }
        });
        actorSystem.actorSelection(NOTIFICATION_MANAGER).tell(new NotificationRequest(deferredResult), null);
        return deferredResult;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ResponseBody
    public void pushMessage(@RequestParam String message) {
        actorSystem.actorSelection(NOTIFICATION_MANAGER).tell(new PushNewMessage(message), null);
    }

}
