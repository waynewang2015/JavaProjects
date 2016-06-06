import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.*;

import javax.swing.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

class Bricks{

    public static int HEIGHT = 10;
    public static int WIDTH = 40;
    
    // The color of Bricks. 
    public static Color COLORS[] = {Color.black, Color.yellow};
    
    // The color of Paddle.
    public static Color X = Color.blue;
    
    Color color; Rectangle r;

    // Default visible, on hit disappear.
    boolean visible; boolean x;

    //If the ball has hit a brick ?
    boolean valid;
    
    // The brick constructor. 
    // It takes an element in a 2d array and randomly chooses a color.
    public Bricks(int x, int y){ this(x, y, COLORS[(int)(COLORS.length*Math.random())]); }

    public Bricks(int x, int y, Color color){
        super();
        this.color = color;
        this.r = new Rectangle(x, y, WIDTH, HEIGHT);
        this.visible = true;
        this.x = false;
        this.valid = true;
    }
    
    public void draw(Graphics g){
        if ((valid)&&(visible)){
            if (!x){           	
                g.setColor(color);
                g.fill3DRect(r.x, r.y, r.width, r.height, true); 
                g.draw3DRect(r.x, r.y, r.width, r.height, true);
            }else{
                g.setColor(X);
            }
        }
    } 
    
    public boolean contains(int x, int y){
        if (!valid){
            return false;
        }else{
            return r.contains(x, y);
        }
    }
    
    public boolean isVisible(){ return visible; }

    public void setX(boolean x){ this.x = x; }

    public boolean isX(){ return x;}

    public void setValid(boolean valid){ this.valid = valid; }

    public boolean isValid(){ return valid; }

}

/*************************************************************************/

class Control implements KeyListener{
    Breakout parent=null;
    
    public Control(Breakout parent){
        super();
        this.parent=parent;
    }
    
    // Boiler plate keypressed
    public void keyPressed(KeyEvent e){
        char key=Character.toLowerCase(e.getKeyChar());
        if (key=='w'){ parent.start(); }
        else if(key=='a'){ parent.moveLeft(); }
        else if(key=='d'){ parent.moveRight(); }
        else if(key=='p'){ parent.pause(); }
        else if(key=='r'){ parent.restart(); }
        else if(key=='s'){ parent.stop(); }
    }

    public void keyReleased(KeyEvent arg0){ parent.stopMoving(); }
    
    public void keyTyped(KeyEvent arg0){}
}

/****************************************************************************/

class Display extends JPanel implements Runnable{
    ArrayList<String> scores = new ArrayList<String>();
	String[] strScore = new String[10];
	
	Graphics g;
    Color DARK_BLUE = new Color(0, 0, 100);
    
    int PAUSE=30;
    int MOVE_LEFT=-1;
    int MOVE_RIGHT=1;
    int MOVE_NONE=0;

    private int lifeCount=3;
    int scoreCount=0;
    
    // Dimenions of the Bricks
    int brickGapHeight=20, brickGapWidth=16;
    int brickHeight=10, brickWidth=30;

    // Dimensions of the Paddle
    int paddleHeight=10, paddleWidth=55;
    int bottomGap=20;

    // Dimensions of the Ball
    int ballHeight=8, ballWidth=8;
    
    Thread thread=null;
    int pause=PAUSE;
    
    int x=100;
    int move=MOVE_NONE;
    
    int bx=-1, by=-1;
    int dx=-2, dy=3;
    
    boolean done=false, gameOver=true;
    
    Breakout parent;
    
    // Each entry in the matrix represents a brick.
    Bricks Bricks[][] = new Bricks[6][8];
    
    public Display(Breakout parent){
        super();
        JOptionPane.showMessageDialog(null,"Bingzhen Wang\n20493844\nW:start  A:left  D:right\nS:stop  P:pause  R:resart\nPress OK to begin!");
        this.parent=parent;
        init();  
    }
    
    // Makes the bricks to be drawn later.
    public void init(){
    	// Looping through the bPlayColor matrix to create new bricks
       for (int row=0; row<Bricks.length; row++){
           for (int col=0; col<Bricks[row].length; col++){
               Bricks[row][col] = new Bricks(brickToX(col), brickToY(row) );
               if ((row%2)==0){
                   if ((col%4)==0){
                       Bricks[row][col].setX(false);
                   }else if ((col%4)==2){
                       Bricks[row][col].setX(false);
                       }    
               }else{
                   if ((col%4)==0){
                       Bricks[row][col].setX(false);
                   }else  if ((col%4)==2){
                       Bricks[row][col].setX(false);
                       }    
               }
           }
       }
    }
    
    // Makes a call to draw for each element of the 2d Brick array
    public void drawBricks(Graphics g){
        for (int row=0; row<Bricks.length; row++){
            for (int col=0; col<Bricks[row].length; col++){
            	// draw(g) is a function of Bricks
                Bricks[row][col].draw(g);
            }
        }    
    }
    
    private void checkHitBrick(){
        for (int row=0; row<Bricks.length; row++){
            for (int col=0; col<Bricks[row].length; col++){
                int bcenterX=bx+ballWidth/2;
                int bcenterY=by+ballHeight/2;
                
                /* If the brick at [row][col] has the center of the ball within it,
                   then set the box to invalid, add to the score, play the sound and
                   make the brick disappear. */
                if (Bricks[row][col].contains(bcenterX, bcenterY)){
                    Bricks[row][col].setValid(false);
                    parent.advanceCounter();  
                    scoreCount++;
                    dy*=(-1);
                    
                    if (Bricks[row][col].isX() )
                        this.faster();
                    if (Bricks[row][col].isX())
                        this.faster();
                }
            }
        }
    }
    
    // Checks if the paddle was hit by the ball and sends it out in the opposite direction
    private void checkHitPaddle(){
        int bcenterX=bx+ballWidth/2;
        int bcenterY=by+ballHeight/2;
        
        if ((dy>0)&&(bcenterY>=getHeight()-paddleHeight-bottomGap)){
            if ((x<=bcenterX)&&(bcenterX<x+paddleWidth)){
                dy = -dy;
                dx+=randomSign()*(int)(2*Math.random());  
            }else{
            	// if the ball doesn't hit the paddle.
            	parent.lifeCountDown();
            	lifeCount--;
            	stop();
            	if (lifeCount==0){ /* Initiates the game over sequence */
            		parent.lifeCountStart();
            		parent.resetCounter();
            		stop();
            		try{ highScore(); }
            		catch(IOException e){}
            		init();
            	}	
            }
        }
    }
    
    // These next two functions determine brick placement and the gaps inbetween the bricks
    private int brickToY(int i){ return i*(brickHeight+brickGapHeight) + brickGapHeight; }
    
    private int brickToX(int i){ return i*(brickWidth+brickGapWidth) + brickGapWidth; }
    
    // This randomizes the bounce of the ball when it makes contact.
    int randomSign() {
        if (Math.random()>0.5){
            return 1;
        }else{
            return -1;
        }
    }
    
    // Start
    public void start(){
        if (thread==null){
            thread=new Thread(this);
            thread.start();
            bx=x+paddleWidth/2;
            by=getHeight() - paddleHeight - bottomGap;
            dx=randomSign()*(int)(4*Math.random()) + 1;
            dy = -2;
            pause=PAUSE;
            move=MOVE_NONE;
            gameOver=false;
        }
    }
    
    // Stop
    public void stop(){
        thread=null;
        move=MOVE_NONE;
        gameOver=true;
        repaint();
    } 
    
    // Pause
    public void pause(){
        thread=null;
        move=MOVE_NONE;        
    }

    // Restart
    public void restart(){
        if (thread==null){
            thread = new Thread(this);
            thread.start();
        }        
    } 

    public void faster(){
        if (pause>=10)
            pause-=15;
    }
    
    public void slower(){
        pause+=5;        
    } 
    
    public void run(){
        while (thread!=null){
            if (move==MOVE_LEFT){
                moveLeft();
            }else if (move==MOVE_RIGHT){
                moveRight();
            }

            bx+=dx;
            by+=dy;
       
            if (by<=0) /* Hitting the bottom of the window */{
                dy*=(-1);
            }else if ((bx<=0) || (bx>=getWidth())) /* Hitting the side edges of the panel */{
                dx*=(-1);
            }else /* If neither, constantly check if the paddle or brick is hit */{
                checkHitBrick();
                checkHitPaddle();
            }
            repaint();
            try{
                thread.sleep(pause);
            }
            catch (InterruptedException e){
                move=MOVE_NONE;
                e.printStackTrace();
            }
        }
    }     
    
    // Moving the paddle left. X determines the speed at which it moves.
    public void moveLeft(){
        if (x<0){
            move=MOVE_NONE;
        }else{
            move=MOVE_LEFT;
            x-=8;
            repaint();
        }   
    }
    
    // Moving the paddle right, x determines the speed at which it moves
    public void moveRight(){
        if (x<getWidth()-paddleWidth){
            move=MOVE_RIGHT;
            x+=8;
            repaint();
        }else{
            move=MOVE_NONE;
        }
    }

    public void stopMoving(){ move=MOVE_NONE; }

    // Draw a paddle
    public void drawPaddle(Graphics g){
        g.setColor(DARK_BLUE);
        g.drawRect(x, getHeight()-paddleHeight-bottomGap, paddleWidth, paddleHeight);
        g.setColor(Color.BLUE);
        g.fillRect(x, getHeight()-paddleHeight-bottomGap, paddleWidth, paddleHeight);
    }
    
    // Draw a ball
    public void drawBall(Graphics g){
        if (gameOver){
            bx=x + paddleWidth/2 - ballWidth/2;
            by=getHeight()-paddleHeight-bottomGap-ballHeight;;
        }
        g.setColor(Color.RED);
        g.fillOval(bx, by, ballHeight, ballWidth);
    }
    
    // Draws the brick, paddle and ball.
    public void paintComponent(Graphics g)  {   	
        super.paintComponent(g); 
        
        drawBricks(g);
        drawPaddle(g);
        drawBall(g);
    }
    
    // This asks for the user's name and saves his score into a file. It then shows the top 10.
    public String[] readScores( String [] x) throws IOException{
    	
    	BufferedReader br=new BufferedReader(new FileReader("highscore.txt"));
    	String sCurrentLine;
    	// If there is a next line it is added to an ArrayList.
		while ((sCurrentLine=br.readLine())!=null) {
			scores.add(sCurrentLine);	
			for (int i=0; i<x.length; i++)
				 x[i]=sCurrentLine;
		}
		br.close();
		return x;
    }
    
    // End of the game high score popup
    public void highScore() throws IOException{
    	done=true;
		
        // Asking the user for input.
    	JOptionPane.showMessageDialog(null,"Game Over!");
    	String name=JOptionPane.showInputDialog("Enter your name");
    	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("highscore.txt", true)));
    	out.println(scoreCount + "\t" + name);
    	out.close();
    	
        // Reading from the file
    	BufferedReader br=new BufferedReader(new FileReader("highscore.txt"));
    	String sCurrentLine;
    	
        // If there is a next line it is added to an ArrayList.
		while ((sCurrentLine=br.readLine())!=null) { scores.add(sCurrentLine); }
		br.close();

    	// These next lines sort them then reverse the order. The largest number will be printed first.
    	Collections.sort(scores); Collections.reverse(scores);

		for(int i=0 ; i<scores.size() ;i++){ System.out.println(i+1 + ") " + scores.get(i)); }
    }

    public Dimension getPreferredSize(){ return new Dimension(400, 400); }
}

/**********************************************************************************************/

class Update extends JLabel {
    int counter=0; int lifeCount=3;
    
    // A basic class for updating the top Panel with the score and lives.
    public Update() {
        super();
        counter=0;
        lifeCount=3;
        setText("Points: " + counter + " points" + " | Life: " + lifeCount);
    }  

    public void upCounter() {
        counter++;
        setText("Points: " + counter + " points" + " | Life: " + lifeCount);
    }

    public void resetCounter(){
        counter=0;
        setText("Points: " + counter + " points" + " | Life: " + lifeCount);
    }

    public void lifeCountDown(){
    	if (lifeCount<=0){
        	lifeCountStart();
        }else{
    		lifeCount--;
    		setText("Points: " + counter + " points" + " | Life: " + lifeCount);
    	}
    }

    public void lifeCountStart(){
    	lifeCount=3;
    	setText("Points: " + counter + " points" + " | Life: " + lifeCount);
    }
}

/***********************************************************************************************/

public class Breakout extends JFrame{
   
    Control con=null;
    Display dis=null;
    Update update=null;
     
    // This constructor brings everything together. Call it from a Main function to start.
    public Breakout(){
        super("Breakout");     
        con=new Control(this);
        dis=new Display(this);
        update=new Update();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add("Center", dis);
        getContentPane().add("North", update);
        
        validate();
        pack();
        setVisible(true);
        
        this.addKeyListener(con); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
    }
    
    public void start(){ dis.start(); }
    public void stop(){ dis.stop(); }
    
    public void moveRight(){ dis.moveRight(); }
    public void moveLeft(){ dis.moveLeft(); }
    
    public void advanceCounter(){ update.upCounter(); } 
    public void resetCounter(){ update.resetCounter(); }
    
    public void lifeCountStart(){ update.lifeCountStart(); }
    public void lifeCountDown(){ update.lifeCountDown(); }
    
    public void restart() { dis.restart(); }
    public void pause() { dis.pause(); }
    
    public void stopMoving(){ dis.stopMoving(); }

    public static void main(String args[]) { Breakout game = new Breakout(); }

}
