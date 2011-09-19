package ru.tpu.cc.kms.statements;

import org.semanticweb.owlapi.model.IRI;

import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;


public class OntologyIRIStatement extends Statement {

	private IRI iri;

	public IRI getIri() {
		return iri;
	}

	public OntologyIRIStatement(final ComparableOntology o, final IRI iri) {
		super(o);
		this.setType(StatementType.OIRI);
		this.iri = iri;
	}

	@Override
	public Signature getSignature() {
		return new Signature(iri.toURI());
	}

    @Override
	public boolean equals(final Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
        if (!(obj instanceof OntologyIRIStatement)) {
            return false;
        }
        OntologyIRIStatement other = (OntologyIRIStatement) obj;
        return iri.equals(other.iri);
    }

	@Override
	public int compareTo(final Statement o) {
		if (this == o) return 0;
		if (!(o.getClass() == this.getClass()))
			return super.compareTo(o);
		return iri.compareTo(((OntologyIRIStatement) o).iri);
	}

    @Override
	public int hashCode() {
        return iri.hashCode() * 10 + 4;
    }

	@Override public String toString() {
		return "OntologyIRI(<" + iri + ">)";
	}
}
