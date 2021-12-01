/*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */

package org.gluu.persist.operation;

import org.gluu.persist.exception.extension.PersistenceExtension;

/**
 * Base interface for Operation Service
 *
 * @author Yuriy Movchan Date: 06/22/2018
 */
public interface PersistenceOperationService {

    boolean isConnected();

	public void setPersistenceExtension(PersistenceExtension persistenceExtension);

}
