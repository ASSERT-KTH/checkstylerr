/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.pipe.timing;

import java.util.Arrays;
import java.util.List;

import com.codahale.metrics.Timer.Context;

public final class TimingContext implements AutoCloseable {

	private final List<Context> contexts;

	public TimingContext(final Context... contexts) {
		this.contexts = Arrays.asList(contexts);
	}

	@Override
	public void close() {
		stop();
	}

	public void stop() {
		contexts.forEach(Context::stop);
	}
}
