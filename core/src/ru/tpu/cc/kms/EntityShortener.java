package ru.tpu.cc.kms;

import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

public class EntityShortener {

    private IriFormat iriFormat;
    private OWLOntology ontology;
    private OWLOntologyManager manager;

    public EntityShortener(IriFormat iriFormat) {
        super();
        this.iriFormat = iriFormat;
    }

    public EntityShortener(IriFormat iriFormat, OWLOntology ontology) {
        this(iriFormat);
        this.ontology = ontology;
        manager = ontology.getOWLOntologyManager();
    }

    public String shorten(OWLObject object) {
        if (object == null)
            throw new NullPointerException("Object is null");
        if (iriFormat == IriFormat.FULL)
            return object.toString();
        OWLOntologyFormat ontologyFormat;
        if (ontology != null) {
            ontologyFormat = manager.getOntologyFormat(ontology);
            if (!(ontologyFormat instanceof PrefixOWLOntologyFormat))
                return object.toString();
        } else {
            ontologyFormat = new OWLFunctionalSyntaxOntologyFormat();
        }
        ToStringRenderer tsr = ToStringRenderer.getInstance();
        DefaultPrefixManager prefixManager = new DefaultPrefixManager();
        prefixManager.clear();
        if ((ontology != null) && (!ontology.isAnonymous())) {
            String defPrefix = ontology.getOntologyID().getOntologyIRI() + "#";
            prefixManager.setDefaultPrefix(defPrefix);
        }
        PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) ontologyFormat;
        ShortFormProvider provider = null;
        switch (iriFormat) {
        case SIMPLE:
            provider = new SimpleShortFormProvider();
            break;
        case QNAME:
            provider = new QNameShortFormProvider(prefixFormat.getPrefixName2PrefixMap());
            break;
        case FULL:
            provider = new FullFormProvider();
            break;
        }
        tsr.setShortFormProvider(provider);
        return tsr.getRendering(object);
    }
}
