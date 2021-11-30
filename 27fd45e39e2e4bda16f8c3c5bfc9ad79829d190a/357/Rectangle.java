package design.practice.designEx.shapes;


public class Rectangle implements Shape{

    private int height;
    private int width;
    
    public Rectangle(int height, int width) {
        this.height = height;
        this.width = width;
    }

    @Override
    public int area() {
        return height * width;
    }

}
