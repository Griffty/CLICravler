package com.github.griffty.world;

import lombok.Getter;

@Getter
public enum BlockType {
    Air(' '),
    Wall('█'),
    Floor('▒'),
    Hatch('⬢'),
    Player('@'),
    Fog('░');

    private final char symbol;

    BlockType(char symbol) {
        this.symbol = symbol;
    }

    public static char getSymbol(byte index) {
        return  BlockType.values()[index].symbol;
    }

}
