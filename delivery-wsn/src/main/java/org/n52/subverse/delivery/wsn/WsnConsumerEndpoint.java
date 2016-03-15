/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
    private final XmlOptions xmlOptions;

    public WsnConsumerEndpoint(String location, XmlOptions xo) throws MalformedURLException {
        this(location, false, xo);
    }

    public WsnConsumerEndpoint(String location, boolean useRawOutput, XmlOptions xo) throws MalformedURLException {
        this.targetUrl = new URL(location);
        this.useRawOutput = useRawOutput;
        this.xmlOptions = xo;
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
            return envDoc.xmlText(this.xmlOptions.setUseCDataBookmarks()).getBytes();
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

    @Override
    public String getEffectiveLocation() {
        return this.targetUrl.toString();
    }



}
