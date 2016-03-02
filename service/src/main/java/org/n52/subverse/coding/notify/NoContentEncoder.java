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
package org.n52.subverse.coding.notify;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.response.NoContentResponse;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.iceland.util.http.NoContent;
import org.n52.subverse.SubverseConstants;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class NoContentEncoder implements Encoder<NoContent, NoContentResponse> {

    private static final Set<EncoderKey> ENCODER_KEYS = Sets.<EncoderKey>newHashSet(
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_NOTIFY,
                    MediaTypes.WILD_CARD));

    @Override
    public NoContent encode(NoContentResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return new NoContent();
    }

    @Override
    public NoContent encode(NoContentResponse objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return new NoContent();
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.WILD_CARD;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return ENCODER_KEYS;
    }

}
