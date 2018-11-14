package com.zugaldia.mapbox.gluon;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.down.plugins.PositionService;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.maps.MapPoint;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BasicView extends View {

    private final static String MAPBOX_ACCESS_TOKEN = "";

    // Dupont Circle, Washington, DC
    private final static MapPoint DEFAULT_CENTER = new MapPoint(38.90962, -77.04341);
    private final static double DEFAULT_ZOOM = 15;

    private Position currentPosition = null;
    private CircleLayer coffeeLayer = new CircleLayer(Color.BLUE);

    public BasicView() {
        Label label = new Label("Tap the button to find coffee places nearby.");

        Button button = new Button("Search");
        button.setGraphic(new Icon(MaterialDesignIcon.LANGUAGE));
        button.setOnAction(e -> findCoffeePlaces());

        MapboxView mapboxView = new MapboxView(MAPBOX_ACCESS_TOKEN);
        mapboxView.setCenter(DEFAULT_CENTER);
        mapboxView.setZoom(DEFAULT_ZOOM);
        mapboxView.addLayer(locationLayer());
        mapboxView.addLayer(coffeeLayer);
        mapboxView.setMaxHeight(750);

        VBox vBox = new VBox(25, label, button, mapboxView);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(25, 0, 0, 0));

        setCenter(vBox);
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setTitleText("Gluon + Mapbox");
    }

    private CircleLayer locationLayer() {
        CircleLayer layer = new CircleLayer(Color.RED);

        return Services.get(PositionService.class)
                .map(positionService -> {
                    positionService.start();
                    ReadOnlyObjectProperty<Position> positionProperty = positionService.positionProperty();
                    Position position = positionProperty.get();
                    if (position == null) {
                        showMessage("Your location isn't available.");
                    } else {
                        currentPosition = position;
                        layer.addPoint(new MapPoint(position.getLatitude(), position.getLongitude()));
                    }

                    return layer;
                }).orElseGet(() -> {
                    showMessage("Location service isn't available.");
                    return layer;
                });
    }

    private void findCoffeePlaces() {
        Point proximity = currentPosition == null ?
                Point.fromLngLat(DEFAULT_CENTER.getLongitude(), DEFAULT_CENTER.getLatitude()) :
                Point.fromLngLat(currentPosition.getLongitude(), currentPosition.getLatitude());

        MapboxGeocoding client = MapboxGeocoding.builder()
                .accessToken(MAPBOX_ACCESS_TOKEN)
                .proximity(proximity)
                .query("starbucks")
                .build();

        showMessage("Searching...");
        client.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                try {
                    showMessage(String.format("%d coffee places found nearby.", response.body().features().size()));
                    Platform.runLater(() -> {
                        for (CarmenFeature place : response.body().features()) {
                            coffeeLayer.addPoint(new MapPoint(place.center().latitude(), place.center().longitude()));
                        }
                    });
                } catch (Exception e) {
                    showMessage(String.format("Search failed: %s", e.getMessage()));
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                showMessage(String.format("Search failed: %s", t.getMessage()));
            }
        });
    }

    private void showMessage(String text) {
        new Toast(text).show();
    }
}
