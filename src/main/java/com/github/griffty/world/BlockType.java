package com.github.griffty.world;

public enum BlockType {
    Air(' '),
    Wall('█'),
    Floor('░'),
    Hatch('⬢');

    private final char symbol;

    BlockType(char symbol) {
        this.symbol = symbol;
    }

    public static char getSymbol(byte index) {
        return  BlockType.values()[index].symbol;
    }
}
