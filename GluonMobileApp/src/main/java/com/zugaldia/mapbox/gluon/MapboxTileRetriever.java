package com.zugaldia.mapbox.gluon;

import com.gluonhq.maps.tile.TileRetriever;
import javafx.scene.image.Image;
import okhttp3.HttpUrl;

/*
 * Heavily inspired by: https://github.com/gluonhq/maps/blob/master/src/main/java/com/gluonhq/impl/maps/tile/osm/OsmTileRetriever.java
 * We should implement caching like: https://github.com/gluonhq/maps/blob/master/src/main/java/com/gluonhq/impl/maps/tile/osm/CachedOsmTileRetriever.java
 */
public class MapboxTileRetriever implements TileRetriever {

    private String accessToken;

    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private String buildUrl(int zoom, long i, long j) {
        return HttpUrl.parse("https://api.mapbox.com/v4").newBuilder()
                .addPathSegment("mapbox.satellite")
                .addPathSegment(String.valueOf(zoom))
                .addPathSegment(String.valueOf(i))
                .addPathSegment(String.format("%s.png", String.valueOf(j)))
                .addQueryParameter("access_token", accessToken)
                .build()
                .toString();
    }

    @Override
    public Image loadTile(int zoom, long i, long j) {
        String url = buildUrl(zoom, i, j);
        return new Image(url, true);
    }
}
