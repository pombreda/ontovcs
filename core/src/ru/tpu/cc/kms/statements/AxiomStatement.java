package ru.tpu.cc.kms.statements;

import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

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
		ToStringRenderer tsr = ToStringRenderer.getInstance();
		OWLOntology ontology;
		try {
			ontology = getOwner().getOntology();
		} catch (OWLOntologyCreationException e) {
			return this.toString();
		}
		OWLOntologyManager man = ontology.getOWLOntologyManager();
        DefaultPrefixManager prefixManager = new DefaultPrefixManager();
        prefixManager.clear();
        if (!ontology.isAnonymous()) {
            String defPrefix = ontology.getOntologyID().getOntologyIRI() + "#";
            prefixManager.setDefaultPrefix(defPrefix);
        }
        OWLOntologyFormat ontologyFormat = man.getOntologyFormat(ontology);
        if (!(ontologyFormat instanceof PrefixOWLOntologyFormat))
        	return this.toString();
        PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) ontologyFormat;
		ShortFormProvider provider = new QNameShortFormProvider(prefixFormat.getPrefixName2PrefixMap());
		tsr.setShortFormProvider(provider);
		return tsr.getRendering(axiom);
	}
}
