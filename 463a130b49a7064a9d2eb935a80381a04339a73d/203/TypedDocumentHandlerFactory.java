/*
 * Copyright (c) 2011 TIBCO Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.genxdm.typed.io;

import javax.xml.stream.XMLReporter;

import org.genxdm.io.Resolver;

public interface TypedDocumentHandlerFactory<N, A>
{
    TypedDocumentHandler<N, A> newDocumentHandler(final SAXValidator<A> validator, final XMLReporter reporter, final Resolver resolver);
  
    // TODO: add the stax variant for document validation
//    TypedDocumentHandler<N, A> newDocumentHandler(final StAXValidator<A> validator, final XMLReporter reporter, final Resolver resolver);
}
