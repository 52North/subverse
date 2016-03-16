package org.n52.subverse.binding;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.ExceptionEncoderKey;
import org.n52.iceland.exception.ows.CodedOwsException;
import org.n52.iceland.exception.ows.OwsExceptionCode;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.ExceptionCode;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.coding.renew.UnacceptableTerminationTimeFault;
import org.n52.subverse.coding.subscribe.InvalidPublicationIdentifierFault;
import org.n52.subverse.coding.subscribe.UnacceptableInitialTerminationTimeFault;
import org.n52.subverse.coding.unsubscribe.ResourceUnknownFault;
import org.n52.subverse.handler.InvalidSubscriptionIdentifierFault;
import org.oasisOpen.docs.wsn.b2.UnacceptableInitialTerminationTimeFaultDocument;
import org.oasisOpen.docs.wsn.b2.UnacceptableTerminationTimeFaultDocument;
import org.oasisOpen.docs.wsrf.bf2.BaseFaultDocument;
import org.oasisOpen.docs.wsrf.bf2.BaseFaultType;
import org.oasisOpen.docs.wsrf.r2.ResourceUnknownFaultDocument;
import org.springframework.stereotype.Component;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Component
public class WsBaseNotificationFaultEncoder implements Encoder<XmlObject, OwsExceptionReport> {

    private static final EncoderKey KEY = new ExceptionEncoderKey(MediaTypes.APPLICATION_SOAP_XML);

    private boolean encodeStackTraces;
    
    @Override
    public XmlObject encode(OwsExceptionReport objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(OwsExceptionReport objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        ExceptionReportDocument excRepDoc = ExceptionReportDocument.Factory.newInstance();
        ExceptionReportDocument.ExceptionReport excRep = excRepDoc.addNewExceptionReport();

        ExceptionType exception = excRep.addNewException();
        exception.addExceptionText(createExceptionText(objectToEncode));

        if (objectToEncode instanceof CodedOwsException) {
            ExceptionCode code = ((CodedOwsException) objectToEncode).getCode();
            if (code instanceof OwsExceptionCode) {
                exception.setExceptionCode(((OwsExceptionCode) code).name());
            }
            else {
                exception.setExceptionCode(code.toString());
            }
        }

        return wrapWithWsrf(excRep, objectToEncode);
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.APPLICATION_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

    private String createExceptionText(OwsExceptionReport ex) {
        String msg = ex.getMessage();

        StringBuilder sb = new StringBuilder();
        if (msg != null) {
            sb.append(msg);
        }

        Throwable cause = ex.getCause();
        if (cause != null) {
            sb.append(System.getProperty("line.separator"));
            sb.append(cause.getMessage());

            StackTraceElement[] stack = cause.getStackTrace();

            if (this.encodeStackTraces && stack != null) {
                for (StackTraceElement ste : stack) {
                    sb.append(System.getProperty("line.separator"));
                    sb.append(ste.toString());
                }
            }
        }

        return sb.toString();
    }
    
    private XmlObject wrapWithWsrf(XmlObject owsXml, OwsExceptionReport targetException) {
        XmlObject result;
        
        BaseFaultType bf;
        if (targetException instanceof ResourceUnknownFault ||
                targetException instanceof InvalidPublicationIdentifierFault ||
                targetException instanceof InvalidSubscriptionIdentifierFault) {
            ResourceUnknownFaultDocument doc = ResourceUnknownFaultDocument.Factory.newInstance();
            bf = doc.addNewResourceUnknownFault();
            result = doc;
        }
        else if (targetException instanceof UnacceptableInitialTerminationTimeFault) {
            UnacceptableInitialTerminationTimeFaultDocument doc = UnacceptableInitialTerminationTimeFaultDocument.Factory.newInstance();
            bf = doc.addNewUnacceptableInitialTerminationTimeFault();
            result = doc;
        }
        else if (targetException instanceof UnacceptableTerminationTimeFault) {
            UnacceptableTerminationTimeFaultDocument doc = UnacceptableTerminationTimeFaultDocument.Factory.newInstance();
            bf = doc.addNewUnacceptableTerminationTimeFault();
            result = doc;
        }
        else {
            BaseFaultDocument doc = BaseFaultDocument.Factory.newInstance();
            bf = doc.addNewBaseFault();
            result = doc;
        }
        
        fillBaseFault(bf, owsXml, targetException);
        
        return result;
    }

    private void fillBaseFault(BaseFaultType bf, XmlObject owsXml, OwsExceptionReport targetException) {
        BaseFaultType.Description desc = bf.addNewDescription();
        desc.setStringValue(targetException.getMessage());
        desc.setLang("eng");

        BaseFaultType.FaultCause cause = bf.addNewFaultCause();
        cause.set(owsXml);
    }
    
}
