import java.awt.*;
import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setTitle("Lunar Lander");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LunarCanvas lCanvas = new LunarCanvas();
    	Container cp = frame.getContentPane();
    	frame.setLayout(new BorderLayout());
        frame.add(lCanvas, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
 //end main
}
