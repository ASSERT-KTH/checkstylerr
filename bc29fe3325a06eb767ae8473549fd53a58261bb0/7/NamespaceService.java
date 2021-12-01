/**
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.repository.services;

import java.util.Optional;
import org.eclipse.vorto.repository.domain.Namespace;
import org.eclipse.vorto.repository.repositories.NamespaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceService {

  @Autowired
  private NamespaceRepository namespaceRepository;

  public boolean exists(Namespace namespace) {
    return namespaceRepository.exists(namespace.getId());
  }

  public boolean exists(String namespaceName) {
    return Optional.ofNullable(namespaceRepository.findByName(namespaceName)).isPresent();
  }
}
