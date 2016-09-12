#!/bin/bash

mvn exec:exec -Dexec.mainClass="org.n52.amqp.client.CollectorClient" -Dexec.args="-classpath %classpath org.n52.amqp.client.CollectorClient amqp://tester:test@localhost:5672/dynamicTopics/52NORTH.Topic01.OUT"
#mvn exec:exec -Dexec.args="-classpath %classpath org.n52.amqp.client.CollectorClient amqp://52north:FhWxG6qIu9h2nDb@192.168.75.127:5672/topic://52NORTH.Topic01.OUT amqp://52north:FhWxG6qIu9h2nDb@192.168.75.127:5672/queue://52NORTH.IN"

