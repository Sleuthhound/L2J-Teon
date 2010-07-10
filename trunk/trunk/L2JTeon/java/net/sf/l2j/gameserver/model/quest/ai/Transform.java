/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.quest.ai;

import java.util.ArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.util.Rnd;

public class Transform extends Quest implements Runnable
{
	private ArrayList<Transformer> _mobs = new ArrayList<Transformer>();

	private static class Transformer
	{
		private int _id;
		private int _idPoly;
		private int _chance;
		private int _message;

		private Transformer(int id, int idPoly, int chance, int message)
		{
			_id = id;
			_idPoly = idPoly;
			_chance = chance;
			_message = message;
		}
		
		private int getId()
		{
			return _id;
		}
		
		private int getIdPoly()
		{
			return _idPoly;
		}

		private int getChance()
		{
			return _chance;
		}
		
		private int getMessage()
		{
			return _message;
		}
	}

	private static String [] Message =
	{
		"I cannot despise the fellow! I see his sincerity in the duel.",
		"Nows we truly begin!",
		"Fool! Right now is only practice!",
		"Have a look at my true strength.",
		"This time at the last! The end!"
	};

	public Transform(int questId, String name, String descr)
    {
		super(questId, name, descr);
		
		_mobs.add(new Transformer(21261,21262,1,5));   // 1st mutation Ol Mahum Transcender
		_mobs.add(new Transformer(21262,21263,1,5));   // 2st mutation Ol Mahum Transcender
		_mobs.add(new Transformer(21263,21264,1,5));   // 3rd mutation Ol Mahum Transcender
		_mobs.add(new Transformer(21258,21259,100,5)); // always mutation on atk Fallen Orc Shaman
		_mobs.add(new Transformer(20835,21608,1,5));   // zaken's seer to zaken's watchman
		_mobs.add(new Transformer(21608,21609,1,5));   // zaken's watchman
		_mobs.add(new Transformer(20832,21602,1,5));   // Zaken's pikeman
		_mobs.add(new Transformer(21602,21603,1,5));   // Zaken's pikeman
		_mobs.add(new Transformer(20833,21605,1,5));   // Zaken's archet
		_mobs.add(new Transformer(21605,21606,1,5));   // Zaken's archet
		_mobs.add(new Transformer(21625,21623,1,5));   // zaken's Elite Guard to zaken's Guard
		_mobs.add(new Transformer(21623,21624,1,5));   // zaken's Guard
		_mobs.add(new Transformer(20842,21620,1,5));   // Musveren
		_mobs.add(new Transformer(21620,21621,1,5));   // Musveren
		_mobs.add(new Transformer(20830,20859,100,0)); //
		_mobs.add(new Transformer(21067,21068,100,0)); //
		_mobs.add(new Transformer(21062,21063,100,0)); // Angels
		_mobs.add(new Transformer(20831,20860,100,0)); //
		_mobs.add(new Transformer(21070,21071,100,0)); //
		
		int[] mobsKill = {20830,21067,21062,20831,21070};
		
        for (int mob : mobsKill)
		{
			this.addEventId(mob, Quest.QuestEventType.ON_KILL);
		}
		
		int[] mobsAttack = {21620,20842,21623,21625,21605,20833,21602,20832,21608,20835,21258};
		
        for (int mob : mobsAttack)
		{		
			this.addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}

	@Override
	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		for (Transformer monster : _mobs)
		{
			if (npc.getNpcId() == monster.getId())
			{
				if (Rnd.get(100) <= monster.getChance() * Config.RATE_DROP_QUEST)
				{
					if (monster.getMessage() !=0)
					{
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(),0,npc.getName(),Message[Rnd.get(monster.getMessage())]));
					}
					npc.onDecay();
					L2Attackable newNpc = (L2Attackable) this.addSpawn(monster.getIdPoly(),npc);
					L2Character originalAttacker = isPet? attacker.getPet(): attacker;
					newNpc.setRunning();
					newNpc.addDamageHate(originalAttacker,0,999);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
				}
			}
		}
		return super.onAttack (npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
    {
        for (Transformer monster : _mobs)
		{
			if (npc.getNpcId() == monster.getId())
			{
				if (monster.getMessage() !=0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(),0,npc.getName(),Message[Rnd.get(monster.getMessage())]));
				}
				L2Attackable newNpc = (L2Attackable) this.addSpawn(monster.getIdPoly(),npc);
				L2Character originalAttacker = isPet? killer.getPet(): killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalAttacker,0,999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
			}
		}
        return super.onKill(npc,killer,isPet);
    }

	@Override
	public void run()
	{
		//new Transform(-1, "transform", "ai");
	}
}