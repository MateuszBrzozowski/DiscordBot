package stats;

public class MapLayer extends Maps {

    private final String layerName;

    public MapLayer(String name, String layerName) {
        super(name);
        this.layerName = layerName;
    }

    public String getLayerName() {
        return layerName;
    }
}
