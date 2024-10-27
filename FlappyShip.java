//includes component,container,window,frame,dialog,canvas,panek,image,menu component,font,colors,etc
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
//ActionListener - The listener interface for recieving action events
//KeyListener - The listener interface for receiving keyboard events
//Jpanel - a generic lightweight container
public class FlappyShip extends JPanel implements ActionListener, KeyListener {
    int boardwidth = 360;
    int boardheight = 640;
    //images
    Image backgroundImg;
    Image shipImg;
    Image toppipeImg;
    Image bottompipeImg;

    //ship
    int shipX = boardwidth/8; // 1/8th of the width of the frame (initial position)
    int shipY = boardheight/2; // 1/2nd of the height of the frame (initial position)
    int shipwidth = 80; //width of the ship.png
    int shipheight = 60; //height of the ship.png
    class Ship {
        int x = shipX;
        int y = shipY;
        int width = shipwidth;
        int height = shipheight;
        Image img; //defining a img
        Ship(Image img) {
            this.img = img;  //refers to the current object in a method or constructor 

        }
    }
    //pipes
    int pipeX = boardwidth;
    int pipeY = 0;
    int pipewidth = 64; //scaled by 1/6
    int pipeheight = 512;
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipewidth;
        int height = pipeheight;
        Image img;
        boolean passed = false;
        Pipe(Image img) {
            this.img = img;
        }
    }
    //sound
    class Sound {
        Clip clip;
        URL soundUrl[] = new URL[30];
        public Sound() {
            soundUrl[0] = getClass().getResource("spaceship.wav");
            soundUrl[1] = getClass().getResource("crash.wav");
            soundUrl[2] = getClass().getResource("space-slash.wav");
        }
        public void setFile(int i) {
            try{
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundUrl[i]);
                clip = AudioSystem.getClip();
                clip.open(ais);
            }catch(Exception e) {
            }
        }
        public void play() {
            clip.start();
        }
        public void loop() {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        public void stop() {
            clip.stop();
        }
    }
    //game logic
    Ship ship;
    int velocityX = -4; //move pipes to the left speed (simulataneously ship moving right)
    int velocityY = 0; //move ship up/down speed
    int gravity = 1;
    ArrayList<Pipe> pipes; //creating an array of pipes
    Random random = new Random(); //to place the pipes in random
    Timer gameloop;  //loop of the(i.e the game won't end until the ship got hit by the pipe)
    Timer placePipeTimer;  //to manage the movement of pipes
    boolean gameover = false;
    double score = 0;
    double highestscore = 0;
    Sound sound;
    FlappyShip() {
        setPreferredSize(new Dimension(boardwidth,boardheight));  //frame
        //load images
        setFocusable(true); 
        addKeyListener(this);
        backgroundImg = new ImageIcon(getClass().getResource("./space.png")).getImage();
        shipImg = new ImageIcon(getClass().getResource("./spaceship.png")).getImage();
        toppipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottompipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();
        //ship
        ship = new Ship(shipImg);
        pipes = new ArrayList<Pipe>();
        sound = new Sound();
        //placepipetimer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();
        //game timer
        gameloop = new Timer(1000/60, this); //1000/60 = 16.6
        gameloop.start();
    }
    public void placePipes() {
        //(0-1)*pipeheight/2 -> (0-256)
        //128
        //0 -128 - (0-256) --> 1/4 pipeheight --> 3/4 pipeheight
        int randomPipeY = (int) (pipeY - pipeheight/4 - Math.random()*(pipeheight/2));
        int open = boardheight/4;
        Pipe toppipe = new Pipe(toppipeImg);
        toppipe.y = randomPipeY;
        pipes.add(toppipe);
        Pipe bottompipe = new Pipe(bottompipeImg);
        bottompipe.y = toppipe.y + pipeheight + open;
        pipes.add(bottompipe);

    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg,0,0,boardwidth,boardheight,null);
        //ship
        g.drawImage(ship.img,ship.x,ship.y,ship.width,ship.height,null);
        //pipes
        for(int i=0;i<pipes.size();i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img,pipe.x,pipe.y,pipewidth,pipeheight,null);
        }
        //score
        g.setColor(Color.white);
        g.setFont(new Font("Ariel",Font.PLAIN,32));
        if(gameover) {
            g.drawString("score: " + String.valueOf((int) score),10,35);
            g.drawString("GAME OVER", 80, 320 );
            g.drawString("highest score: " + String.valueOf((int) highestscore),10,70);
        }
        else {
            g.drawString("score: " + String.valueOf((int) score), 10, 35);
            g.drawString("highest score: " + String.valueOf((int) highestscore),10,70);
        }

    }
    public void move() {
        //ship
        velocityY += gravity;
        ship.y += velocityY;
        ship.y = Math.max(ship.y,0);
        //pipes
        for(int i=0;i<pipes.size();i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;
            if(!pipe.passed && ship.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; //0.5 because there are 2 pipes! so 0.5*2=1, 1 for each set of pipes
            }
            if(collisions(ship, pipe)) {
                gameover = true ;
                playSoundEffect(1);
            }
        }
        if(ship.y>boardheight) {
            gameover=true;
            playSoundEffect(2);
        }
    }
    public boolean collisions(Ship a,Pipe b) {
        return a.x < b.x + b.width &&  //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&  //a's top right corner passes b's top left corner
               a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;   //a's bottom left corner passes b's top left corner
    }
	@Override
	public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameover) {
            placePipeTimer.stop();
            gameloop.stop();

        }
	}
	@Override
	public void keyPressed(KeyEvent e) {
        //to access the keyboard
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if(gameover) {
                ship.y = shipY;
                velocityY = 0;
                pipes.clear();
                gameover = false;
                highestscore = Math.max(highestscore,score);
                score = 0;
                gameloop.start();
                placePipeTimer.start();
            }
        }
	}
    @Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
    
    //adding sound
    public void playSoundEffect(int i) {
        sound.setFile(i);
        sound.play();

    }
}

