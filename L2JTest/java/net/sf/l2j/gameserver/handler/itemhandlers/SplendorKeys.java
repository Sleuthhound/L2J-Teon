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

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

/**
 * @author chris
 */
public class SplendorKeys implements IItemHandler
{
    private static final int[] ITEM_IDS = { 8056 };
    public static final int INTERACTION_DISTANCE = 100;

    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
	int itemId = item.getItemId();
	if (!(playable instanceof L2PcInstance))
	    return;
	L2PcInstance activeChar = (L2PcInstance) playable;
	L2Object target = activeChar.getTarget();
	if (!(target instanceof L2DoorInstance))
	{
	    activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
	    activeChar.sendPacket(new ActionFailed());
	    return;
	}
	L2DoorInstance door = (L2DoorInstance) target;
	if (!activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
	{
	    activeChar.sendMessage("Too far.");
	    activeChar.sendPacket(new ActionFailed());
	    return;
	}
	if ((activeChar.getAbnormalEffect() > 0) || activeChar.isInCombat())
	{
	    activeChar.sendMessage("You cannot use the key now.");
	    activeChar.sendPacket(new ActionFailed());
	    return;
	}
	int openChance = 35;
	if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
	    return;
	switch (itemId)
	{
	case 8056: // Key of Splendor Room
	    if (door.getDoorName().startsWith("Gate_of_Splendor"))
	    {
		if ((openChance > 0) && (Rnd.get(100) < openChance))
		{
		    activeChar.sendMessage("You opened Gate of Splendor.");
		    door.openMe();
		    door.onOpen(); // Closes the door after 60sec
		    activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
		} else
		{
		    // test with: activeChar.sendPacket(new
		    // SystemMessage(SystemMessage.FAILED_TO_UNLOCK_DOOR));
		    activeChar.sendMessage("You failed to open Gate of Splendor.");
		    activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
		    PlaySound playSound = new PlaySound("interfacesound.system_close_01");
		    activeChar.sendPacket(playSound);
		}
	    } else
	    {
		activeChar.sendMessage("Incorrect Door.");
	    }
	    break;
	}
    }

    public int[] getItemIds()
    {
	return ITEM_IDS;
    }
}
