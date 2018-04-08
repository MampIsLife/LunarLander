import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.Area;
import java.awt.geom.*;
import java.lang.Math;
import java.util.*;
import javax.swing.Timer;
import java.awt.image.*;


public class LunarCanvas extends JPanel implements KeyListener, ActionListener
{
    private final int HIEGHT = 700;
    private final int WIDTH = 1200;
    private final int SPEED = 10; // delay between timer firings
    private final int PAUSE = 1000; // pause before timer begins
    private final double THRUST_CONSTANT = 0.5; //how much thrusting influences momentum
    private final double GRAVITY_CONSTANT = 0.3; //how much gravity influences momentum

    private ArrayList<LandPiece> land_pieces;
    private ArrayList<LandingZone> landing_zones;

    private boolean zoomed;
    private int score, fuel, time_min, time_min_10s, time_sec, time_sec_10s, timer;
    private double rotation;
    private Shape current_lander_shape;
    private Lander current_lander;


    public LunarCanvas(){
        timer = 0;
        rotation = 0;
        score = 0;
        fuel = 1000;
        time_min = 0;
        time_min_10s = 0;
        time_sec = 0;
        time_sec_10s = 0;
        zoomed = false;
        land_pieces = new ArrayList<>();
        landing_zones = new ArrayList<>();
        current_lander = new Lander(50,150);
        setPreferredSize(new Dimension(WIDTH,HIEGHT));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);// allow user interaction
        //create the current land, fills land_piece array
        generateLand(50);
        //Timer setup:
        Timer timer = new Timer(SPEED, this);
        timer.setInitialDelay(PAUSE);
        timer.start();
    }

    public void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g; //cast so we can use JAVA2D.
        super.paintComponent(g);  //without this no background color set.
        BufferedImage game_screen = createGameBufferedImage();
        //draw the screen
        g2d.drawImage(game_screen,0,0,null);

    }

    public BufferedImage createGameBufferedImage(){
        BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();

        //set color to white for lander and game information
        g2.setPaint(Color.white);
        //draw the lander
        g2.draw(current_lander.getShape());
        //if thrusting, animate flame growing:
        if(current_lander.getThrustBoolean() && current_lander.getThrustAmount() < 1){
            current_lander.setThrustAmount(current_lander.getThrustAmount() + 0.1);
        }
        //if thrusting, animate flame receeding:
        else if(!current_lander.getThrustBoolean() && current_lander.getThrustAmount() > 0){
            current_lander.setThrustAmount(current_lander.getThrustAmount() - 0.1);
        }
        //draw the flame:
        g2.draw(current_lander.getFlameShape());
        //draw the land
        for(LandPiece l: land_pieces){ g2.draw(l.getShape()); } //draw land pieces
        if(zoomed){

            //find the closest land piece
            LandPiece closest_piece = checkClosestPiece();
            //check for intersection/landing on closest piece of land
            int landing = closest_piece.checkLanding(current_lander);
            //if crashed:
            if(landing == 0){

            }

            //take lander_center values as integers:
            int lander_x = (int) current_lander.getX();
            int lander_y = (int) current_lander.getY();
            //width and hieght of new subimage
            int sub_width = WIDTH/2;
            int sub_height = HIEGHT/2;
            //ajust to frame lander in top left corner
            BufferedImage sub_bi = bi.getSubimage( lander_x-sub_width/4, lander_y-sub_width/4, sub_width, sub_height);
            int w = bi.getWidth();
            int h = bi.getHeight();
            //create another BufferedImage to use for scaling
            BufferedImage scaled_sub_bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(2.0, 2.0);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            scaled_sub_bi = scaleOp.filter(sub_bi, scaled_sub_bi);

            Graphics2D g3 = scaled_sub_bi.createGraphics();
            //draw the various game information into new subimage
            paintInfo(g3);
            return scaled_sub_bi;
        }
        else{
            //draw the various game information
            paintInfo(g2);
            return bi;
        }
    }

    public void generateLand(int numberOfLandPieces){
        //procedurally generate land pieces based on the last land piece made:
        //  -land cannot start or reach beyond certain heights
        //  -land cannot create an angle with the previous piece greater than 50 degrees
        //  -land perfers to trend in the same direction as its previous neighbor
        double scale = 1;
        Point2D left_end, right_end;
        LandPiece initial_piece = new LandPiece(new Point2D.Double(0,600) , new Point2D.Double(50,600));
        land_pieces.add(initial_piece);
        Random r = new Random();
        boolean trend_up = false;
        for(int i=0; i<numberOfLandPieces; i++){
            // (r.nextInt(40) + 20) -- random length from 20 - 60
            //loop through and make pieces of land
            left_end = land_pieces.get(i).getRightEndpoint();
            if(left_end.getY() < 200){ trend_up = false; }
            else if(left_end.getY() > 500){ trend_up = true; }
            if(trend_up){ right_end = new Point2D.Double((r.nextInt(40) + 20)+left_end.getX(), left_end.getY()-(r.nextInt(40) + 20)); }
            else{ right_end = new Point2D.Double((r.nextInt(40) + 20)+left_end.getX(), left_end.getY()+(r.nextInt(40) + 20)); }

            LandPiece land = new LandPiece(left_end, right_end);
            land_pieces.add(land);
        }
    }

    public void paintInfo(Graphics2D g2d){
        //Create the font for the game info
        Font f = new Font("Monospaced", Font.PLAIN, 14);
        g2d.setFont(f);
        g2d.drawString("SCORE:       " + score, 450, 40);
        g2d.drawString("TIME:    " + time_min_10s+time_min+":"+time_sec_10s+time_sec , 450, 65);
        g2d.drawString("FUEL:     " + fuel , 450, 90);
        //convert numbers to ints for cleaner presentation
        int vertical_speed = - (int) current_lander.getVerticalSpeed();
        int horizontal_speed = (int) current_lander.getHorizontalSpeed();
        int altitude = HIEGHT - (int) current_lander.getY();
        g2d.drawString("VERTICAL SPEED:    " + vertical_speed + "  \u21E1", WIDTH-600, 40);
        g2d.drawString("HORIZONTAL SPEED:    " + horizontal_speed + "  \u21E2" , WIDTH-600, 65);
        g2d.drawString("ALTITUDE:           " + altitude, WIDTH-600, 90);
    }

    // Update timer and lander position
    public void actionPerformed(ActionEvent evt) {
        //...Perform a task...
        updateTime();
        if(timer%50==0){ //only update this every 10th a second
            updateLanderMomentum();
            updateLanderPosition();
        }
        //check for landing:
        //returns null or the closest land piece if it is close enough
        LandPiece closest_piece = checkClosestPiece();
        if(closest_piece != null){
            //if piece is close enough, zoom on the area
            zoomed = true;
            current_lander.setZoomBoolean(true);
            System.out.println("AAAAAA");
        }
        else {
            zoomed = false;
            current_lander.setZoomBoolean(false);
            closest_piece = null;
        }
        //create ajusted lander shape
        current_lander.setShape(current_lander.initializeLanderShape(current_lander.getCenter()));
        current_lander.setShape(current_lander.getRotatedShape(current_lander.getShape(), rotation));
        current_lander.setFlameShape(current_lander.initializeThrusterShape(current_lander.getCenter()));
        current_lander.setFlameShape(current_lander.getRotatedShape(current_lander.getFlameShape(), rotation, current_lander.getCenter()));
        repaint();
    }

    // checks x value of lander against all land pieces to find the one the lander is directly above
    // then does a landing check on the piece of land
    public LandPiece checkClosestPiece(){
        LandPiece closest_piece = land_pieces.get(0);
        for(LandPiece l : land_pieces){
            double closest = Math.abs(closest_piece.getMidpointX() - current_lander.getCenter().getX());
            double distance = Math.abs(l.getMidpointX() - current_lander.getCenter().getX());
            if(distance < closest){ closest_piece = l; }
        }
        System.out.println("CLOSEST: "+closest_piece.getMidpointX());
        System.out.println("LANDING: "+closest_piece.checkLanding(current_lander));
        double y_dist = Math.abs(closest_piece.getMidpointY() - current_lander.getCenter().getY());
        if(y_dist < 50){ //if close enough on the y return the closest piece
            return closest_piece;
        }
        else{
            return null;
        }
    }

    public void updateLanderMomentum(){
        double new_x_momentum = current_lander.getHorizontalSpeed();
        double new_y_momentum = current_lander.getVerticalSpeed();
        //first ajust y momentum based on gravity
        new_y_momentum += GRAVITY_CONSTANT;
        //then ajust based on thrust and rotation angle, if thrusting
        if(current_lander.getThrustBoolean()){
            double true_angle = 90 - Math.abs(rotation); // get rotation angle based on usual x/y setup
            //find changes in x and y using sine and cosine
            double y_change = (Math.sin(Math.toRadians(true_angle))) * THRUST_CONSTANT;
            double x_change = (Math.cos(Math.toRadians(true_angle))) * THRUST_CONSTANT;
            if(rotation < 0){ x_change = -x_change; } //if lander is facing left, reverse the x change
            else if(rotation == 0){ x_change = 0; } // if lander is straight up, ensure there is no x change

            new_x_momentum += x_change;
            new_y_momentum -= y_change;

            //ajust fuel level:
            fuel -= 1;
        }
        //finally set the lander momentums
        current_lander.setHorizontalSpeed(new_x_momentum);
        current_lander.setVerticalSpeed(new_y_momentum);
    }

    //update the position of the lander based on the x and y momentums
    public void updateLanderPosition(){
        double x_ajust = current_lander.getHorizontalSpeed() / 5;
        double y_ajust = current_lander.getVerticalSpeed() / 5;
        Point2D new_center = new Point2D.Double(current_lander.getX()+x_ajust, current_lander.getY()+y_ajust);
        current_lander.setCenter(new_center);
    }

    public void updateTime(){
        timer += SPEED; //catch timer variable up with the timer object's count
        time_sec += timer / 1000; // add a second for every 1000 miliseconds
        if(timer >= 1000){ timer = 0; } //reset the timer variable if a second is added
        if(time_sec > 9){
            time_sec = 0;
            time_sec_10s += 1;
        }
        if(time_sec_10s > 5){ //if 60 seconds is reached, add one to the minutes and reset the seconds
            time_sec_10s = 0;
            time_min += 1;
        }
        if(time_min > 9){
            time_min = 0;
            time_min_10s += 1;
        }
    }

    //methods for user interaction
    public void keyPressed(KeyEvent ke){
        if(ke.getKeyCode() == ke.VK_LEFT){
            //Rotate Lander Left
            rotation -= 2.5;
            current_lander.setRotation(rotation);
        }
        if(ke.getKeyCode() == ke.VK_RIGHT){
            //Rotate Lander Right
            rotation += 2.5;
            current_lander.setRotation(rotation);
        }
        if(ke.getKeyCode() == ke.VK_SPACE){
            //adds flame to lander shape and indicate that lander is thrusting
            current_lander.setThrustBoolean(true);
        }
    }
    public void keyReleased(KeyEvent ke){
        //if the player releases the spacebar, then lander should not thrust
        if(ke.getKeyCode() == ke.VK_SPACE){
            current_lander.setThrustBoolean(false);
        }
    }
    public void keyTyped(KeyEvent ke){}

}
