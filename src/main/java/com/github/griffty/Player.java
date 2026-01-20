package com.github.griffty;

import com.github.griffty.util.DebugTools;
import com.github.griffty.util.Vector2;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Player {
    private Vector2 position;
    public Player() {
        this.position = new Vector2();
    }

    public void move(Vector2 movement) {
        this.position = this.position.add(movement);
        DebugTools.log("Moved: " + movement + " Current Position: " + this.position);
    }
}
