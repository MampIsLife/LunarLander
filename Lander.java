import java.awt.*;
import java.awt.geom.*;
import java.lang.Object;
import java.awt.geom.Path2D.Double;

public class Lander{

    private double horizontal_momentum, vertical_momentum, rotation, scale;
    private Point2D lander_center, left_leg_tip, right_leg_tip;
    private Shape lander_shape, flame_shape;
    private boolean thrusting, zoomed;
    private double thrust_amount; //used to animate flame

    public Lander(double x_pos, double y_pos){ //lander is created around a given center
        thrust_amount = 0.1;
        thrusting = false;
        zoomed = false;
        horizontal_momentum = 10;
        vertical_momentum = 0;
        rotation = 0;
        scale = 1;
        lander_center = new Point2D.Double(x_pos, y_pos);
        lander_shape = initializeLanderShape(lander_center); //also initializes the lander leg tips
        flame_shape = initializeThrusterShape(lander_center);
    }

    //rotate method:
    public Shape getRotatedShape(Shape _lander_shape, double rotation){
        AffineTransform at = new AffineTransform();
        Rectangle bounds = _lander_shape.getBounds();
        at.rotate(Math.toRadians(rotation), bounds.getX() + bounds.width/2, bounds.getY() + bounds.height/2);
        Shape new_shape = at.createTransformedShape(_lander_shape);
        return new_shape;
    }
    //same method but rotates around given center
    public Shape getRotatedShape(Shape shape, double rotation, Point2D center){
        AffineTransform at = new AffineTransform();
        Rectangle bounds = shape.getBounds();
        at.rotate(Math.toRadians(rotation), center.getX(), center.getY());
        Shape new_shape = at.createTransformedShape(shape);
        return new_shape;
    }

    //method to create the initial lander shape based on given center:
    public Shape initializeLanderShape(Point2D center){
        double cx = (double) center.getX();
        double cy = (double) center.getY();
        Path2D lander = new Path2D.Double();
        //if(zoomed){ scale = 4.5;} //scale up if the lander is landing - TAKEN OUT
        scale=1;
        //body rect:
        lander.moveTo(cx-6*scale,cy-3*scale);
        lander.lineTo(cx+6*scale,cy-3*scale);
        lander.lineTo(cx+6*scale,cy+3*scale);
        lander.lineTo(cx-6*scale,cy+3*scale);
        lander.lineTo(cx-6*scale,cy-3*scale);
        //Cockpit:
        lander.moveTo(cx-4*scale,cy-4*scale);
        lander.lineTo(cx-6*scale,cy-6*scale);
        lander.lineTo(cx-6*scale,cy-8*scale);
        lander.lineTo(cx-4*scale,cy-10*scale);
        lander.lineTo(cx+4*scale,cy-10*scale);
        lander.lineTo(cx+6*scale,cy-8*scale);
        lander.lineTo(cx+6*scale,cy-6*scale);
        lander.lineTo(cx+4*scale,cy-4*scale);
        //Thruster Rect:
        lander.moveTo(cx-4*scale,cy+4*scale);
        lander.lineTo(cx-4*scale,cy+6*scale);
        lander.lineTo(cx+4*scale,cy+6*scale);
        lander.lineTo(cx+4*scale,cy+4*scale);
        //lander legs:
        lander.moveTo(cx-4*scale,cy+4*scale);
        lander.lineTo(cx-8*scale,cy+10*scale);
        lander.moveTo(cx+4*scale,cy+4*scale);
        lander.lineTo(cx+8*scale,cy+10*scale);
        //set lander leg tips to be used for landing calculations
        left_leg_tip = new Point2D.Double(cx-8*scale,cy+10*scale);
        right_leg_tip = new Point2D.Double(cx+8*scale,cy+10*scale);
        //return finished lander
        return lander;
    }
    //method to create the thruster flame shape based on given lander center:
    public Shape initializeThrusterShape(Point2D _lander_center){
        double cx = (double) _lander_center.getX();
        double cy = (double) _lander_center.getY();
        Path2D thruster_flame = new Path2D.Double();
        //if(zoomed){ scale = 4.5;} //scale up if the lander is landing - TAKEN OUT
        scale = 1; //otherwise leave the lander scaled down
        //if the lander is thrusting, draw the flame:
        if(thrust_amount > 0.1){
            thruster_flame.moveTo(cx-3*scale,cy+6*scale);
            thruster_flame.lineTo(cx,cy+6+(16*thrust_amount)*scale);
            thruster_flame.lineTo(cx+3*scale,cy+6*scale);
        }
        return thruster_flame;
    }
    //get slope between lander leg tips to help check for quality of landing
    public double getLandingLegSlope(){
        //find slope using the tangent of the rotation angle of the lander
        double slope = Math.tan(Math.abs(rotation));
        if(rotation > 0){ slope = -slope; }
        return slope;
    }

    //Getter and Setter methods:
    public double getHorizontalSpeed(){ return horizontal_momentum; }
    public double getVerticalSpeed(){ return vertical_momentum; }
    public double getRotation(){ return rotation; }
    public double getX(){ return lander_center.getX(); }
    public double getY(){ return lander_center.getY(); }
    public Point2D getCenter(){ return lander_center; }
    public void setCenter(Point2D new_center){ lander_center = new_center; }
    public void setShape(Shape new_lander_shape){ lander_shape = new_lander_shape; }
    public Shape getShape(){ return lander_shape; }
    public boolean getThrustBoolean(){ return thrusting; }
    public boolean getZoomBoolean(){ return zoomed; }
    public double getThrustAmount(){ return thrust_amount; }
    public Shape getFlameShape(){ return flame_shape; }
    public void setHorizontalSpeed(double new_speed){ horizontal_momentum = new_speed; }
    public void setVerticalSpeed(double new_speed){ vertical_momentum = new_speed; }
    public void setRotation(double new_rotation){ rotation = new_rotation; }
    public void setThrustBoolean(boolean isThrusting){ thrusting = isThrusting; }
    public void setZoomBoolean(boolean isZoomedIn){ zoomed = isZoomedIn; }
    public void setThrustAmount(double new_amount){ thrust_amount = new_amount; }
    public void setFlameShape(Shape new_shape){ flame_shape = new_shape; }


}
