package com.github.griffty.world;

import com.github.griffty.Main;
import com.github.griffty.Player;
import com.github.griffty.TerminalController;
import com.github.griffty.util.Vector2;
import lombok.Getter;

@Getter
public class Level {
    private final Player player;
    private final Vector2 size;
    private final Vector2 center;

    private final byte[][] tiles;
    private final boolean[][] visibilityMap;

    private final int visibilityRadius = 8;
    public Level(Vector2 size, Vector2 spawn, byte[][] tiles) {
        //todo: fix later
        //Do I need center pixel or center in between pixels?
        //I need to choose one or otherwise rounding error affects wall detection
        if (size.getX() % 2 == 0 || size.getY() % 2 == 0) {
            throw new IllegalArgumentException("Level size must be odd in both dimensions to have a center");
        }
        if (tiles.length != size.getY() || tiles[0].length != size.getX()) {
            throw new IllegalArgumentException("Tiles array dimensions do not match specified size");
        }
        this.size = size;
        this.tiles = tiles;
        this.center = new Vector2(size.getX() / 2, size.getY() / 2);
        player = new Player(spawn);
        visibilityMap = new boolean[size.getY()][size.getX()];

        for (int i = center.getY() + player.getPosition().getY() - visibilityRadius; i <= center.getY() + player.getPosition().getY() + visibilityRadius; i++) {
            for (int j = center.getX() + player.getPosition().getX() - visibilityRadius * 2; j <= center.getX() + player.getPosition().getX() + visibilityRadius * 2; j++) {
                if (i >= 0 && i < size.getY() && j >= 0 && j < size.getX()) {
                    visibilityMap[i][j] = true;
                }
            }
        }
    }


    public char[][] project(){
        Vector2 ts = TerminalController.get().getTerminalSize();
        Vector2 frameCenter = new Vector2(ts.getX() / 2, ts.getY() / 2); //possible problem with odd terminal sizes
        Vector2 playerPos = player.getPosition();
        char[][] projected = new char[ts.getY()][ts.getX()];
        Vector2 offset = frameCenter.sub(center).sub(playerPos);
        
        //reverse logic to render from level (maybe more efficient)
        for (int y = 0; y < ts.getY(); y++) {
            for (int x = 0; x < ts.getX(); x++) {
                if (y - offset.getY() < 0 || x - offset.getX() < 0) {
                    projected[y][x] = ' ';
                    continue;
                }
                if (y - offset.getY() >= size.getY() || x - offset.getX() >= size.getX()) {
                    projected[y][x] = ' ';
                    continue;
                }
                if (!visibilityMap[y - offset.getY()][x - offset.getX()]) {
                    projected[y][x] = BlockType.Fog.getSymbol();
                    continue;
                }
                projected[y][x] = BlockType.getSymbol(tiles[y - offset.getY()][x - offset.getX()]);
            }
        }

        projected[frameCenter.getY()][frameCenter.getX()] = BlockType.Player.getSymbol();

        return  projected;
    }

    public void move(Vector2 delta) {
        Vector2 newPos = player.getPosition().add(delta).add(center);
        if (newPos.getX() < 0 || newPos.getX() >= size.getX()) {
            return;
        }
        if (newPos.getY() < 0 || newPos.getY() >= size.getY()) {
            return;
        }
        byte tile = tiles[newPos.getY()][newPos.getX()];
        if (tile == BlockType.Wall.ordinal()) {
            return;
        }
        if (tile == BlockType.Hatch.ordinal()) {
            Main.finishLevel();
        }
        player.move(delta);

        for (int i = center.getY() + player.getPosition().getY() - visibilityRadius; i <= center.getY() + player.getPosition().getY() + visibilityRadius; i++) {
            for (int j = center.getX() + player.getPosition().getX() - visibilityRadius * 2; j <= center.getX() + player.getPosition().getX() + visibilityRadius * 2; j++) {
                if (i >= 0 && i < size.getY() && j >= 0 && j < size.getX()) {
                    visibilityMap[i][j] = true;
                }
            }
        }
    }
}
