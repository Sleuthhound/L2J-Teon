/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillNeedCharge extends L2Skill
{
	final int numCharges;
	final int chargeSkillId;

	public L2SkillNeedCharge(StatsSet set)
	{
		super(set);
		numCharges = set.getInteger("num_charges", getLevel());
		chargeSkillId = set.getInteger("charge_skill_id");
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		// get the effect
		EffectCharge effect = (EffectCharge) activeChar.getFirstEffect(chargeSkillId);
		if (effect == null || effect.numCharges < numCharges)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(getId());
			activeChar.sendPacket(sm);
			return;
		}
		// decrease?
		effect.numCharges -= numCharges;
		// update icons
		activeChar.updateEffectIcons();
		// maybe exit? no charge
		if (effect.numCharges == 0)
			effect.exit();
		// apply effects
		if (hasEffects())
			for (L2Object target : targets)
				getEffects(activeChar, (L2Character) target);
	}
}
