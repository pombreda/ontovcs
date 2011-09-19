package ru.tpu.cc.kms.statements;

import java.net.URISyntaxException;

import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;

/**
 * Statement base class
 * @author I
 *
 */
public abstract class Statement implements Comparable<Statement> {

	private StatementType type;
	private ComparableOntology owner;

	public Statement(final ComparableOntology o) {
		owner = o;
	}
	public StatementType getType() {
		return type;
	}
	protected void setType(final StatementType type) {
		this.type = type;
	}
	public ComparableOntology getOwner() {
		return owner;
	}
	public abstract Signature getSignature() throws URISyntaxException;
	@Override
	public abstract boolean equals(final Object obj);
	@Override
	public abstract int hashCode();
	@Override
	public int compareTo(final Statement o) {
		if (this == o) return 0;
		return this.type.compareTo(o.type);
	}
}
