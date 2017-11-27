package pt.lsts.loganalizer;

import pt.lsts.imc.EntityInfo;
import pt.lsts.imc.net.Consume;

public class GetLabelEntity {

    static String[] entityIdLabel = new String[256];

    @Consume
    public void on(EntityInfo msg) {
        entityIdLabel[msg.getId()] = msg.getLabel();
    }

    String[] getEntityLabel() {
        return entityIdLabel;
    }
}
