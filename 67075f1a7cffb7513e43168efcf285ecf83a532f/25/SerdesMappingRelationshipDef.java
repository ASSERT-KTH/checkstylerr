/**
 * Copyright 2016-2020 Cloudera, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.cloudera.dim.atlas.types;

import org.apache.atlas.model.typedef.AtlasRelationshipDef;
import org.apache.atlas.model.typedef.AtlasRelationshipEndDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef.Cardinality;

public class SerdesMappingRelationshipDef extends AtlasRelationshipDef implements SchemaRegistryServiceType {

    public static final String RELATIONSHIP_NAME = "schema_serdes_mapping"+TODORemoveThis.COUNTER;
    public static final String RELATIONSHIP_LABEL = "schema_serdes";

    public static final String REL_ONE = "serdes";  // rel.name from the PoV of meta
    public static final String REL_MANY = "schema_meta";  // rel.name from the PoV of serdes

    SerdesMappingRelationshipDef() {
        setName(RELATIONSHIP_NAME);
        setServiceType(SERVICE_TYPE);
        setTypeVersion(TYPE_VERSION);
        setRelationshipCategory(RelationshipCategory.AGGREGATION);
        setRelationshipLabel(RELATIONSHIP_LABEL);
        setPropagateTags(PropagateTags.NONE);

        setEndDef1(new AtlasRelationshipEndDef(
                MetadataEntityDef.SCHEMA_METADATA_INFO,
                REL_ONE,
                Cardinality.SET, true, true
        ));
        setEndDef2(new AtlasRelationshipEndDef(
                SerdesEntityDef.SCHEMA_SERDES_INFO,
                REL_MANY,
                Cardinality.SINGLE, false, true
        ));
    }

}
