package com.zugaldia.mapbox.gluon;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.StorageService;
import com.gluonhq.maps.tile.TileRetriever;
import javafx.scene.image.Image;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Heavily inspired by: https://github.com/gluonhq/maps/blob/master/src/main/java/com/gluonhq/impl/maps/tile/osm/OsmTileRetriever.java
 * and: https://github.com/gluonhq/maps/blob/master/src/main/java/com/gluonhq/impl/maps/tile/osm/CachedOsmTileRetriever.java
 */
public class MapboxTileRetriever implements TileRetriever {

    private static final Logger logger = Logger.getLogger(MapboxTileRetriever.class.getSimpleName());

    private String accessToken;

    private static File cacheRoot;
    private static boolean hasFileCache;
    private final static long MAX_CACHE_SIZE = 10 * 1024 * 1024; // 10 MiB

    static {
        try {
            File storageRoot = Services.get(StorageService.class)
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new IOException("Storage Service is not available."));

            cacheRoot = new File(storageRoot, ".mapbox");
            if (!cacheRoot.isDirectory()) {
                hasFileCache = cacheRoot.mkdirs();
            } else {
                hasFileCache = true;
            }
        } catch (IOException e) {
            hasFileCache = false;
            logger.log(Level.SEVERE, "Failed to obtain cache path.", e);
        }
    }

    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private HttpUrl buildUrl(int zoom, long i, long j) {
        return HttpUrl.parse("https://api.mapbox.com/v4").newBuilder()
                .addPathSegment("mapbox.satellite")
                .addPathSegment(String.valueOf(zoom))
                .addPathSegment(String.valueOf(i))
                .addPathSegment(String.format("%s.png", String.valueOf(j)))
                .addQueryParameter("access_token", accessToken)
                .build();
    }

    @Override
    public Image loadTile(int zoom, long i, long j) {
        try {
            HttpUrl url = buildUrl(zoom, i, j);
            InputStream is = obtainImageStream(url);
            return new Image(is);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load tile.", e);
            return null;
        }
    }

    private InputStream obtainImageStream(HttpUrl url) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (hasFileCache) {
            builder.cache(new Cache(cacheRoot, MAX_CACHE_SIZE));
        }

        OkHttpClient client = builder.build();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }
}
