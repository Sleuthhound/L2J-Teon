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
package quests.Q042_HelpTheUncle;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q042_HelpTheUncle extends Quest
{
	private static final String qn = "Q042_HelpTheUncle";
	
	// NPCs
	private final static int WATERS = 30828;
	private final static int SOPHYA = 30735;
	
	// Items
	private final static int TRIDENT = 291;
	private final static int MAP_PIECE = 7548;
	private final static int MAP = 7549;
	private final static int PET_TICKET = 7583;
	
	// Monsters
	private final static int MONSTER_EYE_DESTROYER = 20068;
	private final static int MONSTER_EYE_GAZER = 20266;
	
	public Q042_HelpTheUncle(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			MAP_PIECE,
			MAP
		};
		
		addStartNpc(WATERS);
		addTalkId(WATERS, SOPHYA);
		
		addKillId(MONSTER_EYE_DESTROYER, MONSTER_EYE_GAZER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30828-01.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30828-03.htm") && st.getQuestItemsCount(TRIDENT) >= 1)
		{
			st.set("cond", "2");
			st.takeItems(TRIDENT, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30828-05.htm") && st.getQuestItemsCount(MAP_PIECE) >= 30)
		{
			st.takeItems(MAP_PIECE, 30);
			st.giveItems(MAP, 1);
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30735-06.htm") && st.getQuestItemsCount(MAP) == 1)
		{
			st.takeItems(MAP, 1);
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30828-07.htm"))
		{
			st.giveItems(PET_TICKET, 1);
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
				if (player.getLevel() >= 25)
					htmltext = "30828-00.htm";
				else
				{
					htmltext = "<html><body>This quest can only be taken by characters that have a minimum level of 25. Return when you are more experienced.</body></html>";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case WATERS:
						if (cond == 1)
							if (st.getQuestItemsCount(TRIDENT) == 0)
								htmltext = "30828-01a.htm";
							else
								htmltext = "30828-02.htm";
						else if (cond == 2)
							htmltext = "30828-03a.htm";
						else if (cond == 3)
							htmltext = "30828-04.htm";
						else if (cond == 4)
							htmltext = "30828-05a.htm";
						else if (cond == 5)
							htmltext = "30828-06.htm";
						break;
					
					case SOPHYA:
						if (cond == 4 && st.getQuestItemsCount(MAP) >= 1)
							htmltext = "30735-05.htm";
						else if (cond == 5)
							htmltext = "30735-06a.htm";
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = Quest.getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.getInt("cond") == 2)
			if (st.dropAlwaysQuestItems(MAP_PIECE, 1, 30))
				st.set("cond", "3");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q042_HelpTheUncle(42, qn, "Help the Uncle!");
	}
}