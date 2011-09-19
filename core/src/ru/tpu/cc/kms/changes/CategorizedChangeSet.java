package ru.tpu.cc.kms.changes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;

import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.StatementType;

public class CategorizedChangeSet {

	private ComparableOntology parent;
	private ComparableOntology child;

	private Map<StatementType, ChangeSet<Statement>> changesByType =
			new HashMap<StatementType, ChangeSet<Statement>>();

	public ChangeSet<Statement> getChangesByType(final StatementType type) {
		return changesByType.get(type);
	}

	public CategorizedChangeSet() {
		for (StatementType st : StatementType.values())
			changesByType.put(st, new ChangeSet<Statement>());
	}

	public CategorizedChangeSet(final ComparableOntology parent, final ComparableOntology child) {
		this.parent = parent;
		this.child = child;
		for (StatementType st : StatementType.values())
			changesByType.put(st, new ChangeSet<Statement>(
        		parent.getStatementsByType(st),
        		child.getStatementsByType(st)));
	}

	public CategorizedChangeSet(final OWLOntology parent, final OWLOntology child) {
		this(new ComparableOntology(parent), new ComparableOntology(child));
	}

	public CategorizedChangeSet(final CategorizedChangeSet cs) {
		for (StatementType st : StatementType.values())
			changesByType.put(st, new ChangeSet<Statement>(cs.changesByType.get(st)));
	}

	public final Collection<Change<Statement>> getAllChanges() {
		ChangeSet<Statement> changes = new ChangeSet<Statement>();
		for (StatementType st : StatementType.values())
			changes.addAll(changesByType.get(st));
		return changes;
	}

	public final CategorizedChangeSet getIntersectionWith(final CategorizedChangeSet cs) {
		CategorizedChangeSet r = new CategorizedChangeSet();
		for (StatementType st : StatementType.values()) {
			ChangeSet <Statement> t = new ChangeSet<Statement>(cs.changesByType.get(st));
			t.retainAll(cs.changesByType.get(st));
			r.changesByType.put(st, t);
		}
		return r;
	}

	public ComparableOntology getParent() {
		return parent;
	}

	public ComparableOntology getChild() {
		return child;
	}
}



