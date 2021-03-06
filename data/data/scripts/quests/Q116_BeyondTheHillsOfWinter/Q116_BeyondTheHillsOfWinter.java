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
package quests.Q116_BeyondTheHillsOfWinter;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q116_BeyondTheHillsOfWinter extends Quest
{
	private static final String qn = "Q116_BeyondTheHillsOfWinter";
	
	// NPCs
	private static final int FILAUR = 30535;
	private static final int OBI = 32052;
	
	// Items
	private static final int BANDAGE = 1833;
	private static final int ENERGY_STONE = 5589;
	private static final int THIEF_KEY = 1661;
	private static final int GOODS = 8098;
	
	// Reward
	private static final int SSD = 1463;
	
	public Q116_BeyondTheHillsOfWinter(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			GOODS
		};
		
		addStartNpc(FILAUR);
		addTalkId(FILAUR, OBI);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30535-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30535-05.htm"))
		{
			st.set("cond", "2");
			st.giveItems(GOODS, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("materials"))
		{
			htmltext = "32052-02.htm";
			st.takeItems(GOODS, -1);
			st.rewardItems(SSD, 1650);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("adena"))
		{
			htmltext = "32052-02.htm";
			st.takeItems(GOODS, -1);
			st.giveItems(57, 16500);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 30 && player.getRace().ordinal() == 4)
					htmltext = "30535-01.htm";
				else
				{
					htmltext = "30535-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case FILAUR:
						if (cond == 1)
						{
							if (st.getQuestItemsCount(BANDAGE) >= 20 && st.getQuestItemsCount(ENERGY_STONE) >= 5 && st.getQuestItemsCount(THIEF_KEY) >= 10)
							{
								htmltext = "30535-03.htm";
								st.takeItems(BANDAGE, 20);
								st.takeItems(ENERGY_STONE, 5);
								st.takeItems(THIEF_KEY, 10);
							}
							else
								htmltext = "30535-04.htm";
						}
						else if (cond == 2)
							htmltext = "30535-05.htm";
						break;
					
					case OBI:
						if (cond == 2 && st.getQuestItemsCount(GOODS) == 1)
							htmltext = "32052-00.htm";
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = Quest.getAlreadyCompletedMsg();
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q116_BeyondTheHillsOfWinter(116, qn, "Beyond the Hills of Winter");
	}
}