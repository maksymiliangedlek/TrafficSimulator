package pl.gedlek.dto;

public record AddCarRequestDto(int startX, int startY,int targetX,int targetY) {
    public int getstartX() {
        return startX;
    }
    public int getstartY() {
        return startY;
    }
    public int gettargetX() {
        return targetX;
    }
    public int gettargetY() {
        return targetY;
    }
}