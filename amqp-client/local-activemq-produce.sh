#!/bin/bash

#mvn exec:exec -Dexec.mainClass="org.n52.amqp.client.CollectorClient" -Dexec.args="-classpath %classpath org.n52.amqp.client.CollectorClient amqp://tester:test@localhost:5672/dynamicTopics/52NORTH.Topic01.OUT"
mvn exec:exec -Dexec.mainClass="org.n52.amqp.client.ClientMain" -Dexec.args="-classpath %classpath org.n52.amqp.client.ClientMain amqp://tester:test@localhost:5672/dynamicTopics/52NORTH.Topic01.OUT"

