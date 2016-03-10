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
package org.n52.subverse.handler;

import org.n52.subverse.coding.capabilities.filter.FilterCapabilitiesProducer;
import org.n52.subverse.coding.capabilities.publications.PublicationsProducer;
import org.n52.subverse.coding.capabilities.delivery.DeliveryCapabilitiesProducer;
import org.n52.subverse.coding.capabilities.publications.Publications;
import org.n52.subverse.coding.capabilities.delivery.DeliveryCapabilities;
import org.n52.subverse.coding.capabilities.filter.FilterCapabilities;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.n52.iceland.binding.Binding;
import org.n52.iceland.binding.BindingRepository;
import org.n52.iceland.coding.OperationKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.ds.OperationHandlerKey;
import org.n52.iceland.exception.HTTPException;
import org.n52.iceland.exception.ows.CompositeOwsException;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.i18n.I18NSettings;
import org.n52.iceland.ogc.ows.Constraint;
import org.n52.iceland.ogc.ows.DCP;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.ogc.ows.OwsExtendedCapabilities;
import org.n52.iceland.ogc.ows.OwsExtendedCapabilitiesProvider;
import org.n52.iceland.ogc.ows.OwsExtendedCapabilitiesProviderRepository;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.iceland.ogc.ows.OwsOperationsMetadata;
import org.n52.iceland.ogc.ows.OwsParameterValuePossibleValues;
import org.n52.iceland.ogc.ows.OwsServiceIdentification;
import org.n52.iceland.ogc.ows.OwsServiceProvider;
import org.n52.iceland.ogc.ows.ServiceMetadataRepository;
import org.n52.iceland.request.GetCapabilitiesRequest;
import org.n52.iceland.request.operator.RequestOperatorRepository;
import org.n52.iceland.response.GetCapabilitiesResponse;
import org.n52.iceland.service.ServiceSettings;
import org.n52.subverse.SubverseCapabilities;
import org.n52.iceland.util.collections.MultiMaps;
import org.n52.iceland.util.collections.SetMultiMap;
import org.n52.iceland.util.http.HTTPHeaders;
import org.n52.iceland.util.http.HTTPMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.SubverseConstants.GetCapabilitiesParam.ServiceMetadataSections;
import org.n52.subverse.SubverseSettings;
import org.springframework.beans.factory.annotation.Autowired;

@Configurable
public class GetCapabilitiesHandler implements OperationHandler {

    private static final Set<OperationHandlerKey> OPERATION_HANDLER_KEY
            = Collections.singleton(new OperationHandlerKey(SubverseConstants.SERVICE,
                            OWSConstants.Operations.GetCapabilities));

    private static final Logger LOG = LoggerFactory.getLogger(GetCapabilitiesHandler.class);

    private ServiceMetadataRepository serviceMetadataRepository;

    private OwsExtendedCapabilitiesProviderRepository extendedCapabilitiesRepository;

    private URI serviceURL;

    private String defaultLanguage;

    private RequestOperatorRepository requestOperatorRepository;

    private BindingRepository bindingRepository;

    @Autowired
    private FilterCapabilitiesProducer filterCapabilitiesProducer;
    @Autowired
    private DeliveryCapabilitiesProducer deliveryCapabilitiesProducer;
    @Autowired
    private PublicationsProducer publicationsProducer;

    private String publicationsString;

    @Setting(SubverseSettings.PUBLICATIONS)
    public void setPublicationsString(String ps) {
        this.publicationsString = ps;
    }

    @Inject
    public void setServiceMetadataRepository(ServiceMetadataRepository repo) {
        this.serviceMetadataRepository = repo;
    }

    @Inject
    public void setExtendedCapabilitiesRepository(OwsExtendedCapabilitiesProviderRepository extendedCapabilitiesRepository) {
        this.extendedCapabilitiesRepository = extendedCapabilitiesRepository;
    }

    @Inject
    public void setRequestOperatorRepository(RequestOperatorRepository requestOperatorRepository) {
        this.requestOperatorRepository = requestOperatorRepository;
    }

    @Inject
    public void setBindingRepository(BindingRepository bindingRepository) {
        this.bindingRepository = bindingRepository;
    }

    @Setting(ServiceSettings.SERVICE_URL)
    public void setServiceURL(URI serviceURL) {
        this.serviceURL = serviceURL;
    }

    @Setting(I18NSettings.I18N_DEFAULT_LANGUAGE)
    public void setDefaultLanguage(String language) {
        this.defaultLanguage = language;
    }

    @SuppressWarnings("ThrowableResultIgnored")
    public GetCapabilitiesResponse getCapabilities(
            GetCapabilitiesRequest request)
            throws OwsExceptionReport {
        LOG.debug("Handling GetCapabilities request: {}", request);

        GetCapabilitiesResponse capabilitiesResponse = request.getResponse();
        String version = request.getVersion();
        String service = request.getService();
        String language = request.getRequestedLanguage();

        if (!language.isEmpty() && !language.equals(this.defaultLanguage)) {
            LOG.debug("Requested language '{}' is different from default '{}'.", language, this.defaultLanguage);
            LOG.warn("Unsupported language was requested, parameter is ignored: {}", language);
        }

        SubverseCapabilities capabilities = new SubverseCapabilities(version);

        // TODO add section parameter handling
        Set<ServiceMetadataSections> requestedSections = getRequestedSections(request);
        LOG.debug("Returning {} sections: {}", requestedSections.size(), Arrays.toString(requestedSections.toArray()));

        if (requestedSections.contains(ServiceMetadataSections.ServiceIdentification)) {
            OwsServiceIdentification si = this.serviceMetadataRepository.getServiceIdentificationFactory(service).get();
            capabilities.setServiceIdentification(si);
        }

        if (requestedSections.contains(ServiceMetadataSections.ServiceProvider)) {
            OwsServiceProvider sp = this.serviceMetadataRepository.getServiceProviderFactory(service).get();
            capabilities.setServiceProvider(sp);
        }

        if (requestedSections.contains(ServiceMetadataSections.OperationsMetadata)) {
            OwsOperationsMetadata operationsMetadata = createOperationsMetadata(service, version);

            OwsExtendedCapabilitiesProvider extProv = this.extendedCapabilitiesRepository.getExtendedCapabilitiesProvider(service, version);
            if (extProv != null && extProv.hasExtendedCapabilitiesFor(request)) {
                OwsExtendedCapabilities extendedCapabilities = extProv.getOwsExtendedCapabilities(request);
                operationsMetadata.setExtendedCapabilities(extendedCapabilities);
            }

            capabilities.setOperationsMetadata(operationsMetadata);
        }

        if (requestedSections.contains(ServiceMetadataSections.FilterCapabilities)) {
            FilterCapabilities fc = this.filterCapabilitiesProducer.get();
            capabilities.setFilterCapabilities(fc);
        }

        if (requestedSections.contains(ServiceMetadataSections.DeliveryCapabilities)) {
            DeliveryCapabilities dc = this.deliveryCapabilitiesProducer.get();
            capabilities.setDeliveryCapabilities(dc);
        }

        if (requestedSections.contains(ServiceMetadataSections.Publications)) {
            this.publicationsProducer.setPublicationsString(this.publicationsString);
            Publications ps = this.publicationsProducer.get();
            capabilities.setPublications(ps);
        }

        capabilitiesResponse.setCapabilities(capabilities);
        return capabilitiesResponse;
    }

    @Override
    public String getOperationName() {
        return OWSConstants.Operations.GetCapabilities.name();
    }

    private Set<ServiceMetadataSections> getRequestedSections(GetCapabilitiesRequest request) throws OwsExceptionReport {
        Set<ServiceMetadataSections> sections = Sets.newHashSet();
        if (!request.isSetSections()) {
            Stream.of(ServiceMetadataSections.values()).forEach(sections::add);
        } else {
            for (final String sectionString : request.getSections()) {
                if (sectionString.isEmpty()) {
                    LOG.warn("A section element is empty!");
                    continue;
                }

                ServiceMetadataSections sms = ServiceMetadataSections.lookup(sectionString);

                if (sms.equals(ServiceMetadataSections.All)) {
                    Stream.of(ServiceMetadataSections.values()).forEach(sections::add);
                    break;
                } else {
                    sections.add(sms);
                }
            }
        }
        return sections;
    }


    @SuppressWarnings("ThrowableResultIgnored")
    private OwsOperationsMetadata createOperationsMetadata(String service, String version) throws CompositeOwsException {
        OwsOperationsMetadata operationsMetadata = new OwsOperationsMetadata();
        CompositeOwsException exception = new CompositeOwsException();

        RequestOperatorRepository ror = this.requestOperatorRepository;
        ror.getKeys().stream()
                .map(ror::getRequestOperator)
                .map(op -> {
                    try {
                        return op.getOperationMetadata(service, version);
                    } catch (OwsExceptionReport ex) {
                        exception.add(ex);
                        return null;
                    }
                })
                .map(opMetadata -> {
                    try {
                        Map<String, Set<DCP>> dcp = getDCP(new OperationKey(service, version, opMetadata.getOperationName()));
                        opMetadata.setDcp(dcp);
                    } catch (OwsExceptionReport ex) {
                        exception.add(ex);
                    }

                    return opMetadata;
                })
                .filter(Objects::nonNull)
                .forEach(operationsMetadata::addOperation);

        // add common query parameters
        operationsMetadata.addCommonValue(SubverseConstants.Param.SERVICE,
                new OwsParameterValuePossibleValues(SubverseConstants.SERVICE));
        operationsMetadata.addCommonValue(SubverseConstants.Param.VERSION,
                new OwsParameterValuePossibleValues(SubverseConstants.VERSION));
        operationsMetadata.addCommonValue(SubverseConstants.Param.REQUEST,
                new OwsParameterValuePossibleValues(Sets.newHashSet(OWSConstants.Operations.GetCapabilities.name(),
                                SubverseConstants.OPERATION_GET_CAPABILITIES)));

        exception.throwIfNotEmpty();
        return operationsMetadata;
    }

    protected Map<String, Set<DCP>> getDCP(OperationKey operationKey)
            throws OwsExceptionReport {
        SetMultiMap<String, DCP> dcps = MultiMaps.newSetMultiMap();

        try {
            for (Entry<String, Binding> entry : this.bindingRepository.getBindingsByPath().entrySet()) {
                String url = this.serviceURL.toString(); // + entry.getKey();
                Binding binding = entry.getValue();

                // these are not the "common" parameters and constraints, but the ones that have values for every endpoint
                Set<Constraint> constraints = Sets.newTreeSet();

                // common constraints
                if (binding.getSupportedEncodings() != null
                        && !binding.getSupportedEncodings().isEmpty()) {
                    SortedSet<String> set = binding.getSupportedEncodings().stream()
                            .map(String::valueOf).collect(TreeSet::new, Set::add, Set::addAll);
                    Constraint constraint = new Constraint(HTTPHeaders.CONTENT_TYPE,
                            new OwsParameterValuePossibleValues(set));
                    constraints.add(constraint);
                }

                // common parameters (none yet)
                // create DCPs according to supported operations and specific parameters
                if (binding.checkOperationHttpGetSupported(operationKey)) {
                    DCP dcp = new DCP(url + "?", constraints);
                    dcps.add(HTTPMethods.GET, dcp);
                }
                if (binding.checkOperationHttpPostSupported(operationKey)) {
                    Set<Constraint> postConstraints = Sets.newHashSet(constraints);
                    DCP dcp = new DCP(url, postConstraints);
                    dcps.add(HTTPMethods.POST, dcp);
                }
                if (binding.checkOperationHttpPutSupported(operationKey)) {
                    DCP dcp = new DCP(url, constraints);
                    dcps.add(HTTPMethods.PUT, dcp);
                }
                if (binding.checkOperationHttpDeleteSupported(operationKey)) {
                    DCP dcp = new DCP(url, constraints);
                    dcps.add(HTTPMethods.DELETE, dcp);
                }
            }

            LOG.trace("Created DCPs for {}: {}", operationKey, dcps);
        } catch (HTTPException e) {
            throw new NoApplicableCodeException().withMessage("Encoder for {} does not support a method", operationKey).causedBy(e);
        }

        return dcps;
    }

    @Override
    public OwsOperation getOperationsMetadata(String service, String version)
            throws OwsExceptionReport {
        OwsOperation op = new OwsOperation();
        op.setOperationName(getOperationName());

        op.addPossibleValuesParameter(SubverseConstants.GetCapabilitiesParam.ACCCEPTVERSIONS, SubverseConstants.VERSION);
        // TODO add sections

        return op;
    }

    @Override
    public Set<OperationHandlerKey> getKeys() {
        return OPERATION_HANDLER_KEY;
    }

}
