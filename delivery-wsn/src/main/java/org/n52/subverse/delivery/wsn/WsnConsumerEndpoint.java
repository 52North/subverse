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
package org.n52.subverse.delivery.wsn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.xmlbeans.CDataBookmark;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.Streamable;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.oasisOpen.docs.wsn.b2.NotifyDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class WsnConsumerEndpoint implements DeliveryEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(WsnConsumerEndpoint.class);
    private final URL targetUrl;
    private final boolean useRawOutput;

    public WsnConsumerEndpoint(String location) throws MalformedURLException {
        this(location, false);
    }

    public WsnConsumerEndpoint(String location, boolean useRawOutput) throws MalformedURLException {
        this.targetUrl = new URL(location);
        this.useRawOutput = useRawOutput;
    }

    @Override
    public void deliver(Optional<Streamable> o) {
        LOG.debug("Delivering object to '{}': {}", targetUrl, o);

        if (o.isPresent()) {
            try {
                byte[] payload = createPayload(o.get());

                sendPayload(o, payload);
            }
            catch (IOException e) {
                LOG.warn("Could not delivery streamable {}", o, e);
            }
        }
        else {
            LOG.warn("Got null object, cannot deliver");
        }
    }

    protected void sendPayload(Optional<Streamable> o, byte[] payload) {
        try (CloseableHttpClient
                client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(this.targetUrl.toURI());
            post.setEntity(new ByteArrayEntity(payload));
            post.addHeader("Content-Type", this.useRawOutput ? o.get().getContentType() : "application/soap+xml");
//            post.addHeader("Content-Length", Integer.toString(payload.length));
            client.execute(post);
        }
        catch (IOException | URISyntaxException ex) {
            LOG.warn("could not send request", ex);
        }
    }

    private byte[] createPayload(Streamable o) throws IOException {
        if (this.useRawOutput) {
            return streamToByteArray(o.asStream());
        }
        else {
            EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
            Envelope env = envDoc.addNewEnvelope();
            Body body = env.addNewBody();
            NotifyDocument notifyDoc = NotifyDocument.Factory.newInstance();
            NotifyDocument.Notify notify = notifyDoc.addNewNotify();

            NotificationMessageHolderType msg = notify.addNewNotificationMessage();

            NotificationMessageHolderType.Message message = msg.addNewMessage();
            createMessageContent(message, o);

            body.set(notifyDoc);
            return envDoc.xmlText(new XmlOptions().setSavePrettyPrint().setUseCDataBookmarks()).getBytes();
        }
    }

    private byte[] streamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (is.available() > 0) {
            baos.write(is.read());
        }

        return baos.toByteArray();
    }

    private void createMessageContent(NotificationMessageHolderType.Message msg, Streamable o) throws IOException {
        if (o.originalObject() instanceof XmlObject) {
            msg.set((XmlObject) o.originalObject());
        }
        else {
            StringBuilder sb = new StringBuilder();
            createStringFromStreamable(sb, o);
            XmlAnySimpleType any = XmlAnySimpleType.Factory.newInstance();
            any.setStringValue(sb.toString());
            XmlCursor cur = any.newCursor();
            cur.toFirstContentToken();
            cur.setBookmark(CDataBookmark.CDATA_BOOKMARK);
            cur.dispose();
            msg.set(any);
        }
    }

    private void createStringFromStreamable(StringBuilder sb, Streamable o) throws IOException {
        InputStream is = o.asStream();
        while (is.available() > 0) {
            sb.append((char) is.read());
        }
    }

}
