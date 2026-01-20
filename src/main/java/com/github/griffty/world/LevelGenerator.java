package com.github.griffty.world;

import com.github.griffty.util.DebugTools;
import com.github.griffty.util.Vector2;
import lombok.Data;

import java.util.*;

public class LevelGenerator {
    private static final Vector2 MAX_ROOM_SIZE = new Vector2(80, 40);
    private static final Vector2 MIN_ROOM_SIZE = new Vector2(40, 20);
    private static final int CORRIDOR_WIDTH = 1;

    private static final Random random = new Random();

    public static Level generateLevel(Vector2 size) {
        if (size.getX() % 2 == 0) size = size.addX(1);
        if (size.getY() % 2 == 0) size = size.addY(1);
        byte[][] tiles = new byte[size.getY()][size.getX()];

        List<Room> rooms = partitionLevel(size);
        List<Corridor> corridors = connectRooms(rooms);

        for (Room room : rooms) {
            generateRoom(tiles, room.getTopRight(), room.getBottomLeft());
        }

        for (Corridor corridor : corridors) {
            generateCorridor(tiles, corridor);
        }

        Vector2 spawnPoint = findSpawnPoint(rooms, size);
        Vector2 exit = findExit(rooms, size);
        tiles[exit.getY()][exit.getX()] = (byte) BlockType.Hatch.ordinal();
        return new Level(size, spawnPoint, tiles);
    }

    private static Vector2 findExit(List<Room> rooms, Vector2 size) {
        Vector2 worldCenter = size.scale(0.5f);
        List<Room> farRooms = new ArrayList<>();
        for (Room room : rooms) {
            float dist = room.getCenter().dist(worldCenter.scale(0.5f));
            if (dist > worldCenter.getX() && dist > worldCenter.getY()) {
                farRooms.add(room);
            }
        }
        Room exitRoom = farRooms.get(random.nextInt(farRooms.size()));
        Vector2 exitPoint = randomPointInside(exitRoom);
        DebugTools.log("Exit point set to: " + (exitPoint.sub(worldCenter)));
        return exitPoint;
    }

    private static Vector2 findSpawnPoint(List<Room> rooms, Vector2 size) {
        Vector2 spawnPoint = new Vector2();
        Vector2 worldCenter = size.scale(0.5f);
        float distToCenter = spawnPoint.dist(worldCenter);
        for (Room room : rooms) {
            if (room.getCenter().dist(worldCenter) < distToCenter) {
                spawnPoint = room.getCenter();
                distToCenter = spawnPoint.dist(worldCenter);
            }
        }
        spawnPoint = spawnPoint.sub(worldCenter);
        DebugTools.log("Spawn point set to: " + (spawnPoint));
        return spawnPoint;
    }

    private static void generateCorridor(byte[][] tiles, Corridor corridor) {
        int h = tiles.length;
        int w = tiles[0].length;

        for (Vector2 p : corridor.getPath()) {
            for (int yy = p.getY() - LevelGenerator.CORRIDOR_WIDTH; yy <= p.getY() + LevelGenerator.CORRIDOR_WIDTH; yy++) {
                for (int xx = p.getX() - LevelGenerator.CORRIDOR_WIDTH; xx <= p.getX() + LevelGenerator.CORRIDOR_WIDTH; xx++) {
                    if (xx <= 0 || yy <= 0 || xx >= w - 1 || yy >= h - 1) continue;

                    tiles[yy][xx] = (byte) BlockType.Floor.ordinal();

                    for (int wy = yy - 1; wy <= yy + 1; wy++) {
                        for (int wx = xx - 1; wx <= xx + 1; wx++) {
                            if (wx <= 0 || wy <= 0 || wx >= w - 1 || wy >= h - 1) continue;
                            if (tiles[wy][wx] == 0) {
                                tiles[wy][wx] = (byte) BlockType.Wall.ordinal();
                            }
                        }
                    }
                }
            }
        }
    }


    private static List<Corridor> connectRooms(List<Room> rooms) {
        if (rooms == null || rooms.size() < 2) return List.of();

        int n = rooms.size();
        Vector2[] centers = new Vector2[n];
        for (int i = 0; i < n; i++) centers[i] = rooms.get(i).getCenter();

        boolean[] inTree = new boolean[n];
        float[] bestDist = new float[n];
        int[] bestFrom = new int[n];

        for (int i = 0; i < n; i++) {
            bestDist[i] = Float.POSITIVE_INFINITY;
            bestFrom[i] = -1;
        }

        int start = random.nextInt(n);
        bestDist[start] = 0;

        List<int[]> edges = new LinkedList<>();

        for (int iter = 0; iter < n; iter++) {
            int v = -1;
            float vBest = Float.POSITIVE_INFINITY;

            for (int i = 0; i < n; i++) {
                if (!inTree[i] && bestDist[i] < vBest) {
                    vBest = bestDist[i];
                    v = i;
                }
            }

            if (v == -1) break;
            inTree[v] = true;

            if (bestFrom[v] != -1) {
                edges.add(new int[]{bestFrom[v], v});
            }

            for (int u = 0; u < n; u++) {
                if (inTree[u]) continue;
                float d = dist2(centers[v], centers[u]);
                if (d < bestDist[u]) {
                    bestDist[u] = d;
                    bestFrom[u] = v;
                }
            }
        }

        int extra = Math.max(0, n / 6);
        for (int k = 0; k < extra; k++) {
            int a = random.nextInt(n);
            int b = nearestNeighborIndex(centers, a);
            if (a != b) edges.add(new int[]{a, b});
        }

        List<Corridor> corridors = new LinkedList<>();
        for (int[] e : edges) {
            int a = e[0], b = e[1];

            Vector2 p1 = randomPointInside(rooms.get(a));
            Vector2 p2 = randomPointInside(rooms.get(b));

            corridors.add(new Corridor(buildLCorridorPath(p1, p2)));
        }

        return corridors;
    }

    private static float dist2(Vector2 a, Vector2 b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        return dx * dx + dy * dy;
    }

    private static int nearestNeighborIndex(Vector2[] centers, int a) {
        int n = centers.length;
        int[] candidates = new int[Math.min(6, n - 1)];
        float[] candDist = new float[candidates.length];

        for (int i = 0; i < candidates.length; i++) {
            candidates[i] = -1;
            candDist[i] = Float.POSITIVE_INFINITY;
        }

        for (int b = 0; b < n; b++) {
            if (b == a) continue;
            float d = dist2(centers[a], centers[b]);

            for (int i = 0; i < candidates.length; i++) {
                if (d < candDist[i]) {
                    for (int j = candidates.length - 1; j > i; j--) {
                        candidates[j] = candidates[j - 1];
                        candDist[j] = candDist[j - 1];
                    }
                    candidates[i] = b;
                    candDist[i] = d;
                    break;
                }
            }
        }

        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] != -1) {
                int pick = random.nextInt(i + 1);
                return candidates[pick];
            }
        }
        return (a + 1) % n;
    }

    private static Vector2 randomPointInside(Room r) {
        int x0 = r.getTopRight().getX() + 1;
        int x1 = r.getBottomLeft().getX() - 2;
        int y0 = r.getTopRight().getY() + 1;
        int y1 = r.getBottomLeft().getY() - 2;

        if (x1 < x0) { x0 = r.getTopRight().getX(); x1 = r.getBottomLeft().getX() - 1; }
        if (y1 < y0) { y0 = r.getTopRight().getY(); y1 = r.getBottomLeft().getY() - 1; }

        int x = x0 + random.nextInt(Math.max(1, (x1 - x0 + 1)));
        int y = y0 + random.nextInt(Math.max(1, (y1 - y0 + 1)));
        return new Vector2(x, y);
    }

    private static List<Vector2> buildLCorridorPath(Vector2 a, Vector2 b) {
        boolean horizFirst = random.nextBoolean();
        Vector2 corner = horizFirst ? new Vector2(b.getX(), a.getY())
                : new Vector2(a.getX(), b.getY());

        List<Vector2> path = new LinkedList<>();
        addLine(path, a, corner);
        addLine(path, corner, b);
        return path;
    }

    private static void addLine(List<Vector2> path, Vector2 from, Vector2 to) {
        int x = from.getX();
        int y = from.getY();

        int dx = Integer.compare(to.getX(), x);
        int dy = Integer.compare(to.getY(), y);

        while (x != to.getX()) {
            path.add(new Vector2(x, y));
            x += dx;
        }
        while (y != to.getY()) {
            path.add(new Vector2(x, y));
            y += dy;
        }
        path.add(new Vector2(x, y));
    }

    private static List<Room> partitionLevel(Vector2 size) {
        Queue<Room> queue = new LinkedList<>();
        List<Room> rooms = new LinkedList<>();
        queue.add(new Room(new Vector2(0, 0), size));
        while (!queue.isEmpty()) {
            Room r = queue.poll();
            Vector2 roomSize = r.getBottomLeft().sub(r.getTopRight());
            if (roomSize.smaller(MAX_ROOM_SIZE)) {
                rooms.add(r);
                continue;
            }

            boolean splitHorizontally = roomSize.getX() < roomSize.getY() * 2;
            if (splitHorizontally) {
                int topY = r.getTopRight().getY();
                int bottomY = r.getBottomLeft().getY();
                int height = bottomY - topY;
                int newHeight;
                do {
                    newHeight = (int)(height * (random.nextFloat() * 0.5f + 0.25f));
                }while (newHeight < MIN_ROOM_SIZE.getY());
                int splitY = topY + newHeight;
                queue.add(new Room(r.getTopRight(), new Vector2(r.getBottomLeft().getX(), splitY)));
                queue.add(new Room(new Vector2(r.getTopRight().getX(), splitY), r.getBottomLeft()));
            } else {
                int leftX = r.getTopRight().getX();
                int rightX = r.getBottomLeft().getX();
                int width = rightX - leftX;
                int newWidth;
                do {
                    newWidth = (int)(width * (random.nextFloat() * 0.5f + 0.25f));
                }while (newWidth < MIN_ROOM_SIZE.getX());
                int splitX = leftX + (int)(width * (random.nextFloat() * 0.5f + 0.25f));
                queue.add(new Room(r.getTopRight(), new Vector2(splitX, r.getBottomLeft().getY())));
                queue.add(new Room(new Vector2(splitX, r.getTopRight().getY()), r.getBottomLeft()));
            }
        }

        for (Room room : rooms) {
            room.scale(0.5f);
        }

        DebugTools.log("Generated " + rooms.size() + " rooms.");
        return rooms;
    }

    public static void generateRoom(byte[][] tiles, Vector2 topRight, Vector2 bottomLeft) {
        for (int i = topRight.getX(); i < bottomLeft.getX(); i++) {
            for (int j = topRight.getY(); j < bottomLeft.getY(); j++) {
                if (i == topRight.getX() || i == bottomLeft.getX() -1 || j == topRight.getY() || j == bottomLeft.getY()-1){
                    tiles[j][i] = (byte) BlockType.Wall.ordinal();
                    continue;
                }
                tiles[j][i] = (byte) BlockType.Floor.ordinal();
            }
        }
    }

    @Data
    private static class Room {
        private Vector2 topRight;
        private Vector2 bottomLeft;
        private Vector2 center;

        public Room(Vector2 topRight, Vector2 bottomLeft) {
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.center = topRight.add(bottomLeft).scale(0.5f);
        }

        public void scale(float scale){
            topRight = topRight.sub(center).scale(scale).add(center);
            bottomLeft = bottomLeft.sub(center).scale(scale).add(center);
        }
    }

    @Data
    private static class Corridor {
        private List<Vector2> path;
        private int width;
        public Corridor(List<Vector2> path) {
            this.path = path;
        }
    }
}
