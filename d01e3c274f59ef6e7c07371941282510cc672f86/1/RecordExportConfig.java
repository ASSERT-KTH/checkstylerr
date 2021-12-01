package com.mobi.catalog.api.record.config;

/*-
 * #%L
 * com.mobi.etl.api
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2016 - 2017 iNovex Information Systems, Inc.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.openrdf.rio.RDFFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class RecordExportConfig extends BaseExportRecordConfig {
    private String record;

    protected RecordExportConfig(Builder builder) {
        super(builder);
        this.record = builder.record;
    }

    public String getRecord() {
        return record;
    }

    public static class Builder extends BaseExportRecordConfig.Builder {
        private String record;

        public Builder(OutputStream output, RDFFormat format) {
            super(output, format);
        }

        /**
         * The set of catalog records to export.
         *
         * @param record The record to export
         * @return The Builder
         * @throws IllegalArgumentException if the set is empty
         */
        public Builder record(String record) {
            if (record.isEmpty()) throw new IllegalArgumentException("Record cannot be empty.");
            return this;
        }

        public RecordExportConfig build() throws IOException {
            return new RecordExportConfig(this);
        }
    }
}
