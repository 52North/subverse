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
package org.n52.subverse.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.lifecycle.Constructable;
import org.n52.subverse.SubverseSettings;
import org.n52.subverse.dao.SubscriptionDao;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.subscription.Subscription;
import org.n52.subverse.subscription.UnknownSubscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@Configurable
public class FileSystemSubscriptionDao implements SubscriptionDao, Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemSubscriptionDao.class);
    private String storageDirectory;
    private Path storagePath;
    private static final String FILE_EXTENSION = "sub";

    @Override
    public void init() {
        if (this.storageDirectory == null) {
            LOG.warn("No storageDirectory provided. using default 'data'");
            this.storageDirectory = "data";
        }
        try {
            Path base = Paths.get(getClass().getResource("/").toURI());
            this.storagePath = base.resolve(storageDirectory);
        } catch (URISyntaxException ex) {
            LOG.warn("Could not resolve base storage directory. falling back to OS tmpdir", ex);
            Path base = Paths.get(System.getProperty("java.io.tmpdir"));
            this.storagePath = base.resolve(storageDirectory);
        }

        if (!this.storagePath.toFile().exists()) {
            try {
                Files.createDirectories(this.storagePath);
            } catch (IOException ex) {
                LOG.warn("Could not create or access storage directory", ex);
            }
        }
    }

    @Setting(SubverseSettings.FILESYSTEM_STORAGE_DIRECTORY)
    public void setStorageDirectory(String storageDir) {
        this.storageDirectory = storageDir;
    }

    @Override
    public synchronized void storeSubscription(Subscription sub) {
        Path targetFile = resolveSubscriptionFileName(sub.getId());
        try {
            Files.write(targetFile, serialize(sub),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            LOG.warn("Could not store subscription", ex);
        }
    }

    private Path resolveSubscriptionFileName(String id) {
        return this.storagePath.resolve(String.format("%s.%s", id, FILE_EXTENSION));
    }

    @Override
    public synchronized Stream<Subscription> getAllSubscriptions() {
        List<Subscription> result = new ArrayList<>();

        try (DirectoryStream<Path> stream =
             Files.newDirectoryStream(this.storagePath, "*.{"+FILE_EXTENSION+"}")) {
            for (Path entry: stream) {
                String f = entry.toFile().getName();
                Optional<Subscription> sub = getSubscription(f.substring(0, f.lastIndexOf(".")));
                if (sub.isPresent()) {
                    result.add(sub.get());
                }
            }
        } catch (IOException ex) {
            LOG.warn("Could not read storage directory", ex);
        }

        return result.stream();
    }

    @Override
    public synchronized Optional<Subscription> getSubscription(String id) {
        Path targetFile = resolveSubscriptionFileName(id);
        if (targetFile == null || !targetFile.toFile().exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(deserialize(Files.readAllBytes(targetFile)));
        } catch (IOException ex) {
            LOG.warn("Could not read subscription", ex);
            return Optional.empty();
        }
    }

    @Override
    public synchronized Subscription deleteSubscription(String subscriptionId) throws UnknownSubscriptionException {
        Optional<Subscription> result = getSubscription(subscriptionId);

        if (result.isPresent()) {
            Path targetFile = resolveSubscriptionFileName(subscriptionId);

            try {
                Files.delete(targetFile);
            } catch (IOException ex) {
                LOG.warn("Could not delete subscription", ex);
            }

            return result.get();
        }

        throw new UnknownSubscriptionException("Subscription with id '{}' is not known");
    }

    @Override
    public void updateTerminationTime(Subscription sub, DateTime terminationTime) {
        SubscribeOptions opts = sub.getOptions();
        SubscribeOptions newOpts = new SubscribeOptions(opts.getPublicationIdentifier(),
                terminationTime,
                opts.getFilter().orElse(null),
                opts.getFilterLanguageId().orElse(null),
                opts.getDeliveryDefinition().orElse(null),
                opts.getDeliveryParameters(),
                opts.getContentType().orElse(null));

        sub.updateOptions(newOpts);

//        this.storage.put(sub.getId(), sub);
    }

    private byte[] serialize(Subscription sub) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(buffer)) {
            oos.writeObject(sub);
            oos.flush();
        }

        return buffer.toByteArray();
    }

    private Subscription deserialize(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(in);

        try {
            return (Subscription) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        }
    }
}
