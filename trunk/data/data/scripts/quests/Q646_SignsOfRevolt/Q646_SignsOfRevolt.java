/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q646_SignsOfRevolt;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;
import net.sf.l2j.util.Rnd;

public class Q646_SignsOfRevolt extends Quest
{
	private static final String qn = "Q646_SignsOfRevolt";
	
	// NPC
	private static final int TORRANT = 32016;
	
	// Item
	private static final int CURSED_DOLL = 8087;
	
	// Rewards
	private static final Map<String, int[]> Rewards = new HashMap<>();
	{
		Rewards.put("1", new int[]
		{
			1880,
			9
		});
		Rewards.put("2", new int[]
		{
			1881,
			12
		});
		Rewards.put("3", new int[]
		{
			1882,
			20
		});
		Rewards.put("4", new int[]
		{
			57,
			21600
		});
	}
	
	public Q646_SignsOfRevolt(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			CURSED_DOLL
		};
		
		addStartNpc(TORRANT);
		addTalkId(TORRANT);
		
		addKillId(22029, 22030, 22031, 22032, 22033, 22034, 22035, 22036, 22037, 22038, 22039, 22040, 22041, 22042, 22043, 22044, 22045, 22047, 22049);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32016-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (Rewards.containsKey(event))
		{
			htmltext = "32016-07.htm";
			st.takeItems(CURSED_DOLL, -1);
			st.giveItems(Rewards.get(event)[0], Rewards.get(event)[1]);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 40 && player.getLevel() <= 51)
					htmltext = "32016-01.htm";
				else
				{
					htmltext = "32016-02.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32016-04.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(CURSED_DOLL) == 180)
						htmltext = "32016-05.htm";
					else
						htmltext = "32016-04.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, npc, "1");
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		int count = st.getQuestItemsCount(CURSED_DOLL);
		
		if (count < 180)
		{
			int chance = (int) (75 * Config.RATE_QUEST_DROP);
			int numItems = chance / 100;
			chance = chance % 100;
			
			if (Rnd.get(100) < chance)
				numItems++;
			
			if (numItems > 0)
			{
				if (count + numItems >= 180)
				{
					numItems = 180 - count;
					st.playSound(QuestState.SOUND_MIDDLE);
					st.set("cond", "2");
				}
				else
					st.playSound(QuestState.SOUND_ITEMGET);
				
				st.giveItems(CURSED_DOLL, numItems);
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q646_SignsOfRevolt(646, qn, "Signs Of Revolt");
	}
}