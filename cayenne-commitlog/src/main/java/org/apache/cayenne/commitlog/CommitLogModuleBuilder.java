/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.commitlog;

import org.apache.cayenne.configuration.server.ServerModule;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.ListBuilder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.commitlog.meta.AnnotationCommitLogEntityFactory;
import org.apache.cayenne.commitlog.meta.IncludeAllCommitLogEntityFactory;
import org.apache.cayenne.commitlog.meta.CommitLogEntity;
import org.apache.cayenne.commitlog.meta.CommitLogEntityFactory;
import org.apache.cayenne.tx.TransactionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

/**
 * A builder of a module that integrates {@link CommitLogFilter} and
 * {@link CommitLogListener} in Cayenne.
 * 
 * @since 4.0
 */
public class CommitLogModuleBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommitLogModuleBuilder.class);

	public static CommitLogModuleBuilder builder() {
		return new CommitLogModuleBuilder();
	}

	private Class<? extends CommitLogEntityFactory> entityFactoryType;
	private Collection<Class<? extends CommitLogListener>> listenerTypes;
	private Collection<CommitLogListener> listenerInstances;
	private boolean excludeFromTransaction;

	CommitLogModuleBuilder() {
		entityFactory(IncludeAllCommitLogEntityFactory.class);
		this.listenerTypes = new HashSet<>();
		this.listenerInstances = new HashSet<>();
	}

	public CommitLogModuleBuilder listener(Class<? extends CommitLogListener> type) {
		this.listenerTypes.add(type);
		return this;
	}

	public CommitLogModuleBuilder listener(CommitLogListener instance) {
		this.listenerInstances.add(instance);
		return this;
	}

	/**
	 * If called, events will be dispatched outside of the main commit
	 * transaction. By default events are dispatched within the transaction, so
	 * listeners can commit their code together with the main commit.
	 */
	public CommitLogModuleBuilder excludeFromTransaction() {
		this.excludeFromTransaction = true;
		return this;
	}

	/**
	 * Installs entity filter that would only include entities annotated with
	 * {@link CommitLog} on the callbacks. Also {@link CommitLog#confidential()}
	 * properties will be obfuscated and {@link CommitLog#ignoredProperties()} -
	 * excluded from the change collection.
	 */
	public CommitLogModuleBuilder commitLogAnnotationEntitiesOnly() {
		return entityFactory(AnnotationCommitLogEntityFactory.class);
	}

	/**
	 * Installs a custom factory for {@link CommitLogEntity} objects that
	 * allows implementors to use their own annotations, etc.
	 */
	public CommitLogModuleBuilder entityFactory(Class<? extends CommitLogEntityFactory> entityFactoryType) {
		this.entityFactoryType = entityFactoryType;
		return this;
	}

	/**
	 * Creates a DI module that would install {@link CommitLogFilter} and its
	 * listeners in Cayenne.
	 */
	public Module build() {
		return new Module() {

			@SuppressWarnings({ "unchecked" })
			@Override
			public void configure(Binder binder) {

				if (listenerTypes.isEmpty() && listenerInstances.isEmpty()) {
					LOGGER.info("No listeners configured. Skipping CommitLogFilter registration");
					return;
				}

				binder.bind(CommitLogEntityFactory.class).to(entityFactoryType);

				ListBuilder<CommitLogListener> listeners = CommitLogModule.contributeListeners(binder)
						.addAll(listenerInstances);

				// types have to be added one-by-one
				for (Class<? extends CommitLogListener> type : listenerTypes) {
					// TODO: temp hack - need to bind each type before adding to collection...
					binder.bind(type).to((Class)type);
					listeners.add(type);
				}

				if (excludeFromTransaction) {
					ServerModule.contributeDomainFilters(binder).addAfter(CommitLogFilter.class, TransactionFilter.class);
				} else {
					ServerModule.contributeDomainFilters(binder).insertBefore(CommitLogFilter.class, TransactionFilter.class);
				}
			}
		};
	}
}
