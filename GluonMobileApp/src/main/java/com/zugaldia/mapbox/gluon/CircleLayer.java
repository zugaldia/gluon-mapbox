package com.zugaldia.mapbox.gluon;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

/*
 * Heavily inspired by: https://github.com/gluonhq/maps/blob/master/samples/mobile/src/main/java/com/gluonhq/maps/samples/mobile/PoiLayer.java
 */
public class CircleLayer extends MapLayer {

    private Color color;

    private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

    public CircleLayer(Color color) {
        this.color = color;
    }

    public void addPoint(MapPoint p) {
        Node icon = new Circle(7, color);
        points.add(new Pair(p, icon));
        this.getChildren().add(icon);
        this.markDirty();
    }

    @Override
    protected void layoutLayer() {
        for (Pair<MapPoint, Node> candidate : points) {
            MapPoint point = candidate.getKey();
            Node icon = candidate.getValue();
            Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
            icon.setVisible(true);
            icon.setTranslateX(mapPoint.getX());
            icon.setTranslateY(mapPoint.getY());
        }
    }
}
