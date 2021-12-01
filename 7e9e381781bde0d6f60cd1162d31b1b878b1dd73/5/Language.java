/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
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

package com.amihaiemil.charles.github;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Language that the agent speaks.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: c850d22567d207c5cecb7082ed2e9bf385beb472 $
 * @since 1.0.0
 */
abstract class Language {
    
    private static final Logger LOG = LoggerFactory.getLogger(Language.class.getName());

    /**
     * Commands that the agent can understand, in a given language.
     */
    private Properties commands = new Properties();
    
    /**
     * Responses that the agent can give, in a given language
     */
    private Properties responses = new Properties();
    
    Language(String commandsFileName, String responsesFileName) {
        try {
        	commands.load(
                this.getClass().getClassLoader().getResourceAsStream(commandsFileName)
            );
            responses.load(
                this.getClass().getClassLoader().getResourceAsStream(responsesFileName)
            );
        } catch (IOException e) {
            LOG.error("Exception when loading commands' patterns!", e);
            throw new IllegalStateException(e);
        }
    }

    String categorize(Command command) throws IOException {
        Set<Object> keys = this.commands.keySet();
        for(Object key : keys) {
            String keyString = (String) key;
            String[] words = this.commands.getProperty(keyString, "").split("\\^");
            boolean match = true;
            for(String word : words) {
                if(!command.json().getString("body").contains(word.trim())) {
                	match = false;
                }
            }
            if(match) {
            	return keyString.split("\\.")[0];
            }
        }
        return "unknown";
    }
    
    String response(String key) {
        return responses.getProperty(key);
    }
}
