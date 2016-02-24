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
package org.n52.subverse.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import org.n52.iceland.coding.encode.ResponseProxy;
import org.n52.iceland.coding.encode.ResponseWriter;
import org.n52.iceland.coding.encode.ResponseWriterKey;
import org.n52.iceland.util.http.MediaType;

public class XmlBeansWriter implements ResponseWriter<XmlObject> {

    public static final ResponseWriterKey KEY
            = new ResponseWriterKey(XmlObject.class);

    private MediaType contentType;

    public XmlBeansWriter() {
    }

    @Override
    public Set<ResponseWriterKey> getKeys() {
        return Collections.singleton(KEY);
    }

    @Override
    public void write(XmlObject xml, OutputStream out,
            ResponseProxy responseProxy)
            throws IOException {
        xml.save(out, new XmlOptions().setSavePrettyPrint());
    }

    @Override
    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean supportsGZip(XmlObject t) {
        return true;
    }
}
