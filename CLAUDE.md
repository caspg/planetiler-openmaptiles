# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Planetiler OpenMapTiles Profile for Velomapa, a Java-based implementation of the OpenMapTiles schema using the Planetiler tool. It processes OpenStreetMap data and other geographic datasets to generate vector map tiles in MBTiles format.

## Build System & Requirements

- **Java Version**: Requires Java 21+ (enforced by Maven)
- **Build Tool**: Maven with wrapper scripts (`./mvnw` on Unix, `mvnw.cmd` on Windows)
- **Main Class**: `org.openmaptiles.OpenMapTilesMain`

## Common Development Commands

### Building

```bash
# Full build with tests
./mvnw clean package

# Build without tests (faster)
./mvnw clean package -Dmaven.test.skip

# Quick build script
./scripts/build.sh
```

### Testing

```bash
# Run all tests
./mvnw test

# Run Monaco verification (integration test)
./scripts/check-monaco.sh

# Check documentation links
./scripts/check-doc-links.sh
```

### Code Formatting

```bash
# Apply code formatting (Spotless plugin with Eclipse formatter)
./mvnw spotless:apply

# Or use the script
./scripts/format.sh

# Check formatting
./mvnw spotless:check
```

### Running Map Generation

```bash
# Local testing (Pomorskie region)
bash ./run_pomorskie.sh

# Full Europe generation (requires significant resources)
./run_europe.sh
```

## Architecture

### Core Components

- **OpenMapTilesMain**: Entry point that configures Planetiler with data sources and output
- **OpenMapTilesProfile**: Main profile class that delegates processing to individual layer implementations
- **Generated Package**: Auto-generated code from OpenMapTiles schema
  - `OpenMapTilesSchema.java`: Layer interfaces with constants and allowed values
  - `Tables.java`: Records for imposm3 table mappings with tag filtering

### Layer System

Layers are implemented in `src/main/java/org/openmaptiles/layers/` and extend these interfaces:
- `LakeCenterlineProcessor`: Process lake centerline shapefile data
- `NaturalEarthProcessor`: Process Natural Earth vector data  
- `OsmWaterPolygonProcessor`: Process water polygon shapefiles
- `OsmAllProcessor`: Process every OSM feature
- `OsmRelationPreprocessor`: First-pass OSM relation processing
- `Tables.RowHandler`: Process filtered/parsed imposm3 table rows
- `FinishHandler`: Notifications when input source processing completes
- `LayerPostProcessor`: Post-process features before tile rendering

### Data Sources

The system processes multiple data sources:
1. **OSM Data**: Primary OpenStreetMap data (`.osm.pbf` files)
2. **Lake Centerlines**: Shapefile for lake centerline features
3. **Water Polygons**: Shapefile for water polygon features  
4. **Natural Earth**: Vector data for natural features

### Key Layers

- Transportation & TransportationName: Roads, railways, ferry routes
- Water & WaterName: Water bodies and their labels
- Building: Building footprints and attributes
- Poi: Points of interest
- Place: City/town labels and administrative boundaries
- Landuse/Landcover: Land use and land cover polygons

## Code Generation

Some code is auto-generated from OpenMapTiles schema:

```bash
# Regenerate from OpenMapTiles schema (specify version tag)
./scripts/regenerate-openmaptiles.sh v3.15

# With custom repository URL
./scripts/regenerate-openmaptiles.sh v3.15 https://raw.githubusercontent.com/openmaptiles/openmaptiles/
```

## Customization

### Adding Custom Layers

1. Create new class implementing `Layer` interface in `src/main/java/org/openmaptiles/addons/`
2. Implement appropriate processor interfaces (e.g., `OsmAllProcessor`)
3. Register in `ExtraLayers.java`

### Layer Exclusion/Inclusion

Use command-line arguments:
- `--exclude-layers=poi,housenumber,...`
- `--only-layers=water,transportation,...`

## Configuration Files

- `pom.xml`: Maven build configuration with dependencies and plugins
- `eclipse-formatter.xml`: Eclipse code formatting rules used by Spotless plugin
- Shell scripts in `scripts/`: Various build and maintenance tasks
- `run_*.sh`: Example map generation configurations

## Memory and Performance

- Local testing typically uses 4GB heap (`JAVA_TOOL_OPTIONS="-Xmx4g"`)
- Europe generation requires 32GB+ heap
- Uses efficient storage options: `--nodemap-type=array --storage=mmap`
- Supports multi-threading for download and processing

## Docker Support

Docker images can be built using Jib Maven plugin:

```bash
# Build Docker image
./mvnw jib:dockerBuild

# Multi-architecture build
./mvnw jib:build -Pjib-multi-arch
```

