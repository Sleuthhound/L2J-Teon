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
package net.sf.l2j.gameserver;

import java.util.logging.Logger;

import net.sf.l2j.Config;

public class L2JTeon 
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	/**
	 *  L2JTeon Info
	 */
	public static void L2JTeon()
	{
		_log.info("--------------------------------------------------------");
		_log.info("             Developers: Maxi56, Meyknho                ");
		_log.info("--------------------------------------------------------");
		_log.info("  #      # # #   # # #    # # #  # # #    ####   #   #  ");
		_log.info("  #          #     #        #    #       #    #  ##  #  ");
		_log.info("  #        #       #        #    # # #   #    #  # # #  ");
		_log.info("  #      #      #  #        #    #       #    #  #  ##  ");
		_log.info("  # # #  # # #  ####        #    # # #    ####   #   #  ");
		_log.info("--------------------------------------------------------");
		_log.info("    L2J Teon Core Version: "+Config.SERVER_VERSION       );
		_log.info("  L2J Teon DataPack Version: "+Config.DATAPACK_VERSION   );
		_log.info("                   Copyright 2009-2010                  ");
		_log.info("--------------------------------------------------------");	
	}
}