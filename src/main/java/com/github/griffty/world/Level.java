package com.github.griffty.world;

import com.github.griffty.Player;
import com.github.griffty.TerminalController;
import com.github.griffty.util.Vector2;
import lombok.Getter;

@Getter
public class Level {
    private final Player player;
    private final Vector2 size;
    private final byte[][] tiles;
    private final Vector2 center;

    public Level(Vector2 size) {
        //todo: fix later
        //Do I need center pixel or center in between pixels?
        //I need to choose one or otherwise rounding error affects wall detection
        if (size.getX() % 2 == 1 || size.getY() % 2 == 1) {
            throw new IllegalArgumentException("Level size must be even in both dimensions to have a center");
        }
        this.size = size;
        this.tiles = new byte[size.getY()][size.getX()];
        this.center = new Vector2(size.getX() / 2, size.getY() / 2);
        player = new Player();
    }

    public Level(Vector2 size, byte[][] tiles) {
        if (size.getX() % 2 == 1 || size.getY() % 2 == 1) {
            throw new IllegalArgumentException("Level size must be even in both dimensions to have a center");
        }
        if (tiles.length != size.getY() || tiles[0].length != size.getX()) {
            throw new IllegalArgumentException("Tiles array dimensions do not match specified size");
        }
        this.size = size;
        this.tiles = tiles;
        this.center = new Vector2(size.getX() / 2, size.getY() / 2);
        player = new Player();
    }


    public char[][] project(){
        Vector2 ts = TerminalController.get().getTerminalSize();
        Vector2 frameCenter = new Vector2(ts.getX() / 2, ts.getY() / 2); //possible problem with odd terminal sizes
        Vector2 playerPos = player.getPosition();
        char[][] projected = new char[ts.getY()][ts.getX()];
        Vector2 offset = frameCenter.sub(center).add(playerPos);
        
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
                projected[y][x] = BlockType.getSymbol(tiles[y - offset.getY()][x - offset.getX()]);
            }
        }

        projected[frameCenter.getY()][frameCenter.getX()] = '@'; //player marker

        return  projected;
    }

    public void move(Vector2 delta) {
        Vector2 newPos = player.getPosition().add(delta);

        if (tiles[newPos.getY() + center.getY() -1][newPos.getX() + center.getX() -1] == BlockType.Wall.ordinal()) {
            return;
        }
        player.move(delta);
    }
}
