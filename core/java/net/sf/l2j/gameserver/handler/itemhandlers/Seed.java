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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.SkillHolder;

/**
 * @author l3x
 */
public class Seed implements IItemHandler
{
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		if (CastleManorManager.getInstance().isDisabled())
			return;
		
		final L2Object tgt = playable.getTarget();
		if (!(tgt instanceof L2Npc))
		{
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!(tgt instanceof L2MonsterInstance) || tgt instanceof L2ChestInstance || ((L2Character) tgt).isRaid())
		{
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2MonsterInstance target = (L2MonsterInstance) tgt;
		if (target.isDead())
		{
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (target.isSeeded())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int seedId = item.getItemId();
		if (areaValid(seedId, MapRegionTable.getInstance().getAreaCastle(playable)))
		{
			target.setSeeded(seedId, (L2PcInstance) playable);
			final SkillHolder[] skills = item.getEtcItem().getSkills();
			if (skills != null)
			{
				if (skills[0] == null)
					return;
				
				L2Skill itemskill = skills[0].getSkill();
				((L2PcInstance) playable).useMagic(itemskill, false, false);
			}
			
		}
		else
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE));
	}
	
	/**
	 * @param seedId
	 * @param castleId
	 * @return
	 */
	private static boolean areaValid(int seedId, int castleId)
	{
		return L2Manor.getInstance().getCastleIdForSeed(seedId) == castleId;
	}
}