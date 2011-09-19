package ru.tpu.cc.kms.statements;

import java.net.URI;
import java.net.URISyntaxException;

import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;

public class OntologyFormatStatement extends Statement {

	private String format;

	public String getFormat() {
		return format;
	}

	public OntologyFormatStatement(final ComparableOntology o, final String format) {
		super(o);
		this.setType(StatementType.FORMAT);
		this.format = format;
	}

	@Override
	public Signature getSignature() throws URISyntaxException {
		// "Fake" URI
		return new Signature(new URI("ontology:format"));
	}

    @Override
	public boolean equals(final Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
        if (!(obj instanceof OntologyFormatStatement)) {
            return false;
        }
        OntologyFormatStatement other = (OntologyFormatStatement) obj;
        return format.equals(other.format);
    }

	@Override
	public int compareTo(final Statement o) {
		if (this == o) return 0;
		if (!(o.getClass() == this.getClass()))
			return super.compareTo(o);
		OntologyFormatStatement other = (OntologyFormatStatement) o;
		return format.compareTo(other.format);
	}

    @Override
	public int hashCode() {
        return format.hashCode() * 10 + 6;
    }

	@Override public String toString() {
		return "OntologyFormat(" + format + ")";
	}
}
