package resources;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;

import graphics.sprites.SheetDeets;
import graphics.sprites.SpriteSheet;

public class Character extends Observable implements Collidable {
	private static final double default_mass = 1.0;
	private static final int default_radius = 20;
	private static final double default_max_speed_x = 5;
	private static final double default_max_speed_y = 5;
	private static final double default_acc = 0.2;
	private static final double default_restitution = 0.7; // 'bounciness'

	public enum Heading {
		N, E, S, W, NE, NW, SE, SW
	};

	// this will have all the Character classes in use.
	public enum Class {
		DEFAULT, WIZARD, ELF, TEST
	}; // add to this as we develop more classes.

	// flags for keys pressed.
	// e.g. if up is true, then the player/ai is holding the up button.
	// jump is currently not being used, but is there if we need it.
	// jump punch and/or block may be replaced by a single 'special' flag,
	// which does an action based on the class of the character.
	// Collided flag added to help with collision calculations (depreciated)
	private boolean up, right, left, down, jump, punch, block, collided = false;

	// these are for the physics engine. Restitution is 'bounciness'.
	private double mass, inv_mass, dx, dy, maxdx, maxdy, acc, restitution = 0.0;

	// these are for the physics engine and the graphics engine.
	// Characters are circles.
	// x and y are the coordinates of the centre of the circle, relative to the
	// top-left of the arena.
	// radius is the radius of the circle, in arbitrary units.
	// direction is the direction that the character's facing
	// (this is entirely for graphics)
	private double x, y = 0.0;
	private int radius = 0;
	private Heading direction = Heading.N;
	private Class classType = Class.DEFAULT;

	// variables imported from CharacterModel
	private SpriteSheet spriteSheet;
	private ArrayList<BufferedImage> rollingSprites, directionSprites;
	private int rollingFrame, directionFrame;
	private boolean moving;

	/**
	 * Default character with default sprite
	 */

	public Character() {
		this(default_mass, 0, 0, default_radius, Heading.N, Class.DEFAULT);
	}

	/**
	 * Default character with a given class
	 * 
	 * @param c
	 *            the class
	 */

	public Character(Class c) {
		this(default_mass, 0, 0, SheetDeets.getRadiusFromSprite(c), Heading.N, c);
	}

	public Character(double mass, double x, double y, int radius, Heading direction, Class classType) {
		this(false, false, false, false, false, false, false, // control flags
				mass, x, // x
				y, // y
				0.0, // speed_x
				0.0, // speed_y
				default_max_speed_x * (1 / mass), default_max_speed_y * (1 / mass), default_acc, // acceleration
																									// (TODO:
																									// calculate
																									// this)
				default_restitution, radius, direction, classType);
	}

	// master constructor. Any other constructors should eventually call this.
	private Character(boolean up, boolean right, boolean left, boolean down, boolean jump, boolean punch, boolean block,
			double mass, double x, double y, double speed_x, double speed_y, double max_speed_x, double max_speed_y,
			double acceleration, double restitution, int radius, Heading direction, Class classType) {
		// new Character();
		this.up = up;
		this.right = right;
		this.left = left;
		this.down = down;
		this.jump = jump;
		this.punch = punch;
		this.block = block;

		this.mass = mass;
		if (mass == 0)
			this.inv_mass = 0; // a mass of 0 makes an object infinitely massive
		else
			this.inv_mass = 1.0 / mass;

		this.x = x;
		this.y = y;
		this.dx = speed_x;
		this.dy = speed_y;
		this.maxdx = max_speed_x;
		this.maxdy = max_speed_y;
		this.acc = acceleration;
		this.restitution = restitution; // bounciness
		this.radius = radius;
		this.direction = direction;
		this.classType = classType;

		// imported from graphics.
		this.spriteSheet = SheetDeets.getSpriteSheetFromCharacter(this);
		this.moving = false;

		rollingSprites = new ArrayList<BufferedImage>();
		directionSprites = new ArrayList<BufferedImage>();
		
		ArrayList<int[][]> sections = spriteSheet.getSections();
		int[][] rollingSpriteLocs = sections.get(0);
		int[][] directionSpriteLocs = sections.get(1);

		for (int i = 0; i < rollingSpriteLocs.length; i++) {
			BufferedImage sprite = spriteSheet.getSprite(rollingSpriteLocs[i][0], rollingSpriteLocs[i][1]);
			rollingSprites.add(sprite);
			rollingSprites.add(sprite);
		}

		for (int i = 0; i < directionSpriteLocs.length; i++) {
			BufferedImage sprite = spriteSheet.getSprite(directionSpriteLocs[i][0], rollingSpriteLocs[i][1]);
			directionSprites.add(sprite);
			directionSprites.add(sprite);
		}

		rollingFrame = 0;
		directionFrame = 0;

	}

	/**
	 * Get the current rolling frame
	 * 
	 * @return the frame
	 */

	public BufferedImage getNextFrame(boolean moving) {
		switch (classType) {
		case TEST:
			if (moving) {
				switch (direction) {
				case N:
					directionFrame = 0;
					break;
				case NE:
					directionFrame = 1;
					break;
				case E:
					directionFrame = 2;
					break;
				case SE:
					directionFrame = 3;
					break;
				case S:
					directionFrame = 4;
					break;
				case SW:
					directionFrame = 5;
					break;
				case W:
					directionFrame = 6;
					break;
				case NW:
					directionFrame = 7;
					break;
				}
			}

			return this.directionSprites.get(directionFrame);

		case ELF:
		case WIZARD:
		case DEFAULT:
			if (moving) {
				switch (direction) {
				case W:
				case NW:
				case SW:
				case N:
					rollingFrame--;
					break;
				case E:
				case NE:
				case SE:
				case S:
					rollingFrame++;
					break;
				}
				if (rollingFrame == 16)
					rollingFrame = 0;

				if (rollingFrame == -1)
					rollingFrame = 15;
			}

			return this.rollingSprites.get(rollingFrame);
		}

		return null;
	}

	/*
	 * Testing methods Should not be used in the final demo
	 */

	/*
	 * private void update() { velX = 0; velY = 0;
	 * 
	 * if (character.isDown()) velY = SPEED; if (character.isUp()) velY =
	 * -SPEED; if (character.isLeft()) velX = -SPEED; if (character.isRight())
	 * velX = SPEED;
	 * 
	 * }
	 */

	/**
	 * Move the character (TESTING)
	 */

	// public void move(){
	// setX(getX() + velX);
	// /setY(getY() + velY);
	// }

	/*
	 * Getters and setters for controls: this is mportant for determining which
	 * frame of the sprite to use next
	 */

	/**
	 * Is an up command being received?
	 * 
	 * @return up?
	 */

	public boolean isUp() {
		return this.up;
	}

	/**
	 * Is a down command being received?
	 * 
	 * @return down?
	 */

	public boolean isDown() {
		return this.down;
	}

	/**
	 * Is a left command being received?
	 * 
	 * @return left?
	 */

	public boolean isLeft() {
		return this.left;
	}

	/**
	 * Is a right command being received?
	 * 
	 * @return right?
	 */

	public boolean isRight() {
		return this.right;
	}

	/**
	 * Is the character jumping?
	 * 
	 * @return is the character jumping?
	 */

	public boolean isJump() {
		return this.jump;
	}

	/**
	 * Is the character punching?
	 * 
	 * @return is the character punching?
	 */

	public boolean isPunch() {
		return this.punch;
	}

	/**
	 * Is the character blocking?
	 * 
	 * @return is the character blocking?
	 */

	public boolean isBlock() {
		return this.block;
	}

	/**
	 * Set if an up command is being received
	 * 
	 * @param up
	 *            up?
	 */

	public void setUp(boolean up) {
		this.up = up;
		setDirection();
	}

	/**
	 * Set if a down command is being received
	 * 
	 * @param down
	 *            down?
	 */

	public void setDown(boolean down) {
		this.down = down;
		setDirection();
	}

	/**
	 * Set if a left command is being received
	 * 
	 * @param left
	 *            left?
	 */

	public void setLeft(boolean left) {
		this.left = left;
		setDirection();
	}

	/**
	 * Set if a right command is being received
	 * 
	 * @param right
	 *            right?
	 */

	public void setRight(boolean right) {
		this.right = right;
		setDirection();
	}

	/**
	 * Set if a jump command is being received
	 * 
	 * @param jump
	 *            is a jump command being received?
	 */

	public void setJump(boolean jump) {
		this.jump = jump;

	}

	/**
	 * Set if a punch command is being received
	 * 
	 * @param punch
	 *            is a punch command being received?
	 */

	public void setPunch(boolean punch) {
		this.punch = punch;
	}

	/**
	 * Set if a block command is being received
	 * 
	 * @param block
	 *            is a block command being received?
	 */

	public void setBlock(boolean block) {
		this.block = block;
	}

	/**
	 * Set the direction of the character based on the commands it is currently
	 * receiving
	 */

	private void setDirection() {

		if (isUp()) {
			if (isLeft()) {
				direction = Heading.NW;
			}

			else if (isRight()) {
				direction = Heading.NE;
			}

			else {
				direction = Heading.N;
			}
		} else if (isDown()) {
			if (isLeft()) {
				direction = Heading.SW;
			}

			else if (isRight()) {
				direction = Heading.SE;
			}

			else {
				direction = Heading.S;
			}
		} else if (isLeft()) {
			direction = Character.Heading.W;
		} else if (isRight()) {
			direction = Character.Heading.E;
		}

		if (getDx() != 0 || getDy() != 0) {
			setMoving(true);
		} else {
			setMoving(false);
		}

		setDirection(direction);

		// update();

		setChanged();
		notifyObservers();
	}

	/*
	 * Moving getters and setters: used for knowing when to generate the next
	 * frame of the sprite
	 */

	/**
	 * Set if the character is moving
	 * 
	 * @param moving
	 *            moving?
	 */

	public void setMoving(boolean moving) {

		this.moving = moving;
	}

	/**
	 * Get if the character is moving
	 * 
	 * @return moving?
	 */

	public boolean isMoving() {
		return this.moving;
	}

	/*
	 * Setters and getters for position: these are important for knowing where
	 * to draw the character
	 */

	/**
	 * Get the x coordinate of a character
	 * 
	 * @return the x coordinate
	 */

	public double getX() {
		return this.x;
	}

	/**
	 * Get the y coordinate of a character
	 * 
	 * @return the y coordinate
	 */

	public double getY() {
		return this.y;
	}

	/**
	 * Has the character collided?
	 * 
	 * @return if the character has collided
	 */

	public boolean isCollided() {
		return this.collided;
	}

	/**
	 * Get the facing of this character
	 * 
	 * @return the facing
	 */

	public Character.Heading getDirection() {
		return this.direction;
	}

	/**
	 * Set the x coordinate of the character
	 * 
	 * @param x
	 *            the x coordinate
	 */

	public void setX(double x) {
		this.x = x;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the y coordinate of the character
	 * 
	 * @param y
	 *            the y coordinate
	 */

	public void setY(double y) {

		this.y = y;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set if the character has collided
	 * 
	 * @param collided
	 *            if the character has collided
	 */

	public void setCollided(boolean collided) {
		this.collided = collided;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the facing of the character
	 * 
	 * @param direction
	 */

	public void setDirection(Character.Heading direction) {
		this.direction = direction;
	}

	/*
	 * Setters and getters for character physics: may be important later on
	 */

	/**
	 * Get the mass of the character
	 * 
	 * @return the mass
	 */

	public double getMass() {
		return this.mass;
	}

	/**
	 * Get the 'inv mass' of the character
	 * 
	 * @return the inv mass
	 */

	public double getInvMass() {
		return this.inv_mass;
	}

	/**
	 * Get the dx of the character
	 * 
	 * @return the dx
	 */

	public double getDx() {
		return this.dx;
	}

	/**
	 * Get the dy of the character
	 * 
	 * @return
	 */

	public double getDy() {
		return this.dy;
	}

	/**
	 * Get the max dx of the character
	 * 
	 * @return the max dx
	 */

	public double getMaxDx() {
		return this.maxdx;
	}

	/**
	 * Get the max dy of the character
	 * 
	 * @return the max dy
	 */

	public double getMaxDy() {
		return this.maxdy;
	}

	/**
	 * Get the acceleration of the character
	 * 
	 * @return the acceleration
	 */

	public double getAcc() {
		return this.acc;
	}

	/**
	 * Get the restitution of the character
	 * 
	 * @return the restitution
	 */

	public double getRestitution() {
		return this.restitution;
	}

	/**
	 * Get the radius of the character
	 * 
	 * @return the radius
	 */

	public int getRadius() {
		return this.radius;
	}

	/**
	 * Set the mass of a character
	 * 
	 * @param mass
	 *            the mass
	 */

	public void setMass(double mass) {
		this.mass = mass;
	}

	/**
	 * Set the dx of a character
	 * 
	 * @param dx
	 *            the dx
	 */

	public void setDx(double dx) {
		this.dx = dx;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the dy of a character
	 * 
	 * @param dy
	 *            the dy
	 */

	public void setDy(double dy) {
		this.dy = dy;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the max dx of a character
	 * 
	 * @param maxDx
	 *            the max dx
	 */

	public void setMaxDx(double maxDx) {
		this.maxdx = maxDx;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the max dy of a character
	 * 
	 * @param maxDy
	 *            the max dy
	 */

	public void setMaxDy(double maxDy) {
		this.maxdy = maxDy;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the acceleration of a character
	 * 
	 * @param acceleration
	 *            the acceleration
	 */

	public void setAcc(double acceleration) {
		this.acc = acceleration;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the restitution of a character
	 * 
	 * @param restitution
	 *            the restitution
	 */

	public void setRestitution(double restitution) {
		this.restitution = restitution;
		setChanged();
		notifyObservers();
	}

	/**
	 * Set the radius of a character
	 * 
	 * @param radius
	 *            the radius
	 */

	public void setRadius(int radius) {
		this.radius = radius;
		setChanged();
		notifyObservers();
	}

	// setters/getters for control flags
	/**
	 * Set all of the control flags at once.
	 * 
	 * @param up
	 * @param down
	 * @param left
	 * @param right
	 * @param jump
	 * @param punch
	 * @param block
	 */
	public void setControls(boolean up, boolean down, boolean left, boolean right, boolean jump, boolean punch,
			boolean block) {
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
		this.jump = jump;
		this.punch = punch;
		this.block = block;
		setChanged();
		notifyObservers();
	}

	/**
	 * Get the class type of this character.
	 * 
	 * @return
	 */
	public Class getClassType() {
		return this.classType;
	}

}
