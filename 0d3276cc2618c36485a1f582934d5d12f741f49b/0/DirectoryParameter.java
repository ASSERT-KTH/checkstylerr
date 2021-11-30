/*
 *  Copyright 2002-2019 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package es.bsc.compss.types.parameter;

import es.bsc.compss.types.annotations.parameter.DataType;
import es.bsc.compss.types.annotations.parameter.Direction;
import es.bsc.compss.types.annotations.parameter.StdIOStream;
import es.bsc.compss.types.data.location.DataLocation;


public class DirectoryParameter extends DependencyParameter {

    /**
     * Serializable objects Version UID are 1L in all Runtime.
     */
    private static final long serialVersionUID = 1L;

    // Same as FileParameter fields
    private final DataLocation location;
    private final String originalName;


    /**
     * Creates a new Directory Parameter.
     *
     * @param direction Parameter direction.
     * @param stream Standard IO Stream flags.
     * @param prefix Parameter prefix.
     * @param name Parameter name.
     * @param location Directory location.
     * @param originalName Original dir name.
     */
    public DirectoryParameter(Direction direction, StdIOStream stream, String prefix, String name, String contentType,
                              DataLocation location, String originalName) {

        super(DataType.DIRECTORY_T, direction, stream, prefix, name, contentType);
        this.location = location;
        this.originalName = originalName;

    }

    public DataLocation getLocation() {
        return this.location;
    }

    @Override
    public String getOriginalName() {
        return this.originalName;
    }

    @Override
    public String toString() {
        return "DirectoryParameter with location " + this.location + ", type " + getType() + ", direction " + getDirection();
    }

}
