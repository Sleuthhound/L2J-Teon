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
package ai.group;

import ai.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;
import net.sf.l2j.gameserver.util.Util;

public class Monastery extends L2AttackableAIScript
{
	private static final int[] mobs1 =
	{
		22124,
		22125,
		22126,
		22127,
		22129
	};
	
	private static final int[] mobs2 =
	{
		22134,
		22135
	};
	
	public Monastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(mobs1, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPAWN, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(mobs2, QuestEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!npc.isInCombat() && npc.getTarget() == null)
		{
			if (player.getActiveWeaponInstance() != null)
			{
				npc.setTarget(player);
				npc.broadcastNpcSay("Brother " + player.getName() + ", move your weapon away!!");
				switch (npc.getNpcId())
				{
					case 22124:
					case 22126:
					{
						npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
						break;
					}
					default:
					{
						npc.setIsRunning(true);
						((L2Attackable) npc).addDamageHate(player, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						break;
					}
				}
			}
			else if (((L2Attackable) npc).getMostHated() == null)
				return null;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getSkillType() == L2SkillType.AGGDAMAGE && targets.length != 0)
		{
			for (L2Object obj : targets)
			{
				if (obj.equals(npc))
				{
					npc.broadcastNpcSay("Brother " + caster.getName() + ", move your weapon away!!");
					((L2Attackable) npc).addDamageHate(caster, 0, 999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
					break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		List<L2Playable> result = new ArrayList<>();
		for (L2Object obj : npc.getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2PcInstance || obj instanceof L2PetInstance)
			{
				if (Util.checkIfInRange(npc.getAggroRange(), npc, obj, true) && !((L2Character) obj).isDead())
					result.add((L2Playable) obj);
			}
		}
		
		if (!result.isEmpty())
		{
			Object[] characters = result.toArray();
			for (Object obj : characters)
			{
				L2PcInstance target = ((L2Playable) obj).getActingPlayer();
				if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null)
				{
					npc.setTarget(target);
					npc.broadcastNpcSay("Brother " + target.getName() + ", move your weapon away!!");
					switch (npc.getNpcId())
					{
						case 22124:
						case 22126:
						case 22127:
						{
							L2Skill skill = SkillTable.getInstance().getInfo(4589, 8);
							npc.doCast(skill);
							break;
						}
						default:
						{
							npc.setIsRunning(true);
							((L2Attackable) npc).addDamageHate(target, 0, 999);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							break;
						}
					}
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (skill.getId() == 4589)
		{
			npc.setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	public static void main(String[] args)
	{
		new Monastery(-1, "Monastery", "ai");
	}
}