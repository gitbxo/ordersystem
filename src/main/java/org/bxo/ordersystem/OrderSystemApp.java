package org.bxo.ordersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import org.bxo.ordersystem.worker.WorkerConfig;

/**
 *
 *  Command to compile application
 *  mvn compile
 *
 *  Command to run service
 *  mvn spring-boot:run
 *
 *  Dockerize using this:
 *  https://spring.io/guides/gs/spring-boot-docker
 *  https://github.com/spring-guides/gs-spring-boot-docker/tree/master/complete
 *  https://github.com/spring-guides/gs-actuator-service/tree/master/complete
 *  https://github.com/learnk8s/spring-boot-k8s-hpa/blob/master/src/main/java/com/learnk8s/app/SpringBootApplication.java
 *
 **/

@SpringBootApplication
@Import(WorkerConfig.class)
public class OrderSystemApp {

	public static void main(String[] args) {
		SpringApplication.run(OrderSystemApp.class, args);
	}

}
