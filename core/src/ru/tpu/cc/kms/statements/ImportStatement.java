package ru.tpu.cc.kms.statements;

import org.semanticweb.owlapi.model.OWLImportsDeclaration;

import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;


public class ImportStatement extends Statement {

	private OWLImportsDeclaration decl;

	public OWLImportsDeclaration getImport() {
		return decl;
	}

	public ImportStatement(final ComparableOntology o,
			final OWLImportsDeclaration d) {
		super(o);
		this.setType(StatementType.IMPORT);
		this.decl = d;
	}

	@Override
	public Signature getSignature() {
		return new Signature(decl.getURI());
	}

    @Override
	public boolean equals(final Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
        if (!(obj instanceof ImportStatement)) {
            return false;
        }
        ImportStatement other = (ImportStatement) obj;
        return decl.equals(other.decl);
    }

	@Override
	public int compareTo(final Statement o) {
		if (this == o) return 0;
		if (!(o.getClass() == this.getClass()))
			return super.compareTo(o);
		ImportStatement other = (ImportStatement) o;
		return decl.compareTo(other.decl);
	}

    @Override
	public int hashCode() {
        return decl.hashCode() * 10 + 2;
    }

	@Override public String toString() {
		return decl.toString();
	}

}
