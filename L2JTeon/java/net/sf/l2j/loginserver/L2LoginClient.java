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
package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.crypt.LoginCrypt;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.serverpackets.L2LoginServerPacket;
import net.sf.l2j.loginserver.serverpackets.LoginFail;
import net.sf.l2j.loginserver.serverpackets.PlayFail;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayFail.PlayFailReason;
import net.sf.l2j.util.Rnd;
import com.l2jserver.mmocore.network.MMOClient;
import com.l2jserver.mmocore.network.MMOConnection;

/**
 * Represents a client connected into the LoginServer
 * 
 * @author KenM
 */
public final class L2LoginClient extends
	MMOClient<MMOConnection<L2LoginClient>>
{
    private static Logger _log = Logger.getLogger(L2LoginClient.class.getName());

    public static enum LoginClientState
    {
	CONNECTED, AUTHED_GG, AUTHED_LOGIN
    };

    private LoginClientState _state;
    // Crypt
    private LoginCrypt _loginCrypt;
    private ScrambledKeyPair _scrambledPair;
    private byte[] _blowfishKey;
    private String _account;
    private int _accessLevel;
    private int _lastServer;
    private boolean _usesInternalIP;
    private SessionKey _sessionKey;
    private int _sessionId;
    private boolean _joinedGS;
    private long _connectionStartTime;

    public L2LoginClient(MMOConnection<L2LoginClient> con)
    {
	super(con);
	_state = LoginClientState.CONNECTED;
	String ip = getConnection().getSocketChannel().socket().getInetAddress().getHostAddress();
	// TODO unhardcode this
	if (ip.startsWith("192.168") || ip.startsWith("10.0") || ip.equals("127.0.0.1"))
	    _usesInternalIP = true;
	_scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
	_blowfishKey = LoginController.getInstance().getBlowfishKey();
	_sessionId = Rnd.nextInt();
	_connectionStartTime = System.currentTimeMillis();
	_loginCrypt = new LoginCrypt();
	_loginCrypt.setKey(_blowfishKey);
    }

    public boolean usesInternalIP()
    {
	return _usesInternalIP;
    }

    /**
     * @see com.l2jserver.mmocore.interfaces.MMOClient#decrypt(java.nio.ByteBuffer,
     *      int)
     */
    @Override
    public boolean decrypt(ByteBuffer buf, int size)
    {
	boolean ret = false;
	try
	{
	    ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
	} catch (IOException e)
	{
	    e.printStackTrace();
	    closeNow();
	    return false;
	}
	if (!ret)
	{
	    byte[] dump = new byte[size];
	    System.arraycopy(buf.array(), buf.position(), dump, 0, size);
	    _log.warning("Wrong checksum from client: " + toString());
	    closeNow();
	}
	return ret;
    }

    /**
     * @see com.l2jserver.mmocore.interfaces.MMOClient#encrypt(java.nio.ByteBuffer,
     *      int)
     */
    @Override
    public boolean encrypt(ByteBuffer buf, int size)
    {
	final int offset = buf.position();
	try
	{
	    size = _loginCrypt.encrypt(buf.array(), offset, size);
	} catch (IOException e)
	{
	    e.printStackTrace();
	    return false;
	}
	buf.position(offset + size);
	return true;
    }

    public LoginClientState getState()
    {
	return _state;
    }

    public void setState(LoginClientState state)
    {
	_state = state;
    }

    public byte[] getBlowfishKey()
    {
	return _blowfishKey;
    }

    public byte[] getScrambledModulus()
    {
	return _scrambledPair._scrambledModulus;
    }

    public RSAPrivateKey getRSAPrivateKey()
    {
	return (RSAPrivateKey) _scrambledPair._pair.getPrivate();
    }

    public String getAccount()
    {
	return _account;
    }

    public void setAccount(String account)
    {
	_account = account;
    }

    public void setAccessLevel(int accessLevel)
    {
	_accessLevel = accessLevel;
    }

    public int getAccessLevel()
    {
	return _accessLevel;
    }

    public void setLastServer(int lastServer)
    {
	_lastServer = lastServer;
    }

    public int getLastServer()
    {
	return _lastServer;
    }

    public int getSessionId()
    {
	return _sessionId;
    }

    public boolean hasJoinedGS()
    {
	return _joinedGS;
    }

    public void setJoinedGS(boolean val)
    {
	_joinedGS = val;
    }

    public void setSessionKey(SessionKey sessionKey)
    {
	_sessionKey = sessionKey;
    }

    public SessionKey getSessionKey()
    {
	return _sessionKey;
    }

    public long getConnectionStartTime()
    {
	return _connectionStartTime;
    }

    public void sendPacket(L2LoginServerPacket lsp)
    {
	getConnection().sendPacket(lsp);
    }

    public void close(LoginFailReason reason)
    {
	getConnection().close(new LoginFail(reason));
    }

    public void close(PlayFailReason reason)
    {
	getConnection().close(new PlayFail(reason));
    }

    public void close(L2LoginServerPacket lsp)
    {
	getConnection().close(lsp);
    }

    @Override
    public void onDisconection()
    {
	if (Config.DEBUG)
	    _log.info("DISCONNECTED: " + toString());
	if (getState() != LoginClientState.AUTHED_LOGIN)
	    LoginController.getInstance().removeLoginClient(this);
	else if (!hasJoinedGS())
	    LoginController.getInstance().removeAuthedLoginClient(getAccount());
    }

    @Override
    public String toString()
    {
	InetAddress address = getConnection().getSocketChannel().socket().getInetAddress();
	if (getState() == LoginClientState.AUTHED_LOGIN)
	    return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
	else
	    return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
    }
}