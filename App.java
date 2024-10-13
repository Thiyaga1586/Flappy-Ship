import javax.swing.*;
public class App {
    public static void main(String[] args) throws Exception {
        int boardwidth = 360;
        int boardheight = 640;
        JFrame frame = new JFrame("Flappy Ship");
        frame.setSize(boardwidth,boardheight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FlappyShip flappyShip = new FlappyShip(); //creating instance of flappyShip
        frame.add(flappyShip);
        frame.pack(); //so that the frame size will be 360x640 excluding the header
        flappyShip.requestFocus();
        frame.setVisible(true);
    }
}
