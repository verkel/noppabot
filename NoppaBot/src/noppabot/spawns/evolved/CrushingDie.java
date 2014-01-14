/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns.evolved;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.dice.HumongousDie;


// Upgrade
public class CrushingDie extends EvolvedPowerup {
	private static final int dmgSides = 30;
	private static final int humongousDmgSides = 10;
	private static final int humongousDmgBonus = 20;
	
	private boolean humongous;
	private INoppaBot bot;
	
	public CrushingDie(BasicPowerup base, boolean humongous) {
		super(base);
		this.bot = base.bot();
		this.humongous = humongous;
	}

	@Override
	public int onContestRoll() {
		int roll = roll();
		if (humongous) return HumongousDie.doContestRoll(this, owner());
		else {
			bot.sendChannelFormat("%s drops the crushing die and the ground trembles. %s! %s", 
				ownerColored(), resultStr(roll), bot.grade(roll));
			return roll;
		}
	}
	
	@Override
	public int onOpponentRoll(String opponent, int roll) {
		if (humongous) return doHumongousCrush(bot, owner(), opponent, roll);
		else return doStandardCrush(bot, owner(), opponent, roll);
	}

	private int doStandardCrush(INoppaBot bot, String owner, String opponent, int roll) {
		int damage = Powerups.powerupRnd.nextInt(dmgSides) + 1;
		int result = roll - damage;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s's %s crushes the opposition! %s's roll takes %d damage and drops down to %s.", 
			owner, name(), ColorStr.nick(opponent), damage, resultStr);
		return result;
	}
	
	private int doHumongousCrush(INoppaBot bot, String owner, String opponent, int roll) {
		int damageRoll = Powerups.powerupRnd.nextInt(humongousDmgSides) + 1;
		int totalDamage = damageRoll + humongousDmgBonus;
		int result = roll - totalDamage;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s's %s pulverizes the opposition! %s's roll takes %d + %d = %d damage and drops down to %s.", 
			owner, name(), ColorStr.nick(opponent), damageRoll, humongousDmgBonus, totalDamage, resultStr);
		return result;
	}
	
	@Override
	public String name() {
		return humongous ? "HUMONGOUS CRUSHING DIE" : "Crushing Die";
	}
	
	@Override
	public String getUpgradeDescription() {
		if (humongous) return String.format("It now deals d%d + %d " +
			"crushing damage to others' rolls!", humongousDmgSides, humongousDmgBonus);
		else return String.format("It loses its bonus, but now deals d%d " +
			"crushing damage to others' rolls!", dmgSides);
	}
}
