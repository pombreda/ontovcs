package ru.tpu.cc.kms.statements;

import org.semanticweb.owlapi.model.IRI;

import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;

public class VersionIRIStatement extends Statement {

	private IRI iri;

	public IRI getIRI() {
		return iri;
	}

	public VersionIRIStatement(final ComparableOntology o, final IRI iri) {
		super(o);
		this.setType(StatementType.VIRI);
		this.iri = iri;
	}

	@Override
	public final Signature getSignature() {
		return new Signature(iri.toURI());
	}

    @Override
	public final boolean equals(final Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
        if (!(obj instanceof VersionIRIStatement)) {
            return false;
        }
        VersionIRIStatement other = (VersionIRIStatement) obj;
        return iri.equals(other.iri);
    }

	@Override
	public final int compareTo(final Statement o) {
		if (this == o) return 0;
		if (!(o.getClass() == this.getClass()))
			return super.compareTo(o);
		return iri.compareTo(((VersionIRIStatement) o).iri);
	}

    @Override
	public final int hashCode() {
        return iri.hashCode() * 10 + 5;
    }

	@Override
	public final String toString() {
		return "VersionIRI(<" + iri + ">)";
	}
}
