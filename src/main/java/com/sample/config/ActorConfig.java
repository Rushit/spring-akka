package com.sample.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sample.notification.fsm.CommonMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static com.sample.config.SpringExtension.SpringExtProvider;

/**
 * Created by rpatel on 7/16/14.
 */

@Configuration
public class ActorConfig {

    // the application context is needed to initialize the Akka Spring Extension
    @Autowired
    private ApplicationContext applicationContext;

    private static String NOTIFICATION_MANAGER_NAME = "notification-manager";
    public static String NOTIFICATION_MANAGER = "/user/" + NOTIFICATION_MANAGER_NAME;

    private static String REGISTRY_MANAGER_NAME = "registry-manager";
    public static String REGISTRY_MANAGER = "/user/" + REGISTRY_MANAGER_NAME;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("NotificationSystem");
        // initialize the application context in the Akka Spring Extension
        SpringExtension.SpringExtProvider.get(system).initialize(applicationContext);
        // todo: better way of init notification manager on startup
        ActorRef registry = system.actorOf(SpringExtProvider.get(system).props("ActorRegistry"),REGISTRY_MANAGER_NAME);
        ActorRef notificationManager = system.actorOf(SpringExtProvider.get(system).props("NotificationManager"),NOTIFICATION_MANAGER_NAME);

        registry.tell(new CommonMessages.WhoAreYou(), null);
        notificationManager.tell(new CommonMessages.WhoAreYou(), null);

        return system;
    }


}
