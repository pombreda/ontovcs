package ru.tpu.cc.kms.statements;

import java.net.URI;
import java.net.URISyntaxException;

import ru.tpu.cc.kms.Signature;
import ru.tpu.cc.kms.changes.ComparableOntology;


public class NamespacePrefixStatement extends Statement {

	private String prefix;
	private String ns;

	public String getPrefix() {
		return prefix;
	}

	public String getNamespace() {
		return ns;
	}

	public NamespacePrefixStatement(final ComparableOntology o,
			final String prefix, final String ns) {
		super(o);
		this.setType(StatementType.PREFIX);
		this.prefix = prefix;
		this.ns = ns;
	}

	@Override
	public Signature getSignature() throws URISyntaxException {
		return new Signature(new URI(ns));
	}

    @Override
	public boolean equals(final Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
        if (!(obj instanceof NamespacePrefixStatement)) {
            return false;
        }
        NamespacePrefixStatement other = (NamespacePrefixStatement) obj;
        return  prefix.equals(other.prefix) && ns.equals(other.ns);
    }

	@Override
	public int compareTo(final Statement o) {
		if (this == o) return 0;
		if (!(o.getClass() == this.getClass()))
			return super.compareTo(o);
		NamespacePrefixStatement other = (NamespacePrefixStatement) o;
		return prefix.compareTo(other.prefix);
	}

    @Override
	public int hashCode() {
        return (prefix.hashCode() + ns.hashCode()) * 10 + 3;
    }

	@Override public String toString() {
		return "Prefix(" + prefix + "=<" + ns + ">)";
	}
}
