import java.awt.*;
import java.awt.geom.*;
import java.lang.Object;
import java.awt.geom.Path2D.Double;

public class LandingZone{

    private Line2D zone_shape;
    private Point2D left_endpoint, right_endpoint;
    private int bonus; //How much the zone is worth


    //Landing zone is created based on a point and the bonus value of the zone
    public LandingZone(Point2D given_point, int _bonus){
        //ensures valid bonus
        if(!isValidBonus(_bonus)){
            System.out.println("INVALID BONUS \n EXITING...");
            System.exit(0);
        }
        bonus = _bonus;
        left_endpoint = given_point;

        //Length determined by the bonus value
        double length = 90/bonus;

        //Right endpoint determined by the left endpoint and length of the zone
        //It is always straight horizontal
        right_endpoint = new Point2D.Double(left_endpoint.getX()+length, left_endpoint.getY()+length);

        //Use the endpoints to build the zone itself
        zone_shape = new Line2D.Double(left_endpoint, right_endpoint);
    }

    //Simple method that check to ensure an entered bonus is valid
    public boolean isValidBonus(int _bonus){
        boolean answer = false;
        if(_bonus == 2 || _bonus == 3 || _bonus == 5){ answer = true; }
        return answer;
    }

    //Getters and Setters for LandingZone
    public Point2D getLeftEndpoint(){ return left_endpoint; }
    public Point2D getRightEndpoint(){ return right_endpoint; }
    public int getBonus(){ return bonus; }
    public Line2D getShape(){ return zone_shape; }
    public void setLeftEndpoint(Point2D new_endpoint){ left_endpoint = new_endpoint; }
    public void setRightEndpoint(Point2D new_endpoint){ right_endpoint = new_endpoint; }
    public void setBonus(int new_bonus){ bonus = new_bonus; }
    public void setShape(Line2D new_zone_shape){ zone_shape = new_zone_shape; }

}
