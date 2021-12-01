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

import java.util.ArrayList;
import java.util.List;

/**
 * .charles.yml config file.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: ec30ce344ef131a492707ef3054f5432ada1acf0 $
 * @since 1.0.1
 *
 */
public interface CharlesYml {
    
    /**
     * Usernames of users who are allowed to command the bot.
     * @return String[]
     */
    List<String> commanders();

    /**
     * Should the bot tweet its activity?
     * @return Boolean.
     */
    boolean tweet();

    /**
     * Driver to use (phantomjs, chrome etc).
     * @return String driver.
     */
    String driver();

    /**
     * Patterns that should be ignored when graph-crawling (index-site command).
     * @return List of patterns.
     */
    List<String> ignored();

    /**
     * Default .charles.yml file.
     */
    final class Default implements CharlesYml {
        @Override
        public List<String> commanders() {
            return new ArrayList<>();
        }

        @Override
        public boolean tweet() {
            return false;
        }

        @Override
        public String driver() {
            return "chrome";
        }

        @Override
        public List<String> ignored() {
            return new ArrayList<>();
        }
    }
    
}
