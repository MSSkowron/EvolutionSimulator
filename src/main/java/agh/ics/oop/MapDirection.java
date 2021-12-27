package agh.ics.oop;

public enum MapDirection {
    NORTH,
    SOUTH,
    WEST,
    EAST,
    NORTHWEST,
    SOUTHWEST,
    NORTHEAST,
    SOUTHEAST;

    public String toString() {
        return switch (this) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST -> "E";
            case WEST -> "W";
            case NORTHWEST -> "NW";
            case NORTHEAST -> "NE";
            case SOUTHWEST -> "SW";
            case SOUTHEAST -> "SE";
        };
    }

    public MapDirection next() {
        return getDirection(NORTHEAST, SOUTHWEST, SOUTHEAST, NORTHWEST, NORTH, WEST, EAST, SOUTH);
    }

    public MapDirection previous() {
        return getDirection(NORTHWEST, SOUTHEAST, NORTHEAST, SOUTHWEST, WEST, SOUTH, NORTH, EAST);
    }

    private MapDirection getDirection(MapDirection northeast, MapDirection southwest, MapDirection southeast, MapDirection northwest, MapDirection north, MapDirection west, MapDirection east, MapDirection south) {
        return switch (this) {
            case NORTH -> northeast;
            case SOUTH -> southwest;
            case EAST -> southeast;
            case WEST -> northwest;
            case NORTHWEST -> north;
            case SOUTHWEST -> west;
            case NORTHEAST -> east;
            case SOUTHEAST -> south;
        };
    }

    public Vector2d toUnitVector() {
        return switch (this) {
            case NORTH -> new Vector2d(0, 1);
            case SOUTH -> new Vector2d(0, -1);
            case EAST -> new Vector2d(1, 0);
            case WEST -> new Vector2d(-1, 0);
            case NORTHWEST -> new Vector2d(-1,1);
            case SOUTHWEST -> new Vector2d(-1,-1);
            case NORTHEAST -> new Vector2d(1,1);
            case SOUTHEAST -> new Vector2d(1,-1);
        };
    }
}

