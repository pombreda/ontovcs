package ru.tpu.cc.kms.statements.render;

import org.semanticweb.owlapi.model.OWLException;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.statements.Statement;

public abstract class StatementRenderer {

    protected IriFormat iriFormat;

    public StatementRenderer(IriFormat iriFormat) {
        super();
        this.iriFormat = iriFormat;
    }

    public abstract String getRendering(Statement statement) throws OWLException;

}
