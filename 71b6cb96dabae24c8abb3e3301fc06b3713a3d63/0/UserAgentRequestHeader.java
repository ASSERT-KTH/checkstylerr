/**
 * Copyright (c) 2019, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1)Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3)Neither the name of zold-java-client nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.zold;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.client5.http.protocol.RequestDefaultHeaders;


/**
 * User Agent Request Header Interceptor.
 * @author Ammar Atef (a_atef45@yahoo.com)
 * @version $Id: 813a4749be9d08eb2222d4e47840ff2a5708da6d $
 * @since 0.0.1
 */
final class UserAgentRequestHeader extends RequestDefaultHeaders {

    /**
     * Config properties file.
     */
    private static final String CONFIG_FILE = "config.properties";

    /**
     * Version property key.
     */
    private static final String VERSION_KEY = "build.version";

    /**
     * Ctor.
     */
    UserAgentRequestHeader() {
        super(Collections.singletonList(
            new BasicHeader(
                HttpHeaders.USER_AGENT,
                join(
                    new String[] {
                        "zold-java-client /",
                        version(),
                        "See https://github.com/amihaiemil/zold-java-client",
                        },
           " "
                )
            )
        ));
    }

    /**
     * Join string array.
     * @param str Array to be joined.
     * @param separator To separate array elements.
     * @return String joined by the separator.
     */
    private static String join(final String[] str, final String separator){
        String retval = "";
        for (final String string: str){
            retval+= separator + string;
        }
        return retval.replaceFirst(separator, "");
    }

    /**
     * Read current version from property file.
     * @return Build version.
     */
    private static String version() {
        final ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        final String version;
        final Properties properties = new Properties();
        try (final InputStream inputStream =
                 loader.getResourceAsStream(CONFIG_FILE)){
            properties.load(inputStream);
            version = properties.getProperty(VERSION_KEY);
        } catch (final IOException exception) {
            throw new RuntimeException(
                String.format("Missing %s file.", CONFIG_FILE)
            );
        }
        return version;
    }

}
