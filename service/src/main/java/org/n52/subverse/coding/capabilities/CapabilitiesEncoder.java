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
package org.n52.subverse.coding.capabilities;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import net.opengis.ows.x11.AddressType;
import net.opengis.ows.x11.AllowedValuesDocument;
import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.ContactType;
import net.opengis.ows.x11.DomainType;
import net.opengis.ows.x11.HTTPDocument;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.ows.x11.OperationDocument;
import net.opengis.ows.x11.OperationsMetadataDocument;
import net.opengis.ows.x11.RequestMethodType;
import net.opengis.ows.x11.ResponsiblePartySubsetType;
import net.opengis.ows.x11.ServiceIdentificationDocument;
import net.opengis.ows.x11.ServiceProviderDocument;
import net.opengis.ows.x11.TelephoneType;
import net.opengis.pubsub.x10.DeliveryCapabilitiesType;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.FilterCapabilitiesType;
import net.opengis.pubsub.x10.FilterLanguageType;
import net.opengis.pubsub.x10.PublicationType;
import net.opengis.pubsub.x10.PublicationsType;
import net.opengis.pubsub.x10.PublisherCapabilitiesDocument;
import net.opengis.pubsub.x10.PublisherCapabilitiesType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.OperationRequestEncoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.i18n.MultilingualString;
import org.n52.iceland.ogc.ows.Constraint;
import org.n52.iceland.ogc.ows.DCP;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.ogc.ows.OwsCapabilities;
import org.n52.iceland.ogc.ows.OwsExtendedCapabilities;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.iceland.ogc.ows.OwsOperationsMetadata;
import org.n52.iceland.ogc.ows.OwsParameterValue;
import org.n52.iceland.ogc.ows.OwsParameterValuePossibleValues;
import org.n52.iceland.ogc.ows.OwsServiceIdentification;
import org.n52.iceland.ogc.ows.OwsServiceProvider;
import org.n52.iceland.response.GetCapabilitiesResponse;
import org.n52.iceland.util.http.HTTPMethods;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseCapabilities;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.capabilities.delivery.DeliveryCapabilities;
import org.n52.subverse.delivery.DeliveryParameter;
import org.n52.subverse.coding.capabilities.filter.FilterCapabilities;
import org.n52.subverse.coding.capabilities.publications.Publications;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.DeliveryProviderRepository;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Configurable
public class CapabilitiesEncoder implements
        Encoder<XmlObject, GetCapabilitiesResponse> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CapabilitiesEncoder.class);

    private static final Set<EncoderKey> ENCODER_KEYS = Sets.<EncoderKey>newHashSet(
            new OperationRequestEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_GET_CAPABILITIES,
                    MediaTypes.TEXT_XML),
            new OperationRequestEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_GET_CAPABILITIES,
                    MediaTypes.APPLICATION_XML));

    @Override
    public XmlObject encode(GetCapabilitiesResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(GetCapabilitiesResponse resp, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        PublisherCapabilitiesDocument rootDoc = PublisherCapabilitiesDocument.Factory.newInstance();
        PublisherCapabilitiesType publisherCaps = rootDoc.addNewPublisherCapabilities();

        SubverseCapabilities capsObject = (SubverseCapabilities) resp.getCapabilities();

        if (capsObject.isSetServiceIdentification()) {
            createServiceIdentification(publisherCaps, capsObject);
        }

        if (capsObject.isSetServiceProvider()) {
            createServiceProvider(publisherCaps, capsObject);
        }

        if (capsObject.isSetOperationsMetadata()) {
            createOperationsMetadata(publisherCaps, capsObject.getOperationsMetadata());
        }

        if (capsObject.isSetFilterCapabilities()) {
            createFilterCapabilites(publisherCaps, capsObject.getFilterCapabilities());
        }

        if (capsObject.isSetDeliveryCapabilities()) {
            createDeliveryCapabilites(publisherCaps, capsObject.getDeliveryCapabilities());
        }

        if (capsObject.isSetPublications()) {
            createPublications(publisherCaps, capsObject.getPublications());
        }

        return rootDoc;
    }

    private void createServiceIdentification(PublisherCapabilitiesType publisherCaps, OwsCapabilities capsObject) {
        ServiceIdentificationDocument.ServiceIdentification serviceIdent = publisherCaps.addNewServiceIdentification();
        OwsServiceIdentification identObject = capsObject.getServiceIdentification();

        MultilingualString titleObj = identObject.getTitle();
        Locale defaultLocale = titleObj.getLocales().stream().filter(
                l -> "eng".equals(l.getLanguage())).findFirst()
                .orElse(titleObj.getLocales().stream().findFirst().get());

        LanguageStringType title = serviceIdent.addNewTitle();
        title.setStringValue(titleObj.getLocalization(defaultLocale).get().getText());
        title.setLang(defaultLocale.getLanguage());

        LanguageStringType abs = serviceIdent.addNewAbstract();
        abs.setStringValue(identObject.getAbstract().getLocalization(defaultLocale).get().getText());
        abs.setLang(defaultLocale.getLanguage());

        CodeType st = serviceIdent.addNewServiceType();
        st.setStringValue(identObject.getServiceType());
        st.setCodeSpace(identObject.getServiceTypeCodeSpace());

        identObject.getVersions().stream().forEach((version) -> {
            serviceIdent.addNewServiceTypeVersion().setStringValue(version);
        });

        serviceIdent.setFees(identObject.getFees());
        identObject.getAccessConstraints().stream().forEach((accessConstraint) -> {
            serviceIdent.addAccessConstraints(accessConstraint);
        });

    }

    private void createServiceProvider(PublisherCapabilitiesType publisherCaps, OwsCapabilities capsObject) {
        ServiceProviderDocument.ServiceProvider serviceProvider = publisherCaps.addNewServiceProvider();

        OwsServiceProvider serviceObj = capsObject.getServiceProvider();

        serviceProvider.setProviderName(serviceObj.getName());
        serviceProvider.addNewProviderSite().setHref(serviceObj.getSite());
        ResponsiblePartySubsetType contact = serviceProvider.addNewServiceContact();
        contact.setIndividualName(serviceObj.getIndividualName());
        contact.setPositionName(serviceObj.getPositionName());

        ContactType ci = contact.addNewContactInfo();
        TelephoneType phone = ci.addNewPhone();
        phone.addNewVoice().setStringValue(serviceObj.getPhone());
        String fax = serviceObj.getFacsimile();
        if (fax != null && !fax.isEmpty()) {
            phone.addNewFacsimile().setStringValue(fax);
        }

        AddressType address = ci.addNewAddress();
        address.addDeliveryPoint(serviceObj.getDeliveryPoint());
        address.setCity(serviceObj.getCity());
        address.setAdministrativeArea(serviceObj.getAdministrativeArea());
        address.setPostalCode(serviceObj.getPostalCode());
        address.setCountry(serviceObj.getCountry());
        address.addElectronicMailAddress(serviceObj.getMailAddress());
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.APPLICATION_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    private void createOperationsMetadata(PublisherCapabilitiesType publisherCaps, OwsOperationsMetadata operationsMetadata) {
        OperationsMetadataDocument.OperationsMetadata xml_operationsMetadata = publisherCaps.addNewOperationsMetadata();

        SortedSet<OwsOperation> operations = operationsMetadata.getOperations();
        operations.stream().forEach((operation) -> {
            OperationDocument.Operation xml_operation = xml_operationsMetadata.addNewOperation();
            xml_operation.setName(operation.getOperationName());

            SortedMap<String, Set<DCP>> dcp = operation.getDcp();
            if (!dcp.isEmpty()) {
                HTTPDocument.HTTP xml_http = xml_operation.addNewDCP().addNewHTTP();
//                dcp.entrySet().stream().map(Map.Entry::getKey).filter(Predicates.equalTo(HTTPMethods.GET))

                if (dcp.containsKey(HTTPMethods.GET)) {
                    dcp.get(HTTPMethods.GET).forEach((val) -> {
                        RequestMethodType xml_get = xml_http.addNewGet();
                        xml_get.setHref(val.getUrl());
                        val.getConstraints().forEach(constr -> addConstraintToRequestMethod(constr, xml_get));
                    });
                }
                if (dcp.containsKey(HTTPMethods.POST)) {
                    dcp.get(HTTPMethods.POST).forEach((val) -> {
                        RequestMethodType xml_post = xml_http.addNewPost();
                        xml_post.setHref(val.getUrl());
                        val.getConstraints().forEach(constr -> addConstraintToRequestMethod(constr, xml_post));
                    });
                }
            }

            SortedMap<String, List<OwsParameterValue>> parameterValues = operation.getParameterValues();
            parameterValues.entrySet().stream().forEach((entry) -> {
                Set<String> newNS = addEntryToOperation(entry, xml_operation.addNewParameter());
            });

            SortedMap<String, List<OwsParameterValue>> constraints = operation.getConstraints();
            constraints.entrySet().stream().forEach((entry) -> {
                Set<String> newNS = addEntryToOperation(entry, xml_operation.addNewConstraint());
            });
        });

        if (operationsMetadata.isSetCommonValues()) {
            SortedMap<String, List<OwsParameterValue>> commonValues = operationsMetadata.getCommonValues();
            commonValues.entrySet()
                    .forEach(entry -> addEntryToDomainType(entry, xml_operationsMetadata.addNewParameter()));
        }

        if (operationsMetadata.isSetCommonConstraints()) {
            SortedMap<String, List<OwsParameterValue>> commonConstraints = operationsMetadata.getCommonConstraints();
            commonConstraints.entrySet()
                    .forEach(entry -> addEntryToDomainType(entry, xml_operationsMetadata.addNewConstraint()));
        }

        if (operationsMetadata.isSetExtendedCapabilities()) {
            OwsExtendedCapabilities extendedCapabilities = operationsMetadata.getExtendedCapabilities();
            //TODO implements extended capabilities
        }
    }


    private Set<String> addEntryToOperation(Map.Entry<String, List<OwsParameterValue>> entry, DomainType xml_parameter) {
        Set<String> namespacesCollectedFromValues = Sets.newHashSet();

        String name = entry.getKey();
        xml_parameter.setName(name);
        List<OwsParameterValue> values = entry.getValue();
        LOG.trace("Encoding operation parameter {} with {} values into {}", name, values.size(), xml_parameter.getDomNode().getNodeName());

        values.stream().forEach((parameterValue) -> {
            if (parameterValue instanceof OwsParameterValuePossibleValues) {
                OwsParameterValuePossibleValues possibleValues = (OwsParameterValuePossibleValues) parameterValue;

//            } else if (parameterValue instanceof OwsParameterValueRange) {
//            } else if (parameterValue instanceof OwsParameterDataType) {
            } else {
                LOG.warn("Unsupported OwsParameterValue type: {}",
                        parameterValue == null ? null : parameterValue.getClass());
            }
        });

        return namespacesCollectedFromValues;
    }

    private void addConstraintToRequestMethod(Constraint constr, RequestMethodType xml_requestMethod) {
        DomainType xml_constraint = xml_requestMethod.addNewConstraint();
        xml_constraint.setName(constr.getName());

        constr.getValues().stream().forEach((parameterValue) -> {
            if (parameterValue instanceof OwsParameterValuePossibleValues) {
                AllowedValuesDocument.AllowedValues xml_allowed = xml_constraint.addNewAllowedValues();

                OwsParameterValuePossibleValues possibleValues = (OwsParameterValuePossibleValues) parameterValue;
                possibleValues.getValues().forEach(val -> {
                    xml_allowed.addNewValue().setStringValue(val);
                });
            } else {
                LOG.warn("Unsupported OwsParameterValue type: {}",
                        parameterValue == null ? null : parameterValue.getClass());
            }
        });
    }

    private void addEntryToDomainType(Map.Entry<String, List<OwsParameterValue>> entry, DomainType xml_domainType) {
        xml_domainType.setName(entry.getKey());

        entry.getValue().stream().forEach((parameterValue) -> {
            if (parameterValue instanceof OwsParameterValuePossibleValues) {
                OwsParameterValuePossibleValues possibleValues = (OwsParameterValuePossibleValues) parameterValue;
                possibleValues.getValues().forEach(xml_domainType::setName);
            } else {
                LOG.warn("Unsupported OwsParameterValue type: {}",
                        parameterValue == null ? null : parameterValue.getClass());
            }
        });
    }

    private void createFilterCapabilites(PublisherCapabilitiesType publisherCaps, FilterCapabilities filterObject) {
        FilterCapabilitiesType filterCaps = publisherCaps.addNewFilterCapabilities();

        filterObject.getLanguages().stream().forEach((language) -> {
            FilterLanguageType lang = filterCaps.addNewFilterLanguage();
            lang.setIdentifier(language.getIdentifier());
            lang.addNewAbstract().setStringValue(language.getTheAbstract());
            XmlObject supported = lang.addNewSupportedCapabilities();
            Object xo = language.getSupportedCapabilities();
            if (xo != null && xo instanceof XmlObject) {
                supported.set((XmlObject) language.getSupportedCapabilities());
            }
        });
    }

    protected void createDeliveryCapabilites(PublisherCapabilitiesType publisherCaps, DeliveryCapabilities deliveryObject) {
        DeliveryCapabilitiesType delivery = publisherCaps.addNewDeliveryCapabilities();

        deliveryObject.getMethods().stream().forEach((method) -> {
            DeliveryMethodType deliveryMethod = delivery.addNewDeliveryMethod();
            deliveryMethod.setIdentifier(method.getIdentifier());
            deliveryMethod.addNewAbstract().setStringValue(method.getTheAbstract());
            List<DeliveryParameter> params = method.getParameters();
            if (params != null && !params.isEmpty()) {
                deliveryMethod.addNewExtension().set(createDeliveryParameters(params));
            }
        });
    }

    private XmlObject createDeliveryParameters(List<DeliveryParameter> parameters) {
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor cur = xo.newCursor();
        cur.toNextToken();

        parameters.forEach(param -> {
            createElement(cur, param);
        });

        cur.dispose();

        return xo;
    }

    private void createElement(XmlCursor cur, DeliveryParameter param) {
        cur.beginElement(new QName(param.getNamespace(), param.getElementName()));

        if (!param.hasChildren()) {
            cur.insertChars(param.getValue());
        }
        else {
            param.getChildren().forEach(child -> {
                createElement(cur, child);
            });
        }

        cur.toEndDoc();
    }


    private void createPublications(PublisherCapabilitiesType publisherCaps, Publications pubObject) {
        PublicationsType publications = publisherCaps.addNewPublications();

        pubObject.getPublicationList().stream().forEach((publication) -> {
            PublicationType pub = publications.addNewPublication();
            pub.setIdentifier(publication.getIdentifier());
            pub.addNewAbstract().setStringValue(publication.getTheAbstract());
        });
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put("http://www.opengis.net/ows/1.1", "ows");
    }


}
