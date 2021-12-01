package com.bakdata.conquery.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.bakdata.conquery.apiv1.QueryDescription;
import com.bakdata.conquery.models.auth.permissions.Ability;
import com.bakdata.conquery.models.auth.permissions.ConceptPermission;
import com.bakdata.conquery.models.auth.permissions.ConqueryPermission;
import com.bakdata.conquery.models.concepts.Connector;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.SecondaryIdDescription;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptElementId;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.query.Visitable;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.concept.NamespacedIdHolding;
import com.bakdata.conquery.models.query.concept.specific.CQAnd;
import com.bakdata.conquery.models.query.concept.specific.CQConcept;
import com.bakdata.conquery.models.query.concept.specific.CQExternal;
import com.bakdata.conquery.models.query.concept.specific.CQOr;
import com.bakdata.conquery.models.query.concept.specific.CQReusedQuery;
import com.bakdata.conquery.models.query.visitor.QueryVisitor;
import com.google.common.collect.ClassToInstanceMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class QueryUtils {

	/**
	 * Provides a starting operator for consumer chains, that does nothing.
	 */
	public static <T> Consumer<T> getNoOpEntryPoint() {
		return (whatever) -> {};
	}
	
	/**
	 * Gets the specified visitor from the map. If none was found an exception is raised.
	 */
	public static <T extends QueryVisitor> T getVisitor(ClassToInstanceMap<QueryVisitor> visitors, Class<T> clazz){
		return Objects.requireNonNull(visitors.getInstance(clazz),String.format("Among the visitor that traversed the query no %s could be found", clazz));
	}

	public static String createDefaultMultiLabel(List<CQElement> elements, String delimiter, Locale locale) {
		return elements.stream().map(elt -> elt.getLabel(locale)).collect(Collectors.joining(delimiter));
	}

	/**
	 * Checks if the query requires to resolve external ids.
	 *
	 * @return True if a {@link CQExternal} is found.
	 */
	public static class ExternalIdChecker implements QueryVisitor {

		private final List<CQElement> elements = new ArrayList<>();

		@Override
		public void accept(Visitable element) {
			if (element instanceof CQExternal) {
				elements.add((CQElement) element);
			}
		}

		public boolean resolvesExternalIds() {
			return !elements.isEmpty();
		}
	}

	/**
	 * Test if this query is only reusing a different query (ie not combining it with other elements, or changing its secondaryId)
	 */
	public static class OnlyReusingChecker implements QueryVisitor {

		private CQReusedQuery reusedQuery = null;
		private boolean containsOthersElements = false;

		@Override
		public void accept(Visitable element) {
			if(containsOthersElements){
				return;
			}

			if (element instanceof CQReusedQuery) {
				// We would have to reason way too much about the sent query so we just reexecute it.
				containsOthersElements = containsOthersElements || ((CQReusedQuery) element).isExcludeFromSecondaryId();

				if (reusedQuery == null) {
					reusedQuery = (CQReusedQuery) element;
				}
				else {
					containsOthersElements = true;
				}
				return;
			}

			if (!(element instanceof CQAnd || element instanceof CQOr || element instanceof QueryDescription)) {
				containsOthersElements = true;
			}
		}

		public Optional<ManagedExecutionId> getOnlyReused() {
			if (containsOthersElements || reusedQuery == null) {
				return Optional.empty();
			}

			return Optional.of(reusedQuery.getQuery());
		}
	}


	/**
	 * Find all ReusedQuery in the queries tree, and return their Ids.
	 *
	 */
	public static class AllReusedFinder implements QueryVisitor {

		@Getter
		final List<CQReusedQuery> reusedElements = new ArrayList<>();

		@Override
		public void accept(Visitable element) {
			if (element instanceof CQReusedQuery) {
				reusedElements.add((CQReusedQuery) element);
			}
		}

	}

	/**
	 * Collects all {@link NamespacedId} references provided by a user from a
	 * {@link Visitable}.
	 */
	public static class NamespacedIdCollector implements QueryVisitor {

		@Getter
		private Set<NamespacedId> ids = new HashSet<>();

		@Override
		public void accept(Visitable element) {
			if (element instanceof NamespacedIdHolding) {
				NamespacedIdHolding idHolder = (NamespacedIdHolding) element;
				idHolder.collectNamespacedIds(ids);
			}
		}
	}

	/**
	 * Collects all {@link NamespacedId} references provided by a user from a
	 * {@link Visitable}.
	 */
	public static class AvailableSecondaryIdCollector implements QueryVisitor {

		@Getter
		private final Set<SecondaryIdDescription> ids = new HashSet<>();

		@Override
		public void accept(Visitable element) {
			if(element instanceof CQConcept){
				final CQConcept cqConcept = (CQConcept) element;

				// Excluded Concepts are not available
				if(cqConcept.isExcludeFromSecondaryIdQuery()){
					return;
				}

				for (Connector connector : cqConcept.getConcept().getConnectors()) {
					for (Column column : connector.getTable().getColumns()) {
						if(column.getSecondaryId() == null){
							continue;
						}

						ids.add(column.getSecondaryId());
					}
				}
			}
		}
	}
	
	public static void generateConceptReadPermissions(@NonNull NamespacedIdCollector idCollector, @NonNull Collection<ConqueryPermission> collectPermissions){
		//TODO id prefer to not use the ids here, instead refactor NamespacedIdCollector to collect the Objects.
		idCollector.getIds().stream()
				   .filter(id -> ConceptElementId.class.isAssignableFrom(id.getClass()))
				   .map(ConceptElementId.class::cast)
				   .map(ConceptElementId::findConcept)
				   .map(cId -> ConceptPermission.onInstance(Ability.READ, cId))
				   .distinct()
				   .collect(Collectors.toCollection(() -> collectPermissions));
	}
}
