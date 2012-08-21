package ru.tpu.cc.kms.changes.render;

import java.util.Map;

import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.Op;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.render.IndentedStatementRenderer;

public class IndentedChangeRenderer extends ChangeRenderer {

    public IndentedChangeRenderer(IriFormat iriFormat) {
        super(iriFormat);
    }

    public IndentedChangeRenderer(IriFormat iriFormat, Map<String, String> prefixMap) {
        super(iriFormat, prefixMap);
    }

    @Override
    public String getRendering(Change<Statement> change) {
        String s;
        s = new IndentedStatementRenderer(provider).getRendering(change.getItem());
        if (change.getOp() == Op.ADD)
            return "+ " + s;
        else
            return "- " + s;
    }

}
