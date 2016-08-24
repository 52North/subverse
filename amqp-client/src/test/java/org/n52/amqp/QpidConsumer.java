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
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class QpidConsumer {

    public static void main(String[] args) throws IOException {
        Messenger messenger = Messenger.Factory.create();
        messenger.start();
        while (true) {
            messenger.subscribe("amqp://localhost/my-test-queue");

            while (!messenger.stopped()) {
                System.out.println("start receiving");
                messenger.recv();
                while (messenger.incoming() > 0) {
                    System.out.println("starting receiving loop");
                    Message msg = messenger.get();
                    System.out.println("New Message with subject: "+msg.getSubject());
                    Section body = msg.getBody();
                    if (body instanceof AmqpValue) {
                        System.out.println(((AmqpValue) body).getValue());
                    }

                    ApplicationProperties ap = msg.getApplicationProperties();
                    if (ap != null && !ap.getValue().isEmpty()) {
                        ap.getValue().forEach((Object key, Object value) -> {
                            System.out.println(key +"="+ value);
                        });
                    }
                }
            }
        }
    }

}
