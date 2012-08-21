package ru.tpu.cc.kms.changes.render;

import java.util.Map;

import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import ru.tpu.cc.kms.FullFormProvider;
import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.statements.Statement;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;

public abstract class ChangeRenderer {

    protected ShortFormProvider provider;

    public ChangeRenderer(ShortFormProvider provider) {
        this.provider = provider;
    }

    public ChangeRenderer(IriFormat iriFormat) {
        super();
        switch (iriFormat) {
        case SIMPLE:
            provider = new SimpleShortFormProvider();
            break;
        case QNAME:
            DefaultPrefixManager prefixManager = new DefaultPrefixManager();
            prefixManager.clear();
            provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(prefixManager);
            break;
        case FULL:
            provider = new FullFormProvider();
            break;
        }
    }

    public ChangeRenderer(IriFormat iriFormat, Map<String, String> prefixMap) {
        this(iriFormat);
        if (iriFormat == IriFormat.QNAME) {
            DefaultPrefixManager prefixManager = new DefaultPrefixManager();
            prefixManager.clear();
            for(Map.Entry<String, String> e : prefixMap.entrySet()) {
                prefixManager.setPrefix(e.getKey(), e.getValue());
            }
            provider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(prefixManager);
        }
    }

    public abstract String getRendering(Change<Statement> change);
}
