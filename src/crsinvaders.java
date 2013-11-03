import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.*;
import java.net.*;
import java.awt.Graphics2D;
import java.util.logging.*;
import javax.sound.sampled.AudioFormat;
import java.util.*;
import java.awt.geom.*;

import java.net.*;
import java.awt.Graphics2D;

import java.awt.geom.*;

class invader 
{
	boolean alive = true;
	boolean dying = false;
	int dyingCountDown = 0;
	crsinvaders IA = null;

	Image goLeft[] = null;
	Image goRight[] = null;

	int x = 0;
	int y = 0;
	int shotcountdown = 0;
	int pointValue = 0;

	/* static var that govern all invaders */
	public static boolean changeDir = false;
	public static int chx = 11;
	/*Affected by level variable*/
	public static int stepmax = 8;
	public static int yAdjustment = 0;

	public static final int chy = 17;
	public static int stepper = stepmax - 1;
	public static int steplimit = stepmax;
	public static final int lowX = 10;
	public static final int highX = 584;

	public static boolean doMove = false;
	public static boolean doFlip = false;
	public static boolean dropDown = false;	
	public static boolean squash = false;	

	public static int livingInvaders = 55;	

	public static int gamelevel = 1;	

	public static void resetCommonProperties()
	{
		stepmax = 8;
		yAdjustment = 0;	

		switch(gamelevel)
		{
			case 5:
				stepmax = 5;
				yAdjustment =  17 * 4;
				break;
			case 4:
				stepmax = 6;
				yAdjustment =  17 * 3;
				break;
			case 3:
				stepmax = 7;
				yAdjustment =  17 * 2;
				break;
			case 2:
				yAdjustment = 17;
				break;

			default:
				/* Do Not Alter Step and Step Max */
				break;
		}

		changeDir = false;
		chx = 11;
		stepper = stepmax - 1;
		steplimit = stepmax;
		doMove = false;
		doFlip = false;
		dropDown = false;	
		squash = false;	
		livingInvaders = 55;
	}

	public static void adjustLevel()
	{
		if (livingInvaders < 34)
		{
			steplimit = stepmax - 1;
		}
		if (livingInvaders < 23)
		{
			steplimit = stepmax - 2;
		}
		if(livingInvaders < 12)
		{
			steplimit = 2;
		}
		if(livingInvaders< 6)
		{
			steplimit = 1;
		}
		if(livingInvaders == 1)
		{
			steplimit = 0;
		}
	}

	public static void resetGameLevel()
	{
		gamelevel = 1;
	}

	public static void increaseGameLevel()
	{
		gamelevel++;
		if(gamelevel > 5)
		{
			gamelevel = 5;
		}
	}

	public static void checkReverse(int x)
	{
		if( (x >= highX) && (chx > 0))
		{
			changeDir = true;
			return;
		}
	
		if( (x <= lowX ) && (chx < 0))
		{
			changeDir = true;
			return;
		}
	}

	public static void checkSquash(int y)
	{
		if((y > 368) && dropDown)
		{
			squash = true;	
		}
	}

	public static void checkDropShield(int y)
	{
		if(y > 317)
		{
			shield.showShields = false;
		}
	}

	public static void stepInvaders()
	{
		if(changeDir)
		{
			dropDown = true;
			changeDir = false;
			chx = -chx;
		}

		stepper++;
		if(stepper >= steplimit)
		{
			doMove = true;
			livingInvaders = 0;
			doFlip = !doFlip;
			stepper = 0;
		}
	}


	public static void afterInvaders()
	{
		if(doMove)
		{
			doMove = false;
			dropDown = false;
		}
	}

	public invader(crsinvaders p_IA,Image l1, Image l2, Image r1, Image r2,int x,int y,int points)
	{
		alive = true;
		dying = false;
		IA = p_IA;
		goLeft = new Image[2];
		goRight = new Image[2];

		goLeft[0] = l1;
		goLeft[1] = l2;
		goRight[0] = r1;
		goRight[1] = r2;

		this.x = x;
		this.y = y + invader.yAdjustment;
		shotcountdown = 2;
		pointValue = points;
	}

	public void drawOn(Graphics g)
	{
		if(dying)
		{
			dyingCountDown--;
			if(dyingCountDown < 0) 
			{
				dying = false;
			}
			/* Draw little pop symbol */
			g.drawImage(crsinvaders.popstarImage,x,y,null);
			return;
		}

		if(!alive)
		{
			return;
		}

		if(chx > 0)
		{
			if(doFlip)
			{
				g.drawImage(goRight[1],x,y,null);
			}
			else
			{
				g.drawImage(goRight[0],x,y,null);
			}
		}
		else
		{
			if(doFlip)
			{
				g.drawImage(goLeft[1],x,y,null);
			}
			else
			{
				g.drawImage(goLeft[0],x,y,null);
			}
		}
		
	}

	public void move()
	{
		/*check if alive*/
		if(!alive)
		{
			return;	
		}

		livingInvaders++;

		if(!doMove)
		{
			/* check for missile hit at current location */
			if(playerMissile.testAllHit(x,y))
			{
				alive = false;
				dying = true;
				player.score += pointValue;
				dyingCountDown = 5;
				/*System.out.println("Players Score : " + player.score);*/
				player.checkForFreeGuy();
			}
			return;
		}

		if(shotcountdown > 0)
		{
			shotcountdown--;
		}

		if(dropDown)
		{
			y += chy;
		}

		if((!doFlip || (steplimit < 1)) && !dropDown)
		{
			x += chx;
		}

		/* check for missile hit at new location */	

		if(playerMissile.testAllHit(x,y))
		{
			alive = false;
			dying = true;
			dyingCountDown = 5;
			player.score += pointValue;
			/*System.out.println("Players Score : " + player.score);*/
			player.checkForFreeGuy();
			return;
		}

		int diffx = player.x - x;
		if (diffx < 0)
		{
			diffx = -diffx;
		}

		/* Enemy shooting routine - no intelligence just want random fire
		 * over the player with a small chance of random shots happening
		 * some where else
		 */
		if((shotcountdown < 1) && (diffx < 40) && enemyMissile.readyToFire())
		{
			if(Math.random() > 0.7)
			{
				enemyMissile.fireAMissile(x,y);
				shotcountdown = 2;
			}
		}
		else if((shotcountdown < 1) && (diffx < 60) && enemyMissile.readyToFire())
		{
			if(Math.random() > 0.9)
			{
				enemyMissile.fireAMissile(x,y);
				shotcountdown = 3;
			}
		}
		else if((shotcountdown < 1) && enemyMissile.readyToFire() && Math.random() < 0.05)
		{
			enemyMissile.fireAMissile(x,y);
			shotcountdown = 4;
		}

		/*if still alive*/
		checkReverse(x);
		checkSquash(y);
		checkDropShield(y);
	}

	public void dropSquash()
	{
		if(alive)
		{
			y += 40;
		}
	}
}

class player
{
	public static int x = 25;
	public static int lives = 3;
	public static final int y = 424; 
	public static final int lowX = 13;
	public static final int highX = 600;
	public static boolean show = true;
	public static boolean readyToFire = true;
	public static final int readyFireCountdown = 2;
	public static int countdown = 0;
	public static boolean alive = true;
	public static int chx = 5;

	public static boolean dying = false;;
	public static int dyingidx = 0;

	public static boolean leftKeyDown = false;
	public static boolean rightKeyDown = false;	

	public static int score = 0;	
	public static int nextFreeLife = 5000;

	public player()
	{
		reset();
	}

	public static void reset()
	{
		x = 115;	
		lives = 3;
		show = true;
		readyToFire = true;
		countdown = 0;
		alive = true;
		dying = false;
		dyingidx = 0;
		score = 0;
		nextFreeLife = 5000;
	}

	public static boolean replacePlayer(boolean lostlife)
	{
		dying = false;
		dyingidx = 0;

		if(lostlife)
		{
			lives--;
		}

		if(lives < 1)
		{
			return (false);
		}

		x = 115;
		alive = true;
		show = true;
		readyToFire = true;
		countdown = 0;

		return(true);
	}

	public static void fire()
	{
		if(!alive || !readyToFire)
		{
			return;
		}

		readyToFire = false;
		countdown = readyFireCountdown;
		playerMissile.fireAMissile(x);
	}

	public static void move()
	{
		if(!alive)
		{
			return;
		}

		if(!readyToFire)
		{
			if(countdown < 1 && playerMissile.readyToFire())
			{
				readyToFire = true;
				/*check to make sure both bullets are not in flight*/
			}
			if(countdown > 0)
			{
				countdown--;
			}
		}

		if(rightKeyDown)
		{
			x += chx;
			if(x > highX)
			{
				x = highX;
			}
		}
		else if(leftKeyDown)
		{
			x -= chx;
			if(x < lowX)
			{
				x = lowX;
			}
		}

        /* check for enemy missle connecting */
		if(enemyMissile.testAllHit(x,y))
		{
			alive = false;
			dying = true;
			dyingidx = 0;
		}
		 

	}

	public static void drawOn(Graphics g)
	{
		if(!show)
		{
			return;
		}

		if(dying)
		{
			dyingidx = (dyingidx + 1) % 4;
			/* draw death here */
			g.drawImage(crsinvaders.shipSplat[dyingidx],x,y,null);
			return;
		}

		if(alive)
		{
			if(readyToFire)
			{
				g.drawImage(crsinvaders.playerImage[0],x,y,null);
			}
			else
			{
				g.drawImage(crsinvaders.playerImage[1],x,y,null);
			}
			return;
		}

		/* if dead maybe show something */
	}

	public static void checkForFreeGuy()
	{
		if(score >= nextFreeLife)
		{
			nextFreeLife += 5000;
			lives++;
		}
	}
}

class playerMissile
{
	boolean alive = false;
	boolean dying = false;
	int x = 0;
	int y = 0;
	int idx = 0;
	public static final int startY = 414;
	public static final int startXmod = 8;
	public static final int chy = -6;

	public static playerMissile pm[] = null;

	private playerMissile()
	{
		alive = false;
		dying = false;
		x = 0;
		y = 0;
		idx = 0;
	}

	public static playerMissile[] getMissileQue()
	{
		int i;
		if(null == pm)
		{
			pm = new playerMissile[2];
			for(i = 0; i < 2 ; i++)
			{
				pm[i] = new playerMissile();
			}
		}
		return pm;
	}

	public static void resetMissileQue()
	{
		playerMissile Q[] = getMissileQue();

		for(int i = 0 ; i < Q.length ;i++)
		{
			Q[i].reset();
		}
	}

	public static boolean readyToFire()
	{
		playerMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(!Q[i].alive)
			{
				return (true);
			}
		}

		return (false);
	}

	public static void fireAMissile(int x)
	{
		playerMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(!Q[i].alive)
			{
				Q[i].fireMissile(x);
				return;
			}
		}

	}

	public static void drawOnAll(Graphics g)
	{
		playerMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			Q[i].drawOn(g);
		}
	}

	public static void moveAll()
	{
		playerMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			Q[i].move();
		}
	}

	public void reset()
	{
		alive = false;
		x = 0;
		y = 0;
		idx = 0;
		dying = false;
	}

	public void fireMissile(int x)
	{
		alive = true;
		dying = false;
		this.x = x + startXmod;
		y = startY;
	}

	public void move()
	{
		if(!alive)
		{
			return;
		}
		idx = (idx + 1) % 4;
		
		y += chy;

		if(y < 0 )
		{
			alive = false;
		}
	}

	public void drawOn(Graphics g)
	{

		if(dying)
		{
			dying = false;
			g.drawImage(crsinvaders.playerMissileBlip,x,y,null);
		}

		if(!alive)
		{
			return;
		}

		g.drawImage(crsinvaders.rollPhone[idx],x,y,null);
	}

	public static boolean testAllHit(int x, int y)
	{
		playerMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(Q[i].testHit(x,y))
			{
				return(true);
			}
		}
		return(false);
	}

	public boolean testHit(int x, int y)
	{
		int x1,y1,x2,y2;

		if(!alive)
		{
			return (false);
		}

		x1 = x - 8;
	    y1 = y - 3;
		x2 = x + 29;
		y2 = y + 47;
	
		if((this.x >= x1) && (this.x <= x2) && (this.y >= y1) && (this.y <= y2))
		{
			alive = false;
			dying = true;
			return(true);
		}


		return (false);
	}

	public static boolean testHitShieldAll(int x,int y)
	{
		playerMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(Q[i].testHitShield(x,y))
			{
				return(true);
			}
		}
		return(false);
	}

	public boolean testHitShield(int x,int y)
	{
		int x1,y1,x2,y2;

		if(!alive)
		{
			return (false);
		}

		x1 = x - 5;
		y1 = y - 5;
		x2 = x + 7;
		y2 = y + 7;
	
		if((this.x >= x1) && (this.x <= x2) && (this.y >= y1) && (this.y <= y2))
		{
			alive = false;
			dying = true;
			return(true);
		}


		return (false);
	}
}

class enemyMissile
{
	boolean alive = false;
	boolean dying = false;
	int x = 0;
	int y = 0;
	int idx = 0;
	public static final int startYmod = 20;
	public static final int startXmod = 12;
	public static final int chy = 6;

	public static enemyMissile pm[] = null;

	private enemyMissile()
	{
		alive = false;
		dying = false;
		x = 0;
		y = 0;
		idx = 0;
	}

	public static enemyMissile[] getMissileQue()
	{
		int i;
		if(null == pm)
		{
			pm = new enemyMissile[4];
			for(i = 0; i < 4 ; i++)
			{
				pm[i] = new enemyMissile();
			}
		}
		return pm;
	}

	public static void resetMissileQue()
	{
		enemyMissile Q[] = getMissileQue();

		for(int i = 0 ; i < Q.length ;i++)
		{
			Q[i].reset();
		}
	}

	public static boolean readyToFire()
	{
		enemyMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(!Q[i].alive)
			{
				return (true);
			}
		}

		return (false);
	}

	public static void fireAMissile(int x,int y)
	{
		enemyMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(!Q[i].alive)
			{
				Q[i].fireMissile(x,y);
				return;
			}
		}

	}

	public static void drawOnAll(Graphics g)
	{
		enemyMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			Q[i].drawOn(g);
		}
	}

	public static void moveAll()
	{
		enemyMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			Q[i].move();
		}
	}

	public void reset()
	{
		alive = false;
		x = 0;
		y = 0;
		idx = 0;
		dying = false;
	}

	public void fireMissile(int x,int y)
	{
		alive = true;
		dying = false;
		this.x = x + startXmod;
		this.y = y + startYmod;
	}

	public void move()
	{
		if(!alive)
		{
			return;
		}
		idx = (idx + 1) % 4;
		
		y += chy;

		if(y > 475 )
		{
			alive = false;
		}
	}

	public void drawOn(Graphics g)
	{

		if(dying)
		{
			dying = false;
			g.drawImage(crsinvaders.playerMissileBlip,x,y,null);
		}

		if(!alive)
		{
			return;
		}

		g.drawImage(crsinvaders.alienPhone[idx],x,y,null);
	}

	public static boolean testAllHit(int x, int y)
	{
		enemyMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(Q[i].testHit(x,y))
			{
				return(true);
			}
		}
		return(false);
	}

	public boolean testHit(int x, int y)
	{
		int x1,y1,x2,y2;

		if(!alive)
		{
			return (false);
		}

		x1 = x -6;
		y1 = y + 7;
		x2 = x + 21;
		y2 = y + 42;
	
		if((this.x >= (x1)) && (this.x <= x2) && (this.y >= y1) && (this.y <= y2))
		{
			alive = false;
			dying = true;
			return(true);
		}

		return (false);
	}

	public static boolean testHitShieldAll(int x,int y)
	{
		enemyMissile Q[] = getMissileQue();
		for(int i = 0 ; i < Q.length ;i++)
		{
			if(Q[i].testHitShield(x,y))
			{
				return(true);
			}
		}
		return(false);
	}

	public boolean testHitShield(int x,int y)
	{
		int x1,y1,x2,y2;

		if(!alive)
		{
			return (false);
		}

		x1 = x - 5;
		y1 = y - 5;
		x2 = x + 7;
		y2 = y + 7;
	
		if((this.x >= x1) && (this.x <= x2) && (this.y >= y1) && (this.y <= y2))
		{
			alive = false;
			dying = true;
			return(true);
		}


		return (false);
	}

}

class shield
{
	public static boolean showShields = true;
	public static shield Shields[] = null;

	int x,y;
	int pieces[] = null;
	boolean alive = true;

	public static void resetShields()
	{
		showShields = true;
		if(invader.gamelevel > 4)
		{
			showShields = false;
		}

		shield sq[] = getShieldQueue();
		for(int i = 0 ; i < sq.length ; i++)
		{
			sq[i].resetShield();
		}
	}

	public static shield[] getShieldQueue()
	{
		if(null == Shields)
		{
			Shields = new shield[3];
			Shields[0] = new shield(101,366);
			Shields[1] = new shield(290,366);
			Shields[2] = new shield(479,366);
		}
		return Shields;
	}

	private shield()
	{
		/* not used */
	}

	private shield(int x, int y)
	{
		this.x = x;
		this.y = y;
		pieces = new int[20];
		resetShield();
	}

	private void resetShield()
	{
		alive = true;
		for(int i=0; i < 20 ; i++)
		{
			pieces[i] = 3;
		}
	}

	public static void drawOnAll(Graphics g)
	{
		shield sq[] = getShieldQueue();
		for(int i = 0 ; i < sq.length ; i++)
		{
			sq[i].drawOn(g);
		}
	}

	public void drawOn(Graphics g)
	{
		int i;

		if(!showShields || !alive)
		{
			/*
			System.out.println("Do Not Draw Shields");
			if(!showShields)
			{
				System.out.println("no show");
			}
			if(!showShields)
			{
				System.out.println("not alive");
			}
			*/
			return;
		}	

		boolean anypieces = false;
		for(i = 0; i < 20 ; i++)
		{
			int x = this.x + (i % 5) * 12;
			int y = this.y + (i / 5) * 12;

			if(pieces[i] > 0)
			{
				g.drawImage(crsinvaders.shieldImage[(pieces[i] - 1)],x,y,null);
				anypieces = true;
			}
		}
		if(anypieces)
		{
			g.drawImage(crsinvaders.shieldOverlay,this.x,this.y,null);
			return;
		}

		alive = false;

		return;
	}

	public static void moveAll()
	{
		shield sq[] = getShieldQueue();
		for(int i = 0 ; i < sq.length ; i++)
		{
			sq[i].move();
		}
	}

	public void move()
	{
		int i;

		if(!showShields || !alive)
		{
			return;
		}	

		for(i = 0; i < 20 ; i++)
		{
			int x = this.x + (i % 5) * 12;
			int y = this.y + (i / 5) * 12;

			if(pieces[i] > 0)
			{
				if(enemyMissile.testHitShieldAll(x,y))
				{
					pieces[i]--;
					continue;
				}
				if(playerMissile.testHitShieldAll(x,y))
				{
					pieces[i]--;
				}
			}
		}

	}

}

final class msPiggy
{
	public static boolean alive = false;
	public static boolean dying = false;
	public static int dyingCountDown = 0;
	public static int x = 640;
	public static int y = 0;
	public static final int respawnTimeNeeded = 300;
	public static int respawnCounter = respawnTimeNeeded;

	public static int picidx = 0;
	public static int pointValue = 150;

	public static final int chx = -3;

	public msPiggy()
	{
		resetPig();
	}

	public static void resetPig()
	{
		alive = false;
		dying = false;
		dyingCountDown = 0;
		x = 640;
		y = 0;
		respawnCounter = respawnTimeNeeded;
		picidx = 0;
	}

	public static void move()
	{
		if(dying)
		{
			/*System.out.println("Pig Dying");*/
			dyingCountDown--;
			if(dyingCountDown < 1)
			{
				resetPig();
			}
			return;
		}

		if(!alive)
		{
			/*System.out.println("Pig Dead");
			System.out.println("Respawn " + respawnCounter);*/
			respawnCounter--;
			if(respawnCounter < 1)
			{
				alive = true;
			}
			return;
		}

		/*System.out.println("pig alive at : " + x);*/
		x += chx;

		if(x < -36)
		{
			resetPig();
			return;
		}

		picidx = (picidx + 1) % 2;

		if(playerMissile.testAllHit(x,y))
		{
			alive = false;
			dying = true;
			dyingCountDown = 10;
			player.score += pointValue;
			/*System.out.println("Players Score : " + player.score);*/
			player.checkForFreeGuy();
			return;
		}
	}

	public static void drawOn(Graphics g)
	{
		if(dying)
		{
			g.drawImage(crsinvaders.popstarImage,x,y,null);
			return;
		}

		if(!alive)
		{
			return;
		}

		g.drawImage(crsinvaders.bonusPig[picidx],x,y,null);
	}


}

public final class crsinvaders extends Applet implements
	Runnable, KeyListener,MouseListener
{
	int DELAYTIME = 40;

	boolean startgame = false;

	public static Image hoverLeft[] = null;
	public static Image hoverRight[] = null;
	public static Image chairLeft[] = null;
	public static Image chairRight[] = null;
	public static Image footLeft[] = null;
	public static Image footRight[] = null;

	public static Image playerImage[] = null;
	public static Image popstarImage = null;

	public static Image background = null;
	public static Image db = null;

	public static Image rollPhone[] = null;	
	public static Image playerMissileBlip = null;

	public static Image alienPhone[] = null;

	public static Image shieldOverlay = null;
	public static Image shieldImage[] = null;

	public static Image shipSplat[] = null;

	public static Image numShips[] = null;

	public static Image gameOverImage = null;

	public static Image scoreImage = null;
	public static Image numberImages[] = null;

	public static Image bonusPig[] = null;

	public static Image splashImage = null;

	public static Graphics superg = null;

	invader Invaders[] = null;

	public void keyTyped(KeyEvent e)
	{
		switch(e.getKeyChar())
		{
			case ' ':
				player.fire();
				break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
				/* go left
				 */
				player.leftKeyDown = false;
				break;
			case KeyEvent.VK_RIGHT:
				/* go right
				 */
				player.rightKeyDown = false;
				break;
		}
	}

	public void keyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
				/* go left
				 */
				player.leftKeyDown = true;
				break;
			case KeyEvent.VK_RIGHT:
				/* go right
				 */
				player.rightKeyDown = true;
				break;

			default:

				switch(e.getKeyChar())
				{
					case '0':
						DELAYTIME=15;
						break;
					case '9':
						DELAYTIME=20;
						break;
					case '8':
						DELAYTIME=25;
						break;
					case '7':
						DELAYTIME=30;
						break;
					case '6':
						DELAYTIME=35;
						break;
					case '5':
						DELAYTIME=40;
						break;
					case '4':
						DELAYTIME=45;
						break;
					case '3':
						DELAYTIME=50;
						break;
					case '2':
						DELAYTIME=55;
						break;
					case '1':
						DELAYTIME=60;
						break;
			
					case '+':
					case '=':
						DELAYTIME--;
						break;
					case '-':
					case '_':
						DELAYTIME ++;
						break;
				}/*end switch*/
		
				if(DELAYTIME<10)
					DELAYTIME=10;

				break;

		}/*end switch key code.*/
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseClicked(MouseEvent e)
	{
		startgame = true;
		e.consume();
	}

	public void mousePressed(MouseEvent e)
	{
		/*
		System.out.println("Start Game!");
		startgame = true;
		*/
	}

	public crsinvaders()
	{
	}

	public void init()
	{
		hoverLeft = new Image[2];
		hoverRight = new Image[2];
		chairLeft = new Image[2];
		chairRight = new Image[2];
		footLeft = new Image[2];
		footRight = new Image[2];
		playerImage = new Image[2];
		background = null;	

		superg = null;
		addKeyListener(this);
		addMouseListener(this);

		rollPhone = new Image[4];
		alienPhone = new Image[4];

		shieldImage = new Image[3];
		shieldOverlay = null;

		shipSplat = new Image[4];

		numShips = new Image [6];

		gameOverImage = null;

		scoreImage = null;
		numberImages = new Image[10];

		bonusPig = new Image[2];

		splashImage = null;

		Invaders = new invader[55];
	}

	public void start()
	{
		db = createImage(640,480);
		try
		{
			hoverLeft[0] = getImage(getCodeBase(),"images/hovLeft1.gif");
			hoverLeft[1] = getImage(getCodeBase(),"images/hovLeft2.gif");

			hoverRight[0] = getImage(getCodeBase(),"images/hovRight1.gif");
			hoverRight[1] = getImage(getCodeBase(),"images/hovRight2.gif");

			chairLeft[0] = getImage(getCodeBase(),"images/chairLeft1.gif");
			chairLeft[1] = getImage(getCodeBase(),"images/chairLeft2.gif");

			chairRight[0] = getImage(getCodeBase(),"images/chairRight1.gif");
			chairRight[1] = getImage(getCodeBase(),"images/chairRight2.gif");

			footLeft[0] = getImage(getCodeBase(),"images/footLeft1.gif");
			footLeft[1] = getImage(getCodeBase(),"images/footLeft2.gif");

			footRight[0] = getImage(getCodeBase(),"images/images/footRight1.gif");
			footRight[1] = getImage(getCodeBase(),"footRight2.gif");

			background = getImage(getCodeBase(),"images/background.gif");

			playerImage[0] = getImage(getCodeBase(),"images/LogoShipReady.gif");
			playerImage[1] = getImage(getCodeBase(),"images/LogoShipNotReady.gif");
			popstarImage = getImage(getCodeBase(),"images/popstar.gif");

			rollPhone[0] = getImage(getCodeBase(),"images/RollPhone1.gif");
			rollPhone[1] = getImage(getCodeBase(),"images/RollPhone2.gif");
			rollPhone[2] = getImage(getCodeBase(),"images/RollPhone3.gif");
			rollPhone[3] = getImage(getCodeBase(),"images/RollPhone4.gif");

			alienPhone[0] = getImage(getCodeBase(),"images/AlienPhone1.gif");
			alienPhone[1] = getImage(getCodeBase(),"images/AlienPhone2.gif");
			alienPhone[2] = getImage(getCodeBase(),"images/AlienPhone3.gif");
			alienPhone[3] = getImage(getCodeBase(),"images/AlienPhone4.gif");

			playerMissileBlip = getImage(getCodeBase(),"images/missileBlip.gif");

			shieldOverlay = getImage(getCodeBase(),"images/ShieldMask.gif");

			shieldImage[2] = getImage(getCodeBase(),"images/shield1.gif");
			shieldImage[1] = getImage(getCodeBase(),"images/shield2.gif");
			shieldImage[0] = getImage(getCodeBase(),"images/shield3.gif");

			shipSplat[0] = getImage(getCodeBase(),"images/shipSplat1.gif");
			shipSplat[1] = getImage(getCodeBase(),"images/shipSplat2.gif");
			shipSplat[2] = getImage(getCodeBase(),"images/shipSplat3.gif");
			shipSplat[3] = getImage(getCodeBase(),"images/shipSplat4.gif");

			numShips[0] = getImage(getCodeBase(),"images/players1.gif");
			numShips[1] = getImage(getCodeBase(),"images/players2.gif");
			numShips[2] = getImage(getCodeBase(),"images/players3.gif");
			numShips[3] = getImage(getCodeBase(),"images/players4.gif");
			numShips[4] = getImage(getCodeBase(),"images/players5.gif");
			numShips[5] = getImage(getCodeBase(),"images/players6.gif");

			gameOverImage = getImage(getCodeBase(),"images/GameOver.gif");

			scoreImage = getImage(getCodeBase(),"images/Score.gif");
			numberImages[0] = getImage(getCodeBase(),"images/number0.gif");
			numberImages[1] = getImage(getCodeBase(),"images/number1.gif");
			numberImages[2] = getImage(getCodeBase(),"images/number2.gif");
			numberImages[3] = getImage(getCodeBase(),"images/number3.gif");
			numberImages[4] = getImage(getCodeBase(),"images/number4.gif");
			numberImages[5] = getImage(getCodeBase(),"images/number5.gif");
			numberImages[6] = getImage(getCodeBase(),"images/number6.gif");
			numberImages[7] = getImage(getCodeBase(),"images/number7.gif");
			numberImages[8] = getImage(getCodeBase(),"images/number8.gif");
			numberImages[9] = getImage(getCodeBase(),"images/number9.gif");

			bonusPig[0] = getImage(getCodeBase(),"images/bonusPig1.gif");
			bonusPig[1] = getImage(getCodeBase(),"images/bonusPig2.gif");

			splashImage = getImage(getCodeBase(),"images/splashscreen.gif");
			
			System.out.println("Use Web Code Base");

		}
		catch(Exception e)
		{
			
			System.out.println("Use Tool Kit");
			
			Toolkit t = Toolkit.getDefaultToolkit();

			splashImage = t.createImage(getClass().getResource("images/splashscreen.gif"));
			
			hoverLeft[0] = t.createImage(getClass().getResource("images/hovLeft1.gif"));
			hoverLeft[1] = t.createImage(getClass().getResource("images/hovLeft2.gif"));

			hoverRight[0] = t.createImage(getClass().getResource("images/hovRight1.gif"));
			hoverRight[1] = t.createImage(getClass().getResource("images/hovRight2.gif"));

			chairLeft[0] = t.createImage(getClass().getResource("images/chairLeft1.gif"));
			chairLeft[1] = t.createImage(getClass().getResource("images/chairLeft2.gif"));

			chairRight[0] = t.createImage(getClass().getResource("images/chairRight1.gif"));
			chairRight[1] = t.createImage(getClass().getResource("images/chairRight2.gif"));

			footLeft[0] = t.createImage(getClass().getResource("images/footLeft1.gif"));
			footLeft[1] = t.createImage(getClass().getResource("images/footLeft2.gif"));

			footRight[0] = t.createImage(getClass().getResource("images/footRight1.gif"));
			footRight[1] = t.createImage(getClass().getResource("images/footRight2.gif"));

			background = t.createImage(getClass().getResource("images/background.gif"));

			playerImage[0] = t.createImage(getClass().getResource("images/LogoShipReady.gif"));
			playerImage[1] = t.createImage(getClass().getResource("images/LogoShipNotReady.gif"));
			popstarImage = t.createImage(getClass().getResource("images/popstar.gif"));

			rollPhone[0] = t.createImage(getClass().getResource("images/RollPhone1.gif"));
			rollPhone[1] = t.createImage(getClass().getResource("images/RollPhone2.gif"));
			rollPhone[2] = t.createImage(getClass().getResource("images/RollPhone3.gif"));
			rollPhone[3] = t.createImage(getClass().getResource("images/RollPhone4.gif"));

			alienPhone[0] = t.createImage(getClass().getResource("images/AlienPhone1.gif"));
			alienPhone[1] = t.createImage(getClass().getResource("images/AlienPhone2.gif"));
			alienPhone[2] = t.createImage(getClass().getResource("images/AlienPhone3.gif"));
			alienPhone[3] = t.createImage(getClass().getResource("images/AlienPhone4.gif"));

			playerMissileBlip = t.createImage(getClass().getResource("images/missileBlip.gif"));

			shieldOverlay = t.createImage(getClass().getResource("images/ShieldMask.gif"));
			shieldImage[2] = t.createImage(getClass().getResource("images/shield1.gif"));
			shieldImage[1] = t.createImage(getClass().getResource("images/shield2.gif"));
			shieldImage[0] = t.createImage(getClass().getResource("images/shield3.gif"));

			shipSplat[0] = t.createImage(getClass().getResource("images/shipSplat1.gif"));
			shipSplat[1] = t.createImage(getClass().getResource("images/shipSplat2.gif"));
			shipSplat[2] = t.createImage(getClass().getResource("images/shipSplat3.gif"));
			shipSplat[3] = t.createImage(getClass().getResource("images/shipSplat4.gif"));

			numShips[0] = t.createImage(getClass().getResource("images/players1.gif"));
			numShips[1] = t.createImage(getClass().getResource("images/players2.gif"));
			numShips[2] = t.createImage(getClass().getResource("images/players3.gif"));
			numShips[3] = t.createImage(getClass().getResource("images/players4.gif"));
			numShips[4] = t.createImage(getClass().getResource("images/players5.gif"));
			numShips[5] = t.createImage(getClass().getResource("images/players6.gif"));

			gameOverImage = t.createImage(getClass().getResource("images/GameOver.gif"));

			scoreImage = t.createImage(getClass().getResource("images/Score.gif"));
			numberImages[0] = t.createImage(getClass().getResource("images/number0.gif"));
			numberImages[1] = t.createImage(getClass().getResource("images/number1.gif"));
			numberImages[2] = t.createImage(getClass().getResource("images/number2.gif"));
			numberImages[3] = t.createImage(getClass().getResource("images/number3.gif"));
			numberImages[4] = t.createImage(getClass().getResource("images/number4.gif"));
			numberImages[5] = t.createImage(getClass().getResource("images/number5.gif"));
			numberImages[6] = t.createImage(getClass().getResource("images/number6.gif"));
			numberImages[7] = t.createImage(getClass().getResource("images/number7.gif"));
			numberImages[8] = t.createImage(getClass().getResource("images/number8.gif"));
			numberImages[9] = t.createImage(getClass().getResource("images/number9.gif"));

			bonusPig[0] = t.createImage(getClass().getResource("images/bonusPig1.gif"));
			bonusPig[1] = t.createImage(getClass().getResource("images/bonusPig2.gif"));

		}
		MediaTracker MT;
		MT = new MediaTracker(this);

		MT.addImage(hoverLeft[0],0);
		MT.addImage(hoverLeft[1],0);
		MT.addImage(hoverRight[0],0);
		MT.addImage(hoverRight[1],0);
		MT.addImage(chairLeft[0],0);
		MT.addImage(chairLeft[1],0);
		MT.addImage(chairRight[0],0);
		MT.addImage(chairRight[1],0);
		MT.addImage(footLeft[0],0);
		MT.addImage(footLeft[1],0);
		MT.addImage(footRight[0],0);
		MT.addImage(footRight[1],0);

		MT.addImage(background,0);
	
		MT.addImage(playerImage[0],0);
		MT.addImage(playerImage[1],0);
		MT.addImage(popstarImage,0);

		MT.addImage(rollPhone[0],0);
		MT.addImage(rollPhone[1],0);
		MT.addImage(rollPhone[2],0);
		MT.addImage(rollPhone[3],0);

		MT.addImage(alienPhone[0],0);
		MT.addImage(alienPhone[1],0);
		MT.addImage(alienPhone[2],0);
		MT.addImage(alienPhone[3],0);

		MT.addImage(playerMissileBlip,0);

		MT.addImage(shieldOverlay,0);

		MT.addImage(shieldImage[0],0);
		MT.addImage(shieldImage[1],0);
		MT.addImage(shieldImage[2],0);
		
		MT.addImage(shipSplat[0],0);
		MT.addImage(shipSplat[1],0);
		MT.addImage(shipSplat[2],0);
		MT.addImage(shipSplat[3],0);

		MT.addImage(numShips[0],0);
		MT.addImage(numShips[1],0);
		MT.addImage(numShips[2],0);
		MT.addImage(numShips[3],0);
		MT.addImage(numShips[4],0);
		MT.addImage(numShips[5],0);

		MT.addImage(gameOverImage,0);
	
		MT.addImage(bonusPig[0],0);
		MT.addImage(bonusPig[1],0);
		MT.addImage(splashImage,0);

		try{MT.waitForAll();}
		catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Got the Goods");

		System.out.println("About to Start!");

		Thread t = new Thread(this);

		t.start();

	}

	public void fastpaint()
	{

		superg = getGraphics();
		Graphics dbg = db.getGraphics();
		drawScreen(dbg);

		/*dbg = sp.getGraphics(); score panel*/

		/*drawScorePanel(dbg); render score panel to buffer*/

		superg.drawImage(db,0,0,null);
		/*superg.drawImage(sp,0,350,null); score panel draw*/
	}

	public void drawScreen(Graphics g)
	{
		g.drawImage(background,0,0,null);

		g.drawImage(scoreImage,4,14,null);
		int the_score = player.score;
		int x;
		int i;
		for(x = 189,i = 0; i < 6; i++, x -= 14,the_score = the_score / 10)
		{
			g.drawImage(crsinvaders.numberImages[the_score % 10],x,14,null);
		}


		if(player.lives < 1)
		{
			g.drawImage(crsinvaders.gameOverImage,109,189,null);
			return;
		}

		switch(player.lives)
		{
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				int index = player.lives - 1;
				if(index > 5)
				{
					index = 5;
				}
				g.drawImage(numShips[index],553,4,null);
				break;
			default:
				/* No players left */
				break;
		}

		shield.drawOnAll(g);

		enemyMissile.drawOnAll(g);
		
		for(i = 0 ; i < 55 ; i++)
		{
			Invaders[i].drawOn(g);
		}

		playerMissile.drawOnAll(g);

		msPiggy.drawOn(g);
		player.drawOn(g);
	}

	public void resetAliens()
	{
		int i;
		int x = 0;
		int y = 0;

		invader.resetCommonProperties();

		for(i = 44 , x = 10, y = 45; i < 55 ; i++, x += 45)
		{
			Invaders[i] = new invader(this,hoverLeft[0],hoverLeft[1],hoverRight[0],hoverRight[1],x,y,40);
		}
		for(i = 33 , x = 10, y = 96; i < 44 ; i++, x += 45)
		{
			Invaders[i] = new invader(this,chairLeft[0],chairLeft[1],chairRight[0],chairRight[1],x,y,25);
		}
		for(i = 22 , x = 10, y = 147; i < 33 ; i++, x += 45)
		{
			Invaders[i] = new invader(this,chairLeft[0],chairLeft[1],chairRight[0],chairRight[1],x,y,20);
		}
		for(i = 11 , x = 10, y = 198; i < 22 ; i++, x += 45)
		{
			Invaders[i] = new invader(this,footLeft[0],footLeft[1],footRight[0],footRight[1],x,y,15);
		}
		for(i = 0 , x = 10, y = 249; i < 11 ; i++, x += 45)
		{
			Invaders[i] = new invader(this,footLeft[0],footLeft[1],footRight[0],footRight[1],x,y,10);
		}
	}

	public void paint(Graphics g)
	{
		if(g != null)
		{
			superg = g; /*once we know it is valid then we*/
			/*can call get graphics on the applet*/
			/*any time to get a valid graphics context*/
		}
		else
			return;

		if(db == null)
		{
			
			System.out.println("Double Buffer is null");
			return;
		}

		Graphics dbg = db.getGraphics();

		if(dbg == null)
			return;

		/*splashpaint(true); Splash Screen*/
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void run()
	{
		this.requestFocus();
		while(superg == null)
		{
			try
			{
				Thread.sleep(50);
			}
			catch(Exception e)
			{
			}
		
			repaint(0);
		}
		for(;;)
		{
			splashScreen(); /*Splash Screen*/
			simpleloop();/*game play loop*/
		}
	}

	public void splashScreen()
	{
		startgame = false;

		while(!startgame)
		{
			superg = getGraphics();
			Graphics dbg = db.getGraphics();
			dbg.drawImage(crsinvaders.splashImage,0,0,null);
			superg.drawImage(db,0,0,null);
			try
			{
				Thread.sleep(200);
			}
			catch(Exception e)
			{
			}
		}
	}

	public void simpleloop()
	{	
		msPiggy mp = new msPiggy();

		invader.resetGameLevel();
		resetAliens();
		player.reset();
		playerMissile.resetMissileQue();
		enemyMissile.resetMissileQue();
		shield.resetShields();

		int i;
		for(;;)
		{
			invader.stepInvaders();

			/* move missiles */
			playerMissile.moveAll();

			/* move invaders */
			for(i = 0; i < 55 && ( invader.doMove || invader.dropDown); i++)
			{
				Invaders[i].move();
			}

			/*Check Squash and advance and do squash maneuver if needed */
			if(invader.squash)
			{
				player.alive = false;
				for(i = 0; i < 55 ; i++ )
				{
					Invaders[i].dropSquash();
				}
				fastpaint();
				try
				{Thread.sleep(4000);}
				catch(Exception e){}
				player.lives = 0;
				fastpaint();
				break;

			}

			enemyMissile.moveAll();

			msPiggy.move();

			shield.moveAll();

			player.move();

			fastpaint();

			invader.afterInvaders();
			try
			{Thread.sleep(DELAYTIME);}
			catch(Exception e){}

			if(invader.livingInvaders < 1)
			{
				/* Raise Level - adjust stepper values reset player shields and queues*/
				System.out.println("Level Cleared");
				try
				{Thread.sleep(1000);}
				catch(Exception e){}
				msPiggy.resetPig();
				invader.increaseGameLevel();
				playerMissile.resetMissileQue();
				enemyMissile.resetMissileQue();
				resetAliens();
				shield.resetShields();
				player.replacePlayer(false);
			}
			else
			{
				invader.adjustLevel();
			}
				

			/*check end of lives here*/
			if(!player.alive)
			{
				for(i = 0 ; i < 40 ; i++)
				{
					fastpaint();
					try
					{Thread.sleep(40);}
					catch(Exception e){}
				}
				System.out.println("Player has been hit");
				/* Clear missile queues*/
				playerMissile.resetMissileQue();
				enemyMissile.resetMissileQue();
				if(!player.replacePlayer(true))
				{
					fastpaint();
					System.out.println("Game over - out of lives");
					break;
				}
				
			}
		}

		/* Make this go to a game over screen */
		try
		{Thread.sleep(5000);}
		catch(Exception e){}

	}

	public static void main(String args[])
	{
		
		String D=new String("CRS Invaders");

		System.out.println(D);

		crsinvaders P = new crsinvaders();

		Frame f = new Frame(D);
		Dimension dim = new Dimension((640+10),(480+30));
		f.setSize(dim);

		f.add("Center",P);
		f.setVisible(true);

		f.addWindowListener(new WindowListener()
		{
			public void windowActivated(WindowEvent e){} 
			public void windowClosed(WindowEvent e){}
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
			public void windowDeactivated(WindowEvent e){}
			public void windowDeiconified(WindowEvent e){}
			public void windowIconified(WindowEvent e){}
			public void windowOpened(WindowEvent e){}
		});

		P.init();
		P.start();
	}

}