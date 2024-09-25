package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import java.util.List;
import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;

/**
 * Based on:
 * https://github.com/onthegomap/planetiler/blob/main/planetiler-examples/src/main/java/com/onthegomap/planetiler/examples/BikeRouteOverlay.java
 */
public class BicycleRouteLayer implements
  Layer,
  ForwardingProfile.OsmRelationPreprocessor,
  OpenMapTilesProfile.OsmAllProcessor,
  ForwardingProfile.LayerPostProcesser
 {
  private static final String LAYER_NAME = "bicycle_route";

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // Minimal container for data we extract from OSM bicycle route relations. This is held in RAM so keep it small.
  private record RouteRelationInfo(
    // OSM ID of the relation (required):
    @Override long id,
    // Values for tags extracted from the OSM relation:
    String name,
    String ref,
    String route,
    String network,
    String wikipedia,
    String website
  ) implements OsmRelationInfo {}

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    // If this is a "route" relation ...
    if (relation.hasTag("type", "route", "superroute")) {
      // where route=bicycle or route=mtb ...
      if (relation.hasTag("route", "mtb", "bicycle")) {
        // then store a RouteRelationInfo instance with tags we'll need later
        return List.of(new RouteRelationInfo(
          relation.id(),
          relation.getString("name"),
          relation.getString("ref"),
          relation.getString("route"),
          // except map network abbreviation to a human-readable value
          switch (relation.getString("network", "")) {
            case "icn" -> "international";
            case "ncn" -> "national";
            case "rcn" -> "regional";
            case "lcn" -> "local";
            default -> "other";
          },
          relation.getString("wikipedia"),
          relation.getString("website")
        ));
      }
    }

    // for any other relation, return null to ignore
    return null;
  }

  @Override
  public void processAllOsm(SourceFeature feature, FeatureCollector features) {
    List<OsmReader.RelationMember<RouteRelationInfo>> routeInfos = feature.relationInfo(RouteRelationInfo.class);

    if (routeInfos == null || routeInfos.isEmpty() || !feature.canBeLine()) {
      return;
    }

    for (var routeInfo : routeInfos) {
      RouteRelationInfo relation = routeInfo.relation();

      // min zoom level based on network type
      int minZoom = switch (relation.network()) {
        case "international" -> 1;
        case "national" -> 2;
        case "regional" -> 3;
        case "local" -> 4;
        default -> 4;
      };

      features.line(LAYER_NAME)
      .setAttr("name", relation.name)
      .setAttr("ref", relation.ref)
      .setAttr("network", relation.network)
      .setAttr("wikipedia", relation.wikipedia)
      .setAttr("website", relation.website)
      .setZoomRange(minZoom, 14)
      // don't filter out short line segments even at low zooms because the next step needs them
      // to merge lines with the same tags where the endpoints are touching
      .setMinPixelSize(0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
        // FeatureMerge has several utilities for merging geometries in a layer that share the same tags.
    // `mergeLineStrings` combines lines with the same tags where the endpoints touch.
    // Tiles are 256x256 pixels and all FeatureMerge operations work in tile pixel coordinates.
    return FeatureMerge.mergeLineStrings(items,
      0.5, // after merging, remove lines that are still less than 0.5px long
      0.1, // simplify output linestrings using a 0.1px tolerance
      4 // remove any detail more than 4px outside the tile boundary
    );
  }
}
