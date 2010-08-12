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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2ManorManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

/**
 * Format: cdd[dd] c // id (0xC4) d // manor id d // seeds to buy [ d // seed id d // count ]
 *
 * @param decrypt
 * @author l3x
 */
public class RequestBuySeed extends L2GameClientPacket
{
	private static final String _C__C4_REQUESTBUYSEED = "[C] C4 RequestBuySeed";
	private int _count;
	private int _manorId;
	private int[] _items; // size _count * 2

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if (_count > 500 || _count * 8 < _buf.remaining()) // check values
		{
			_count = 0;
			return;
		}
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i * 2 + 0] = itemId;
			long cnt = readD();
			if (cnt > Integer.MAX_VALUE || cnt < 1)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (_count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Object target = player.getTarget();
		if (!(target instanceof L2ManorManagerInstance))
			target = player.getLastFolkNPC();
		if (!(target instanceof L2ManorManagerInstance))
			return;
		Castle castle = CastleManager.getInstance().getCastleById(_manorId);
		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			int price = 0;
			int residual = 0;
			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			price = seed.getPrice();
			residual = seed.getCanProduce();
			if (price <= 0)
				return;
			if (residual < count)
				return;
			totalPrice += count * price;
			L2Item template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight += count * template.getWeight();
			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(seedId) == null)
				slots++;
		}
		if (totalPrice > Integer.MAX_VALUE)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
			player.closeNetConnection(); // kick
			return;
		}
		if (!player.getInventory().validateWeight(totalWeight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		// Charge buyer
		if (totalPrice < 0 || !player.reduceAdena("Buy", (int) totalPrice, target, false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		// Adding to treasury for Manor Castle
		castle.addToTreasuryNoTax((int) totalPrice);
		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			if (count < 0)
				count = 0;
			// Update Castle Seeds Amount
			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			seed.setCanProduce(seed.getCanProduce() - count);
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				CastleManager.getInstance().getCastleById(_manorId).updateSeed(seed.getId(), seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Buy", seedId, count, player, target);
			if (item.getCount() > count)
				playerIU.addModifiedItem(item);
			else
				playerIU.addNewItem(item);
			// Send Char Buy Messages
			SystemMessage sm = null;
			sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(seedId);
			sm.addNumber(count);
			player.sendPacket(sm);
		}
		// Send update packets
		player.sendPacket(playerIU);
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	@Override
	public String getType()
	{
		return _C__C4_REQUESTBUYSEED;
	}
}
