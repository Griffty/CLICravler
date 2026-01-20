package com.github.griffty.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public final class Vector2 {
    private final int x;
    private final int y;

    public Vector2() {
        this.x = 0;
        this.y = 0;
    }
    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 addX(int deltaX) {
        return new Vector2(this.x + deltaX, this.y);
    }

    public Vector2 addY(int deltaY) {
        return new Vector2(this.x, this.y + deltaY);
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public  Vector2 sub(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 scale(float scalar) {
        return new Vector2((int)(this.x * scalar), (int)(this.y * scalar));
    }
    public Vector2 clamp(int minX, int minY, int maxX, int maxY) {
        int clampedX = Math.max(minX, Math.min(this.x, maxX));
        int clampedY = Math.max(minY, Math.min(this.y, maxY));
        return new Vector2(clampedX, clampedY);
    }

    public boolean bigger(Vector2 v) {
        return getX() > v.getX() && getY() > v.getY();
    }

    public  boolean smaller(Vector2 v) {
        return getX() < v.getX() && getY() < v.getY();
    }

    public float dist(Vector2 worldCenter) {
        int dx = this.x - worldCenter.x;
        int dy = this.y - worldCenter.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}