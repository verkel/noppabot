/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns.evolved;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.dice.HumongousDie;


// Upgrade
public class CrushingDie extends EvolvedDie {
	private static final int dmgSides = 25;
	private static final int humongousDmgSides = 10;
	private static final int humongousDmgBonus = 15;
	
	private boolean humongous;
	private INoppaBot bot;
	
	public CrushingDie(BasicDie base, boolean humongous) {
		super(base);
		this.bot = base.bot();
		this.humongous = humongous;
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		if (humongous) return HumongousDie.doContestRoll(this, owner());
		else {
			bot.sendChannelFormat("%s drops the crushing die and the ground trembles. %s! %s", 
				ownerColored(), resultStr(roll), bot.grade(roll));
			return roll;
		}
	}
	
	@Override
	public Roll onOpponentRoll(String opponent, Roll roll) {
		if (humongous) return doHumongousCrush(bot, owner(), opponent, (DiceRoll)roll);
		else return doStandardCrush(bot, owner(), opponent, (DiceRoll)roll);
	}

	private DiceRoll doStandardCrush(INoppaBot bot, String owner, String opponent, DiceRoll roll) {
		int damage = Powerups.powerupRnd.nextInt(dmgSides) + 1;
		DiceRoll result = roll.sub(damage).clamp();
		bot.sendChannelFormat("%s's %s crushes the opposition! %s's roll takes %s damage and drops down to %s.", 
			owner, name(), Color.nick(opponent), damage, crushResultStr(opponent, result));
		return result;
	}
	
	private DiceRoll doHumongousCrush(INoppaBot bot, String owner, String opponent, DiceRoll roll) {
		int damageRoll = Powerups.powerupRnd.nextInt(humongousDmgSides) + 1;
		int totalDamage = damageRoll + humongousDmgBonus;
		DiceRoll result = roll.sub(totalDamage).clamp();
		bot.sendChannelFormat("%s's %s pulverizes the opposition! %s's roll takes %s + %s = %s damage and drops down to %s.", 
			owner, name(), Color.nick(opponent), damageRoll, humongousDmgBonus, totalDamage, crushResultStr(opponent, result));
		return result;
	}
	
	private String crushResultStr(String opponent, DiceRoll result) {
		return result.toResultString(bot.rollCanParticipate(opponent), false, bot);
	}
	
	@Override
	public String name() {
		return humongous ? "HUMONGOUS CRUSHING DIE" : "Crushing Die";
	}
	
	@Override
	public String getUpgradeDescription() {
		if (humongous) return String.format("It now deals d%s + %s " +
			"crushing damage to others' rolls!", humongousDmgSides, humongousDmgBonus);
		else return String.format("It loses its bonus, but now deals d%s " +
			"crushing damage to others' rolls!", dmgSides);
	}
}
