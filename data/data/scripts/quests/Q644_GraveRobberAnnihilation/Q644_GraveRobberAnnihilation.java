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
package quests.Q644_GraveRobberAnnihilation;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q644_GraveRobberAnnihilation extends Quest
{
	private final static String qn = "Q644_GraveRobberAnnihilation";
	
	// Item
	private final static int GOODS = 8088;
	
	// Rewards
	private static final Map<String, int[]> Rewards = new HashMap<>();
	{
		Rewards.put("var", new int[]
		{
			1865,
			30
		});
		Rewards.put("ask", new int[]
		{
			1867,
			40
		});
		Rewards.put("ior", new int[]
		{
			1869,
			30
		});
		Rewards.put("coa", new int[]
		{
			1870,
			30
		});
		Rewards.put("cha", new int[]
		{
			1871,
			30
		});
		Rewards.put("abo", new int[]
		{
			1872,
			40
		});
	}
	
	// NPC
	private final static int KARUDA = 32017;
	
	public Q644_GraveRobberAnnihilation(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			GOODS
		};
		
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		
		addKillId(22003, 22004, 22005, 22006, 22008);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (Rewards.containsKey(event))
		{
			if (st.getQuestItemsCount(GOODS) == 120)
			{
				htmltext = "32017-04.htm";
				st.takeItems(GOODS, -1);
				st.rewardItems(Rewards.get(event)[0], Rewards.get(event)[1]);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "32017-07.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 20 && player.getLevel() <= 33)
					htmltext = "32017-01.htm";
				else
					htmltext = "32017-06.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32017-05.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(GOODS) == 120)
						htmltext = "32017-03.htm";
					else
						htmltext = "32017-07.htm";
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
		
		if (st.dropQuestItems(GOODS, 1, 120, 500000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q644_GraveRobberAnnihilation(644, qn, "Grave Robber Annihilation");
	}
}