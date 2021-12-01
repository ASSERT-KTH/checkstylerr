package io.openems.edge.ess.power.api;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

/**
 * Creates a constraint with following settings:
 * <ul>
 * <li>Relationship (EQ, GEQ, LEQ) as given in constructor
 * <li>Value as given in constructor
 * <li>Setting each coefficient, i.e.
 * 
 * <pre>
 * y = 1*p1 + 0*q1 * + 1*p2 + 0*q1 +...
 * </pre>
 * </ul>
 */
public class Constraint {

	private final static DecimalFormat VALUE_FORMAT = new DecimalFormat("0.#");

	private final String description;
	private final LinearCoefficient[] coefficients;
	private final Relationship relationship;

	private Optional<Double> value;

	public Constraint(String description, LinearCoefficient[] coefficients, Relationship relationship, double value) {
		this(description, coefficients, relationship, Optional.of(value));
	}

	public Constraint(String description, List<LinearCoefficient> coefficients, Relationship relationship,
			double value) {
		this(description, coefficients.toArray(new LinearCoefficient[coefficients.size()]), relationship, value);
	}

	/**
	 * Creates an initially disabled Constraint
	 * 
	 * @param coefficients
	 * @param relationship
	 */
	public Constraint(String description, LinearCoefficient[] coefficients, Relationship relationship) {
		this(description, coefficients, relationship, Optional.empty());
	}

	public Constraint(String description, LinearCoefficient[] coefficients, Relationship relationship,
			Optional<Double> value) {
		this.description = description;
		this.coefficients = coefficients;
		this.relationship = relationship;
		this.value = value;
	}

	public Constraint(String description, List<LinearCoefficient> coefficients, Relationship relationship,
			Optional<Double> value) {
		this(description, coefficients.toArray(new LinearCoefficient[coefficients.size()]), relationship, value);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(String.format("%-30s", this.description));
		for (LinearCoefficient c : this.coefficients) {
			b.append(c.toString());
		}
		b.append(" " + relationship.toString() + " ");
		if (this.value.isPresent()) {
			b.append(VALUE_FORMAT.format(this.value.get()));
		} else {
			b.append("DISABLED");
		}
		return b.toString();
	}

	public LinearCoefficient[] getCoefficients() {
		return coefficients;
	}

	public Relationship getRelationship() {
		return relationship;
	}

	public Optional<Double> getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = Optional.ofNullable(value);
	}

	public void disable() {
		this.value = Optional.empty();
	}

}
