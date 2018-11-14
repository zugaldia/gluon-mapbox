package com.zugaldia.mapbox.gluon;

import com.gluonhq.maps.MapView;
import com.gluonhq.maps.tile.TileRetrieverProvider;

public class MapboxView extends MapView {

    public MapboxView(String accessToken) {
        ((MapboxTileRetriever) TileRetrieverProvider.getInstance().load()).setAccessToken(accessToken);
    }
}
