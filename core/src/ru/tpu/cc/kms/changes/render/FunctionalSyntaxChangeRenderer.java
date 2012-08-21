package ru.tpu.cc.kms.changes.render;

import java.util.Map;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.Op;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.render.FunctionalSyntaxStatementRenderer;

public class FunctionalSyntaxChangeRenderer extends ChangeRenderer {

    public FunctionalSyntaxChangeRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    public FunctionalSyntaxChangeRenderer(IriFormat iriFormat, Map<String, String> prefixMap) {
        super(iriFormat, prefixMap);
    }

    @Override
    public String getRendering(Change<Statement> change) {
        String s = new FunctionalSyntaxStatementRenderer(provider).getRendering(change.getItem());
        if (change.getOp() == Op.ADD)
            return "+ " + s;
        else
            return "- " + s;
    }

}
