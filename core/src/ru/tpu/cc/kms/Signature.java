package ru.tpu.cc.kms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.ChangeSet;
import ru.tpu.cc.kms.statements.Statement;

/**
 * Signature is a set of URIs used by an ontology,
 * an ontology statement, or a changeset
 * @author I
 *
 */

public class Signature extends HashSet<URI> {

	private static final long serialVersionUID = -3127650857589442572L;

	public Signature() {
        super();
    }
    public Signature(final Set<URI> s) {
        this.addAll(s);
    }
    public Signature(final URI uri) {
        this.add(uri);
    }
    public Signature(final Statement s) throws URISyntaxException {
        this(s.getSignature());
    }
    public Signature(final Change<Statement> c) throws URISyntaxException {
        this.addAll(c.getItem().getSignature());
    }
    public Signature(final ChangeSet<Statement> cs) throws URISyntaxException {
        for (Change<Statement> c : cs)
            this.addAll(c.getItem().getSignature());
    }
    public final Signature getIntersectionWith(final Signature sig) {
        Signature i = new Signature(this);
        i.retainAll(sig);
        return i;
    }
    public final boolean isIntersectionEmpty(final Signature sig) {
        if (this.size() < sig.size()) {
            for (URI uri : this)
                if (sig.contains(uri))
                    return false;
        } else {
            for (URI uri : sig)
                if (this.contains(uri))
                    return false;
        }
        return true;
    }
}
