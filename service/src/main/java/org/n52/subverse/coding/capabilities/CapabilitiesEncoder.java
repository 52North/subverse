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
package org.n52.subverse.coding.capabilities;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.DomainType;
import net.opengis.ows.x11.HTTPDocument;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.ows.x11.OperationDocument;
import net.opengis.ows.x11.OperationsMetadataDocument;
import net.opengis.ows.x11.RequestMethodType;
import net.opengis.ows.x11.ResponsiblePartySubsetType;
import net.opengis.ows.x11.ServiceIdentificationDocument;
import net.opengis.ows.x11.ServiceProviderDocument;
import net.opengis.pubsub.x10.DeliveryCapabilitiesType;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.FilterCapabilitiesType;
import net.opengis.pubsub.x10.FilterLanguageType;
import net.opengis.pubsub.x10.PublicationType;
import net.opengis.pubsub.x10.PublicationsType;
import net.opengis.pubsub.x10.PublisherCapabilitiesDocument;
import net.opengis.pubsub.x10.PublisherCapabilitiesType;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.OperationRequestEncoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
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
import org.n52.subverse.coding.capabilities.filter.FilterCapabilities;
import org.n52.subverse.coding.capabilities.publications.Publications;
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

        Locale defaultLocale = Locale.forLanguageTag("eng");

        LanguageStringType title = serviceIdent.addNewTitle();
        title.setStringValue(identObject.getTitle().getLocalization(defaultLocale).get().getText());

        LanguageStringType abs = serviceIdent.addNewAbstract();
        abs.setStringValue(identObject.getAbstract().getLocalization(defaultLocale).get().getText());

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
        contact.setIndividualName("TBA");
        contact.setPositionName("TBA");
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
                OwsParameterValuePossibleValues possibleValues = (OwsParameterValuePossibleValues) parameterValue;
                possibleValues.getValues().forEach(xml_constraint::setName);
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

    private void createDeliveryCapabilites(PublisherCapabilitiesType publisherCaps, DeliveryCapabilities deliveryObject) {
        DeliveryCapabilitiesType delivery = publisherCaps.addNewDeliveryCapabilities();

        deliveryObject.getMethods().stream().forEach((method) -> {
            DeliveryMethodType deliveryMethod = delivery.addNewDeliveryMethod();
            deliveryMethod.setIdentifier(method.getIdentifier());
            deliveryMethod.addNewAbstract().setStringValue(method.getTheAbstract());
        });
    }

    private void createPublications(PublisherCapabilitiesType publisherCaps, Publications pubObject) {
        PublicationsType publications = publisherCaps.addNewPublications();

        pubObject.getPublicationList().stream().forEach((publication) -> {
            PublicationType pub = publications.addNewPublication();
            pub.setIdentifier(publication.getIdentifier());
            pub.addNewAbstract().setStringValue(publication.getTheAbstract());
        });
    }


}
