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
package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class FuncEnchant extends Func
{
    public FuncEnchant(Stats pStat, int pOrder, Object owner, @SuppressWarnings("unused")
    Lambda lambda)
    {
	super(pStat, pOrder, owner);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void calc(Env env)
    {
	if ((cond != null) && !cond.test(env))
	    return;
	L2ItemInstance item = (L2ItemInstance) funcOwner;
	int cristall = item.getItem().getCrystalType();
	Enum itemType = item.getItemType();
	if (cristall == L2Item.CRYSTAL_NONE)
	    return;
	int enchant = item.getEnchantLevel();
	int overenchant = 0;
	if (enchant > 3)
	{
	    overenchant = enchant - 3;
	    enchant = 3;
	}
	if ((stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE))
	{
	    env.value += enchant + 3 * overenchant;
	    return;
	}
	if (stat == Stats.MAGIC_ATTACK)
	{
	    switch (item.getItem().getCrystalType())
	    {
	    case L2Item.CRYSTAL_S:
		env.value += 4 * enchant + 8 * overenchant;
		break;
	    case L2Item.CRYSTAL_A:
		env.value += 3 * enchant + 6 * overenchant;
		break;
	    case L2Item.CRYSTAL_B:
		env.value += 3 * enchant + 6 * overenchant;
		break;
	    case L2Item.CRYSTAL_C:
		env.value += 3 * enchant + 6 * overenchant;
		break;
	    case L2Item.CRYSTAL_D:
		env.value += 2 * enchant + 4 * overenchant;
		break;
	    }
	    return;
	}
	switch (item.getItem().getCrystalType())
	{
	case L2Item.CRYSTAL_A:
	    if (itemType == L2WeaponType.BOW)
		env.value += 8 * enchant + 16 * overenchant;
	    else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
		env.value += 5 * enchant + 10 * overenchant;
	    else
		env.value += 4 * enchant + 8 * overenchant;
	    break;
	case L2Item.CRYSTAL_B:
	    if (itemType == L2WeaponType.BOW)
		env.value += 6 * enchant + 12 * overenchant;
	    else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
		env.value += 4 * enchant + 8 * overenchant;
	    else
		env.value += 3 * enchant + 6 * overenchant;
	    break;
	case L2Item.CRYSTAL_C:
	    if (itemType == L2WeaponType.BOW)
		env.value += 6 * enchant + 12 * overenchant;
	    else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
		env.value += 4 * enchant + 8 * overenchant;
	    else
		env.value += 3 * enchant + 6 * overenchant;
	    break;
	case L2Item.CRYSTAL_D:
	    if (itemType == L2WeaponType.BOW)
		env.value += 4 * enchant + 8 * overenchant;
	    else
		env.value += 2 * enchant + 4 * overenchant;
	    break;
	case L2Item.CRYSTAL_S:
	    if (itemType == L2WeaponType.BOW)
		env.value += 10 * enchant + 20 * overenchant;
	    else if ((itemType == L2WeaponType.DUALFIST) || (itemType == L2WeaponType.DUAL) || ((itemType == L2WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
		env.value += 4 * enchant + 12 * overenchant;
	    else
		env.value += 4 * enchant + 10 * overenchant;
	    break;
	}
	return;
    }
}