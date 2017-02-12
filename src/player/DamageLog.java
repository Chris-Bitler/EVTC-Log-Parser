package player;

import enums.Activation;
import enums.Result;
import enums.StateChange;

public class DamageLog {

	// Fields
	private int time;
	private int damage;
	private int skill_id;
	private boolean is_condi;
	private Result result;
	private boolean is_ninety;
	private boolean is_moving;
	private StateChange is_statechange;
	private Activation is_activation;

	// Constructor
	public DamageLog(int time, int damage, int skill_id, boolean is_condi, Result result, boolean is_ninety,
			boolean is_moving, StateChange is_statechange, Activation activation) {
		this.time = time;
		this.damage = damage;
		this.skill_id = skill_id;
		this.is_condi = is_condi;
		this.result = result;
		this.is_ninety = is_ninety;
		this.is_moving = is_moving;
		this.is_statechange = is_statechange;
		this.is_activation = activation;
	}

	// Getters
	public int getTime() {
		return time;
	}

	public int getDamage() {
		return damage;
	}

	public int getID() {
		return skill_id;
	}

	public boolean is_condi() {
		return is_condi;
	}

	public Result get_result() {
		return result;
	}

	public boolean is_ninety() {
		return is_ninety;
	}

	public boolean is_moving() {
		return is_moving;
	}

	public StateChange is_statechange() {
		return is_statechange;
	}

	public Activation is_activation() {
		return is_activation;
	}

}
