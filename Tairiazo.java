import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;

public class Tairiazo extends JFrame
{
	GamePane gp;
	int width,height;
	int rows,cols;
	int puyo_len;
	Dimension screenSize;
	public Tairiazo()
	{
		super("Tairiazo");
		cols=6;
		rows=9;
		screenSize= Toolkit.getDefaultToolkit().getScreenSize();
		width=screenSize.width;
		height=screenSize.height;
		puyo_len=(width/8)*2/cols;
		gp=new GamePane(puyo_len,rows,cols);
		Container c=getContentPane();
		c.add(gp);
		setResizable(false);
		setBounds ((width/8)*3-puyo_len*3/2,(height/6)*1-puyo_len,(width/8)*2+puyo_len*3+6,(height/6)*4+25+puyo_len);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public static void main(String args[]) {
		System.out.println("Starting TAIRIAZO...");
		JFrame.setDefaultLookAndFeelDecorated(true);
		new Tairiazo();
	}
}
class GamePane extends JComponent implements ActionListener
{
	static int rows,cols;
	static int scr[][];
	Node tetris;
	Timer timer,timer1,timer2,anim_timer;
	Image img[]=new Image[4];
	Image fpipe,bpipe;

	Toolkit tk;
	Random rand;
	int rot;
	int len;
	boolean reached;
	int count;
	boolean started;
	boolean gameOver,paused;
	int a,b;
	int level,score,pieces,removed_puyos;
	int minscore;
	int anim;
	float alpha,alpha1;
	boolean levelflag;
	public GamePane(int l,int r,int c)
	{
		len=l;
		rows=r+1;
		cols=c;
		init();
		loadImages();
		generatePuyos();

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode()==KeyEvent.VK_ENTER)
				{

					if(!started)
					{
						setDelays();
						timer.start();
						started=true;
					}
					if(gameOver)
					{
						init();
						generatePuyos();
						started=false;
					}
					if(paused)
					{
						init();
						generatePuyos();
						started=false;
					}
					repaint();
				}
				else
				if(e.getKeyCode()==KeyEvent.VK_LEFT && !reached && !paused)
				{
					moveLeft();
				}
				else
				if(e.getKeyCode()==KeyEvent.VK_RIGHT && !reached && !paused)
				{
					moveRight();
				}
				else
				if(e.getKeyCode()==KeyEvent.VK_UP && !paused )
				{
					if(!reached)
					rotate();
					if(!started && level<19)
						level++;
				}
				else
				if(e.getKeyCode()==KeyEvent.VK_DOWN && !paused)
				{
					moveDown();
					if(!started && level>0)
					level--;
				}
				else
				if(e.getKeyCode()==KeyEvent.VK_P && started && !gameOver)
				{

					if(paused)
					{
						paused=false;
						alpha1=0.0f;
						timer.start();
					}
					else
					{
						timer.stop();
						paused=true;
					}
				}
				else
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
				{

					if( started && !gameOver)
					{
						if(paused)
						System.exit(0);
						else
						{
							timer.stop();
							paused=true;
						}
					}
					else
					System.exit(0);
				}
			}
		});
		setFocusable(true);
	}
	public void init()
	{
		scr=new int[rows][cols];
		rot=1;
		reached=true;
		count=0;
		started=false;
		gameOver=false;
		paused=false;
		a=0;
		b=0;
		level=0;
		score=0;
		pieces=-1;
		removed_puyos=0;
		minscore=50;
		anim=0;
		alpha=0.0f;
		alpha1=0.0f;
		levelflag=true;
		tk= Toolkit.getDefaultToolkit();
		rand=new Random();
		timer=new Timer(1000,this);
		timer.setInitialDelay(0);
		timer1=new Timer(1000,this);
		timer2=new Timer(500,this);
		anim_timer=new Timer(50,this);
		anim_timer.start();
	}
	public void loadImages()
	{
		String s="";
		if(len>=42)
		s="_";
		for(int i=0;i<img.length;i++)
		img[i]=tk.getImage("images\\puyo_"+s+(i+1)+".png");
		fpipe=tk.getImage("images\\pipe"+s+"1.png");
		bpipe=tk.getImage("images\\pipe"+s+".png");
	}

	public void setDelays()
	{
		int delay=0,delay1=0;
		for(int i=0;i<=level;i++)
		{
			delay+=20*(4-i/5);
			delay1+=4-i/5;
		}
		if(level==20)
		{
			delay+=25;
			delay1+=1;
		}
		timer.setDelay(1075-delay);
		anim_timer.setDelay(52-delay1);
		anim_timer.restart();
	}
	public void generatePuyos()
	{

		int p;
		if(cols%2==0)
		p=cols/2-1;
		else
		p=cols/2;
		if(scr[0][p]==0 && scr[1][p]==0)
		{
			scr[0][p]=a;
			scr[1][p]=b;
		}
		else
		{
			timer.stop();
			gameOver=true;
			return;
		}
		int r;
		while((r=rand.nextInt(8))%2==0);
		a=r;
		while((r=rand.nextInt(8))%2==0);
		b=r;
		pieces++;
		rot=1;
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==timer)
		{
			movePuyos();
		}
		else if(e.getSource()==timer1)
		{
			erase_puyos();
		}
		else if(e.getSource()==timer2)
		{
			fillVacated();
			timer2.stop();
		}
		repaint();
	}
	public void movePuyos()
	{
		int flag=0;
		for(int i=rows-1;i>=0;i--)
		for(int j=0;j<cols;j++)
		if(scr[i][j]%2==1)
		{
			if(i==rows-1)
			{
				scr[i][j]+=1;
				reached=true;

			}
			else if(scr[i+1][j]==0)
			{
				scr[i+1][j]=scr[i][j];
				scr[i][j]=0;
				flag=1;
			}
			else
			{
				scr[i][j]+=1;
				reached=true;
			}
			anim=0;
		}
		if(flag==0)
		erase_puyos();
	}
	public void erase_puyos()
	{
		int flag=0;
		for(int i=0;i<rows;i++)
		for(int j=0;j<cols;j++)
		if(scr[i][j]>0)
		{
			count=1;
			tetris=new Node(i,j);
			chkForTetris(i,j);
			if(count>=4)
			{
				removeAllTetris();
				flag=1;
				///////////////////////////////////////
				///////////////////////////////////////
			}
		}
		if(flag==1)
		{
			timer.stop();
			timer1.start();
			timer2.start();
			return;
		}
		timer1.stop();
		minscore=50;
		generatePuyos();
		if(!timer.isRunning())
		timer.start();
	}
	public void chkForTetris(int x,int y)
	{
		if(y<cols-1 && scr[x][y]==scr[x][y+1] && !existsInTetris(x,y+1))
		{
			count++;
			addToTetris(x,y+1);
			chkForTetris(x,y+1);
		}
		if(x<rows-1 && scr[x][y]==scr[x+1][y] && !existsInTetris(x+1,y))
		{
			count++;
			addToTetris(x+1,y);
			chkForTetris(x+1,y);
		}
		if(y>0 && scr[x][y]==scr[x][y-1] && !existsInTetris(x,y-1))
		{
			count++;
			addToTetris(x,y-1);
			chkForTetris(x,y-1);
		}

		if(x>0 && scr[x][y]==scr[x-1][y] && !existsInTetris(x-1,y))
		{
			count++;
			addToTetris(x-1,y);
			chkForTetris(x-1,y);
		}
	}
	public void addToTetris(int x,int y)
	{
		tetris.setNext(new Node(x,y));
		tetris.getNext().setPrev(tetris);
		tetris=tetris.getNext();
	}
	public boolean existsInTetris(int x,int y)
	{
		Node n=tetris;
		while(n!=null)
		{
			if(n.getX()==x && n.getY()==y)
			return true;
			n=n.getPrev();
		}
		return false;
	}
	public void removeAllTetris()
	{
		Node n=tetris;
		while(n!=null)
		{
			scr[n.getX()][n.getY()]=0;
			n=n.getPrev();
		}
		removed_puyos+=count;
		if(removed_puyos>=50)
		{
			if(levelflag)
			level+=1;
			else
			level-=1;
			if(level==20)
			levelflag=false;
			if(level==15 && !levelflag)
			levelflag=true;
			setDelays();
			removed_puyos=0;
		}
		score+=minscore*(count-3)*count;
		minscore=minscore*count;

	}
	public void fillVacated()
	{
		for(int i=rows-2;i>=0;i--)
		for(int j=0;j<cols;j++)
		if(scr[i][j]>0)
		{
			int k;
			for(k=i+1;k<=rows-1;k++)
			if(scr[k][j]>0)
			{
				scr[k-1][j]=scr[i][j];
				if(i!=k-1)
				scr[i][j]=0;
				break;
			}
			else if(k==rows-1)
			{
				scr[rows-1][j]=scr[i][j];
				if(i!=rows-1)
				scr[i][j]=0;
			}
		}
	}
	public void moveLeft()
	{
		for(int i=0;i<rows;i++)
		for(int j=0;j<cols;j++)
		if(scr[i][j]>0 && scr[i][j]%2==1 && j>0 )
		{

			if(j<cols-1 && scr[i][j+1]%2==1 && scr[i][j-1]==0)
			{																		scr[i][j-1]=scr[i][j];
					scr[i][j]=scr[i][j+1];
					scr[i][j+1]=0;
			}
			else
			if(scr[i][j-1]==0 && scr[i+1][j-1]==0 )
			{
				scr[i][j-1]=scr[i][j];
				scr[i+1][j-1]=scr[i+1][j];
				scr[i][j]=0;
				scr[i+1][j]=0;
			}
			return;
		}
	}
	public void moveRight()
	{
		for(int i=0;i<rows;i++)
		for(int j=cols-1;j>=0;j--)
		if(scr[i][j]>0 && scr[i][j]%2==1 && j<cols-1)
		{
			if(j>0 && scr[i][j-1]%2==1 && scr[i][j+1]==0)
			{
					scr[i][j+1]=scr[i][j];
					scr[i][j]=scr[i][j-1];
					scr[i][j-1]=0;
			}
			else
			if(scr[i][j+1]==0 && scr[i+1][j+1]==0 )
			{
				scr[i][j+1]=scr[i][j];
				scr[i+1][j+1]=scr[i+1][j];
				scr[i][j]=0;
				scr[i+1][j]=0;
			}
			return;
		}
	}
	public void rotate()
	{
		for(int i=0;i<rows;i++)
		for(int j=0;j<cols;j++)
		if(scr[i][j]>0 && scr[i][j]%2==1)
		{

			if(rot==1)
			{
				if(j>0 && scr[i][j-1]==0)
				{
					scr[i][j-1]=scr[i+1][j];
					scr[i+1][j]=0;
					rot=2;
				}
				else if(j<cols-1 && scr[i][j+1]==0)
				{
					scr[i][j+1]=scr[i][j];
					scr[i][j]=scr[i+1][j];
					scr[i+1][j]=0;
					rot=2;
				}
			}
			else if(rot==2 && i>1)
			{
				scr[i-1][j+1]=scr[i][j];
				scr[i][j]=0;
				rot=3;
			}
			else if(rot==3)
			{
				if(j<cols-1 && scr[i+1][j+1]==0)
				{
					scr[i+1][j+1]=scr[i][j];
					scr[i][j]=0;
					rot=4;
				}
				else if(j>0 && scr[i+1][j-1]==0)
				{
					scr[i+1][j-1]=scr[i+1][j];
					scr[i+1][j]=scr[i][j];
					scr[i][j]=0;
					rot=4;
				}
			}
			else if(rot==4 && i<rows-1)
			{
				if(scr[i+1][j]==0)
				{
					scr[i+1][j]=scr[i][j+1];
					scr[i][j+1]=0;
					rot=1;
				}
			}
			return;
		}
	}
	public void moveDown()
	{
		for(int i=rows-1;i>=0;i--)
		for(int j=0;j<cols;j++)
		if(scr[i][j]%2==1)
		{
			if(i==rows-1)
			{
				scr[i][j]=scr[i][j]+1;
				reached=true;
			}
			else if(scr[i+1][j]>0 && scr[i+1][j]%2==0)
			{
				scr[i][j]=scr[i][j]+1;
				reached=true;
			}
			else
			{
				scr[i+1][j]=scr[i][j];
				scr[i][j]=0;
			}
		}
		repaint();
	}
	public void paint(Graphics g)
	{
		g.setColor(Color.white);
		g.fillRect(0,0,len*cols,len*rows);
		g.setColor(Color.black);
		g.fillRect(len*cols,0,len*3,len*rows);
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g2.setPaint(new GradientPaint(cols*len,0,new Color(50,50,50),cols*len+len/2,0,new Color(200,200,200),false));
		g2.fill(new Rectangle(cols*len,0,len/2,rows*len));
		g2.setPaint(new GradientPaint((cols+2)*len+len/2,0,new Color(200,200,200),(cols+3)*len,0,new Color(50,50,50),false));
		g2.fill(new Rectangle((cols+2)*len+len/2,0,len/2,rows*len));
		g2.setPaint(Color.white);
		g.fill3DRect((cols+3/2)*len,len,len,len*2,true);
		g.drawImage(img[a/2],(cols+3/2)*len,len,len,len,null);
		g.drawImage(img[b/2],(cols+3/2)*len,len*2,len,len,null);
		g.fill3DRect((cols)*len+len/2,(rows+1)*len/2-5,len*2,len-2,true);
		g.fill3DRect((cols)*len+len/2,(rows+1)*len/2+len-5,len*2,len-2,true);
		g.fill3DRect((cols)*len+len/2,(rows+1)*len/2+2*len-5,len*2,len,true);
		g2.setPaint(Color.black);
		g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
		g2.drawString("Level: "+level,(cols)*len+len/2,rows*len/2+len);
		g2.drawString("pieces: "+pieces,(cols)*len+len/2,rows*len/2+2*len);
		g2.drawString("Score:"+score,(cols)*len+len/2,rows*len/2+3*len);
		int p;
		if(cols%2==0)
		p=cols/2;
		else
		p=cols/2+1;
		g.drawImage(bpipe,p*len-len,0,len,len,null);
		for(int i=0;i<rows;i++)
		for(int j=0;j<cols;j++)
		if(scr[i][j]>0)
		{
			int k=scr[i][j];
			if(k%2==0)
			{
				k=(int)k/2;
				g.drawImage(img[k-1],j*len,i*len,len,len,null);
			}
			else
			{
				k=(int)k/2+1;
				g.drawImage(img[k-1],j*len,(i-1)*len+anim,len,len,null);
				anim+=2;
				if(anim>=len)
				anim=len;
				if(reached && i==2)
				reached=false;
			}

		}

		g.drawImage(fpipe,p*len-len,0,len,len,null);


		if(!started)
		{
			g2.setPaint(Color.blue);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
			g2.fill(new Rectangle(0,rows*len/4,(cols+3)*len,(rows+1)*len/2));
			g2.setPaint(Color.black);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			g2.drawString("  Level: "+level,len*3,rows*len/3);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			g2.drawString("Use the <up> and <down> arrow keys to change level now",len/4,(rows+2)*len/3);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			g2.drawString(" Press <Enter> to start the Game",len,rows*len/2);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			g2.drawString("   Use the left,right and down arrow keys to move the characters.",0,(rows+1)*len/2);
			g2.drawString("   Pressing the up arrow key rotates the piece.",len,(rows+2)*len/2);
			g2.drawString("  When 4 or more same characters  are touching",len/3,(rows+3)*len/2);
			g2.drawString("                       they disappear. ",len,(rows+4)*len/2);
			g2.drawString("              Press <p> to pause the Game",len,(rows+5)*len/2);
			g2.drawString("            Press <Escape> to exit the Game",len,(rows+6)*len/2);
		}
		if(gameOver)
		{
			g2.setPaint(Color.white);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
			if(alpha<0.9f)
			alpha=alpha+0.02f;
			g2.fill(new Rectangle(0,0,len*cols,len*rows));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
			g2.setPaint(Color.red);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			g2.drawString("Game Over",len*3/2,rows*len/2);
			g2.setPaint(Color.blue);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			g2.drawString("Press <Enter> to restart the Game",len/2,(rows+1)*len/2);
		}
		if(paused)
		{
			g2.setPaint(Color.white);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha1));
			if(alpha1<0.9f)
			alpha1=alpha1+0.02f;
			g2.fill(new Rectangle(0,0,len*cols,len*rows));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
			g2.setPaint(Color.blue);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			g2.drawString("Game Paused",len*3/2,rows*len/2);
			g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			g2.drawString("   Press <p> to resume the Game",len/2,(rows+1)*len/2);
			g2.drawString("  Press <Escape> to exit the Game",len/2,(rows+2)*len/2);
			g2.drawString(" Press <Enter> to restart the Game",len/2,(rows+3)*len/2);
		}
	}
}
class Node
{
	int x,y;
	Node nextnode;
	Node prevnode;
	public Node(int x,int y)
	{
		this.x=x;
		this.y=y;
		nextnode=null;
		prevnode=null;
	}
	public Node(Node p)
	{
		x=p.x;
		y=p.y;
		nextnode=null;
		prevnode=null;

	}
	public void setNext(Node lnode)
	{
		nextnode=lnode;
	}
	public Node getNext()
	{
		return nextnode;
	}
	public void setPrev(Node lnode)
	{
		prevnode=lnode;
	}
	public Node getPrev()
	{
		return prevnode;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
}
