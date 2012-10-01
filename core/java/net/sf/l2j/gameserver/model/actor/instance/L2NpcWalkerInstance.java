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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2NpcWalkerAI;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.templates.chars.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * This class manages some npcs can walk in the city. <br>
 * @author Rayan RPG, JIV
 */
public class L2NpcWalkerInstance extends L2NpcInstance
{
	public L2NpcWalkerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setAI(new L2NpcWalkerAI(new L2NpcWalkerAIAccessor()));
	}
	
	/**
	 * AI can't be detached, npc must move always with the same AI instance.
	 * @param newAI AI to set for this L2NpcWalkerInstance
	 */
	@Override
	public void setAI(L2CharacterAI newAI)
	{
		if (!(_ai instanceof L2NpcWalkerAI))
			_ai = newAI;
	}
	
	@Override
	public void onSpawn()
	{
		getAI().setHomeX(getX());
		getAI().setHomeY(getY());
		getAI().setHomeZ(getZ());
	}
	
	/**
	 * Sends a chat to all _knowObjects
	 * @param chat message to say
	 */
	public void broadcastChat(String chat)
	{
		NpcSay cs = new NpcSay(getObjectId(), Say2.ALL, getNpcId(), chat);
		Broadcast.toKnownPlayers(this, cs);
	}
	
	/**
	 * NpcWalkers are immortals
	 */
	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
	}
	
	/**
	 * NpcWalkers are immortals
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		return false;
	}
	
	@Override
	public L2NpcWalkerAI getAI()
	{
		return (L2NpcWalkerAI) _ai;
	}
	
	protected class L2NpcWalkerAIAccessor extends L2Character.AIAccessor
	{
		/**
		 * AI can't be deattached.
		 */
		@Override
		public void detachAI()
		{
		}
	}
}