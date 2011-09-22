package ru.tpu.cc.kms.statements;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ru.tpu.cc.kms.EntityShortener;
import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;


public class AxiomStatement extends Statement {

	private OWLAxiom axiom;

	public OWLAxiom getAxiom() {
		return axiom;
	}

	public AxiomStatement(final ComparableOntology o, final OWLAxiom a) {
		super(o);
		this.setType(StatementType.AXIOM);
		axiom = a;
	}

	@Override
	public Signature getSignature() {
		Signature s = new Signature();
		for (OWLEntity e : axiom.getSignature())
			s.add(e.getIRI().toURI());
		return s;
	}

    @Override
	public boolean equals(final Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
        if (!(obj instanceof AxiomStatement)) {
            return false;
        }
        AxiomStatement other = (AxiomStatement) obj;
        return axiom.equals(other.axiom);
    }
	@Override
	public int compareTo(final Statement o) {
		if (this == o) return 0;
		if (!(o.getClass() == this.getClass()))
			return super.compareTo(o);
		AxiomStatement other = (AxiomStatement) o;
		return axiom.compareTo(other.axiom);
	}

    @Override
	public int hashCode() {
        return axiom.hashCode() * 10 + 1;
    }

	@Override
	public String toString() {
		OWLOntology ontology;
		try {
			ontology = getOwner().getOntology();
		} catch (OWLOntologyCreationException e) {
			return axiom.toString();
		}
		return new EntityShortener(axiom, ontology).toString();
	}
}
