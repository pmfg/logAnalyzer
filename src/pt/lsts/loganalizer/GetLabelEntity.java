package pt.lsts.loganalizer;

import java.util.HashMap;
import java.util.Map;

import pt.lsts.imc.EntityInfo;
import pt.lsts.imc.net.Consume;

public class GetLabelEntity {

    private Map<Integer, String> entityIdLabel = new HashMap<>();

    @Consume
    public void on(EntityInfo msg) {
        entityIdLabel.put((int) msg.getId(), msg.getLabel());
    }

    public Map<Integer, String> getEntityLabel() {
        return entityIdLabel;
    }
}
