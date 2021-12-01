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

import java.util.List;

import com.amihaiemil.charles.DataExportException;
import com.amihaiemil.charles.Repository;
import com.amihaiemil.charles.WebPage;

/**
 * Our repository is an ElasticSearch Index in the AWS cloud. Extend the Repository interface
 * to add our methods too.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: f9d39a071884a260c82cf2dce493b4ea710582c6 $
 */
public interface AwsEsRepository extends Repository {
	
	/**
	 * Does the index exist?
	 * @return True or false.
	 */
    boolean exists();
    
    /**
     * Delete this intex.
     */
    void delete();

    /**
     * Delete a single document from this index.
     * @param type Type withing index.
     * @param id Id of the document.
     * @see https://www.elastic.co/guide/en/elasticsearch/reference/2.3/docs-delete.html
     */
    void delete(final String type, final String id);
    
    /**
     * Fake for unit tests.
     *
     */
    final static class Fake implements AwsEsRepository {

        /**
         * Does this index exist?
         */
        private boolean indexExists;

        /**
         * Ctor.
         * @param exists True if the index should exist, false otherwise.
         */
        public Fake(boolean exists) {
            this.indexExists = exists;
        }
        @Override
        public void export(List<WebPage> pages) throws DataExportException {
            //Fake export; nothing to do.
        }

        @Override
        public boolean exists() {
            return this.indexExists;
        }

        @Override
        public void delete() {
            //Fake delete; nothing to do.
        }
		@Override
		public void delete(String type, String id) {
			 //Fake delete; nothing to do.
		}
        
    }
}
