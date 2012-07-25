package ru.tpu.cc.kms.changes.render;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.statements.Statement;

public abstract class ChangeRenderer {

    protected IriFormat iriFormat;

    public ChangeRenderer(IriFormat iriFormat) {
        super();
        this.iriFormat = iriFormat;
    }

    public abstract String getRendering(Change<Statement> change);
}
