/*
* Copyright 2016 52°North.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.n52.amqp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class QpidPublisher {

    public static void main(String[] args) throws IOException {
        Map<String, String> messageAnnotations = new HashMap<>();
        Map<String, String> deliveryAnnotations = new HashMap<>();

        Messenger messenger = Messenger.Factory.create("my-id");
        messenger.start();

        Message message = Message.Factory.create();
        message.setAddress("amqp://localhost/my-test-queue");

        message.setSubject("testing-amqp");
        message.setContentType("text/plain");

        //set message annotations
        messageAnnotations.forEach((String k, String v) -> {
            message.getMessageAnnotations().getValue().put(Symbol.valueOf(k), v);
        });

        //set delivery annotations
        deliveryAnnotations.forEach((String k, String v) -> {
            message.getDeliveryAnnotations().getValue().put(Symbol.valueOf(k), v);
        });

        message.setBody(new AmqpValue("Hello AMQP!"));

        messenger.put(message);
        messenger.send();
    }

}
