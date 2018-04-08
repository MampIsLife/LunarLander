import java.awt.*;
import java.awt.geom.*;
import java.lang.*;
import java.lang.Double;
import java.lang.Object;
import java.awt.geom.Path2D.*;

public class LandPiece{

    private Point2D left_endpoint;
    private Point2D right_endpoint;
    private Line2D piece_shape;
    private boolean isLandingWorthy;

    public LandPiece(Point2D _left_endpoint, Point2D _right_endpoint){
        //ensures LandPiece continues to build the land toward the right end of the screen
        if(_right_endpoint.getX() < _left_endpoint.getX()){
            System.out.println("INVALID LAND PIECE: PIECE BACKWARDS \n EXITING...");
            System.exit(0);
        }
        left_endpoint = _left_endpoint;
        right_endpoint = _right_endpoint;
        piece_shape = new Line2D.Double(left_endpoint, right_endpoint);
    }

    //checks if the given lander shape intersects this LandPiece
    public boolean landIntersection(Path2D path) {
        double x1 = -1 ,y1 = -1 , x2 = -1, y2 = -1; //initialize points
        //iterate through path, loop through points found
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next())
        {
            double[] coordinates = new double[6];
            switch (pi.currentSegment(coordinates))
            {
                case PathIterator.SEG_MOVETO:
                // nothing needed here
                case PathIterator.SEG_LINETO:
                {
                    if(x1 == -1 && y1 == -1 )
                    {
                        //create first point from iterator
                        x1= coordinates[0];
                        y1= coordinates[1];
                        break;
                    }
                    if(x2 == -1 && y2 == -1)
                    {
                        //create second point from iterator
                        x2= coordinates[0];
                        y2= coordinates[1];
                        break;
                    }
                    break;
                }
            }
            if(x1 != -1 && y1 != -1 && x2 != -1 && y2 != -1)
            {
                Line2D segment = new Line2D.Double(x1, y1, x2, y2);
                if (segment.intersectsLine(piece_shape))
                {
                    return true;
                }
                //reset points
                x1 = -1;
                y1 = -1;
                x2 = -1;
                y2 = -1;
            }
        }
        return false;
    }

    //check if the lander landed or crashed
    //returns number indicating Landing rating
    // 0_crash   1_good   2_great   3_perfect
    // will return "-1" if the lander has not intersected the land at all
    public int checkLanding(Lander lander){
        Path2D lander_path = (Path2D) lander.getShape();
        if(landIntersection(lander_path)){
            //now check if landed well using both momentums and by calculating the slope of the line created between the lander leg tips and making sure it is at least very close to the slope of the land it is landing on
            double lander_x_speed = lander.getHorizontalSpeed();
            double lander_y_speed = lander.getVerticalSpeed();
            int momentum_check = 0;
            //perfect landing momentum
            if(lander_x_speed == 0 && Math.abs(lander_y_speed) < 4){ momentum_check = 3; }
            //great landing momentum
            else if(Math.abs(lander_x_speed) <= 1 && Math.abs(lander_y_speed) < 6){ momentum_check = 2; }
            //good landing momentum
            else if(Math.abs(lander_x_speed) <= 2 && Math.abs(lander_y_speed) < 10){ momentum_check = 1; }
            //if lander failed to land, crash
            else{ momentum_check = 0; }
            double lander_slope = lander.getLandingLegSlope();
            double land_slope = getSlope();
            int slope_check = Double.compare(lander_slope, land_slope);
            System.out.println("LANDER SLOPE: "+lander_slope);
            System.out.println("LAND SLOPE: "+land_slope);
            switch(slope_check){
                case 0: //slopes are the exact same
                    slope_check = 3;
                    break;
                case -1: //land slope is greater
                    if(land_slope - lander_slope < .2){ slope_check = 3; }
                    else if(land_slope - lander_slope < .4){ slope_check = 2; }
                    else if(land_slope - lander_slope < .6){ slope_check = 1; }
                    else{ slope_check = 0; }
                    break;
                case 1: //lander slope is greater
                    if(lander_slope - land_slope < .2){ slope_check = 3; }
                    else if(lander_slope - land_slope < .4){ slope_check = 2; }
                    else if(lander_slope - land_slope < .6){ slope_check = 1; }
                    else{ slope_check = 0; }
                    break;
            }
            int quality_of_landing = 0; // defaults as a crash
            //compare the checks, the landing is only as good as the worst checks
            int check_comparison = Integer.valueOf(momentum_check).compareTo(slope_check);

            switch(check_comparison){
                case 0: //if the checks are the same, the quailty is the same as either
                    quality_of_landing = momentum_check;
                    break;
                case -1: //if the momentum_check is smaller, use that as the quality
                    quality_of_landing = momentum_check;
                    break;
                case 1: //if the slope_check is smaller, use that as the quality
                    quality_of_landing = slope_check;
                    break;
            }
            return quality_of_landing;
        }
        else { return -1;} //no intersection
    }

    public double getSlope(){
        double x1 = left_endpoint.getX();
        double y1 = left_endpoint.getY();
        double x2 = right_endpoint.getX();
        double y2 = right_endpoint.getY();
        double slope = (y2-y1) / (x2-x1);
        return -slope; //return the negative slope because lower valuse on y are treated as bigger
    }

    //if the lander approaches the land closely, this will be used to zoom in on the land based on the lander
    public Shape getZoomedShape(LunarCanvas canvas){
        //left_endpoint
        AffineTransform st = new AffineTransform();
        st.scale(4.5, 4.5); //ajust transform for scaling
        double y_diff = right_endpoint.getY() - left_endpoint.getY();
        Point2D new_left = new Point2D.Double(left_endpoint.getX(), canvas.getHeight()-100);
        Point2D new_right = new Point2D.Double(right_endpoint.getX(), canvas.getHeight()-100+y_diff);
        Line2D ajusted_land = new Line2D.Double(new_left, new_right);
        Shape zoomedShape = st.createTransformedShape(ajusted_land);
        return zoomedShape;
    }

    //returns the x value of the midpoint of this LandPiece
    public double getMidpointX(){ return left_endpoint.getX() + ((right_endpoint.getX() - left_endpoint.getX()) / 2); }
    //returns the y value of the midpoint of this LandPiece
    public double getMidpointY(){
        double smaller_y;
        if(left_endpoint.getY() > right_endpoint.getY()){
            smaller_y = right_endpoint.getY();
        }
        else{
            smaller_y = left_endpoint.getY();
        }
        return smaller_y + ((right_endpoint.getY() - left_endpoint.getY()) / 2);
    }
    //Getters and Setters for LandPiece
    public Point2D getLeftEndpoint(){ return left_endpoint; }
    public Point2D getRightEndpoint(){ return right_endpoint; }
    public Line2D getShape(){ return piece_shape; }
    public void setLeftEndpoint(Point2D new_endpoint){ left_endpoint = new_endpoint; }
    public void setRightEndpoint(Point2D new_endpoint){ right_endpoint = new_endpoint; }
    public void setShape(Line2D new_piece_shape){ piece_shape = new_piece_shape; }


}
