package com.earth2me.essentials;

import java.io.File;
import java.util.*;
import net.ess3.api.IEssentials;
import net.ess3.storage.StoredLocation;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;


public class UserData
{
	protected final IEssentials ess;
	private EssentialsConf config;
	private final File folder;


	protected UserData(IEssentials ess, File file)
	{
		this.ess = ess;
		folder = new File(ess.getPlugin().getDataFolder(), "userdata");
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		config = new EssentialsConf(file);
		reloadConfig();
	}

	public final void reloadConfig()
	{
		config.load();
		money = _getMoney();
		unlimited = _getUnlimited();
		powertools = _getPowertools();
		homes = _getHomes();
		lastLocation = _getLastLocation();
		lastTeleportTimestamp = _getLastTeleportTimestamp();
		lastHealTimestamp = _getLastHealTimestamp();
		jail = _getJail();
		mails = _getMails();
		teleportEnabled = getTeleportEnabled();
		ignoredPlayers = getIgnoredPlayers();
		godmode = _getGodModeEnabled();
		muted = getMuted();
		muteTimeout = _getMuteTimeout();
		jailed = getJailed();
		jailTimeout = _getJailTimeout();
		lastLogin = _getLastLogin();
		lastLogout = _getLastLogout();
		lastLoginAddress = _getLastLoginAddress();
		afk = getAfk();
		geolocation = _getGeoLocation();
		isSocialSpyEnabled = _isSocialSpyEnabled();
		isNPC = _isNPC();
		arePowerToolsEnabled = _arePowerToolsEnabled();
		kitTimestamps = _getKitTimestamps();
		nickname = _getNickname();
	}

	private double money;

	private double _getMoney()
	{
		double money = ess.getSettings().getData().getEconomy().getStartingBalance();
		if (config.hasProperty("money"))
		{
			money = config.getDouble("money", money);
		}
		if (Math.abs(money) > ess.getSettings().getData().getEconomy().getMaxMoney())
		{
			money = money < 0 ? -ess.getSettings().getData().getEconomy().getMaxMoney() : ess.getSettings().getData().getEconomy().getMaxMoney();
		}
		return money;
	}

	public double getMoney()
	{
		return money;
	}

	public void setMoney(double value)
	{
		money = value;
		if (Math.abs(money) > ess.getSettings().getData().getEconomy().getMaxMoney())
		{
			money = money < 0 ? -ess.getSettings().getData().getEconomy().getMaxMoney() : ess.getSettings().getData().getEconomy().getMaxMoney();
		}
		config.setProperty("money", value);
		config.save();
	}

	private Map<String, Object> homes;

	private Map<String, Object> _getHomes()
	{
		if (config.isConfigurationSection("homes"))
		{
			return config.getConfigurationSection("homes").getValues(false);
		}
		return new HashMap<String, Object>();
	}

	private String getHomeName(String search)
	{
		if (Util.isInt(search))
		{
			try
			{
				search = getHomes().get(Integer.parseInt(search) - 1);
			}
			catch (Exception e)
			{
			}
		}
		return search;
	}

	public StoredLocation getHome(String name) throws Exception
	{
		String search = getHomeName(name);
		return config.getLocation("homes." + search, ess.getServer());
	}


	public List<String> getHomes()
	{
		return new ArrayList<String>(homes.keySet());
	}

	public void setHome(String name, Location loc)
	{
		//Invalid names will corrupt the yaml
		name = Util.safeString(name);
		homes.put(name, loc);
		config.setProperty("homes." + name, loc);
		config.save();
	}

	public void delHome(String name) throws Exception
	{
		String search = getHomeName(name);
		if (!homes.containsKey(search))
		{
			search = Util.safeString(search);
		}
		if (homes.containsKey(search))
		{
			homes.remove(search);
			config.removeProperty("homes." + search);
			config.save();
		}
		else
		{
			throw new Exception("invalidHome");
		}
	}

	public boolean hasHome()
	{
		if (config.hasProperty("home"))
		{
			return true;
		}
		return false;
	}

	private String nickname;

	public String _getNickname()
	{
		return config.getString("nickname");
	}

	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(String nick)
	{
		nickname = nick;
		config.setProperty("nickname", nick);
		config.save();
	}

	private List<Integer> unlimited;

	private List<Integer> _getUnlimited()
	{
		return config.getIntegerList("unlimited");
	}

	public List<Integer> getUnlimited()
	{
		return unlimited;
	}

	public boolean hasUnlimited(ItemStack stack)
	{
		return unlimited.contains(stack.getTypeId());
	}

	public void setUnlimited(ItemStack stack, boolean state)
	{
		if (unlimited.contains(stack.getTypeId()))
		{
			unlimited.remove(Integer.valueOf(stack.getTypeId()));
		}
		if (state)
		{
			unlimited.add(stack.getTypeId());
		}
		config.setProperty("unlimited", unlimited);
		config.save();
	}

	private Map<String, Object> powertools;

	private Map<String, Object> _getPowertools()
	{
		if (config.isConfigurationSection("powertools"))
		{
			return config.getConfigurationSection("powertools").getValues(false);
		}
		return new HashMap<String, Object>();
	}

	public Set<String> getPowertools()
	{
		return powertools.keySet();
	}

	public void clearAllPowertools()
	{
		powertools.clear();
		config.setProperty("powertools", powertools);
		config.save();
	}

	@SuppressWarnings("unchecked")
	public List<String> getPowertool(ItemStack stack)
	{
		return (List<String>)powertools.get("" + stack.getTypeId());
	}

	@SuppressWarnings("unchecked")
	public List<String> getPowertool(int id)
	{
		return (List<String>)powertools.get("" + id);
	}

	public void setPowertool(ItemStack stack, List<String> commandList)
	{
		if (commandList == null || commandList.isEmpty())
		{
			powertools.remove("" + stack.getTypeId());
		}
		else
		{
			powertools.put("" + stack.getTypeId(), commandList);
		}
		config.setProperty("powertools", powertools);
		config.save();
	}

	public boolean hasPowerTools()
	{
		return !powertools.isEmpty();
	}

	private StoredLocation lastLocation;

	private StoredLocation _getLastLocation()
	{
		try
		{
			return config.getLocation("lastlocation", ess.getServer());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public StoredLocation getLastLocation()
	{
		return lastLocation;
	}


	private long lastTeleportTimestamp;

	private long _getLastTeleportTimestamp()
	{
		return config.getLong("timestamps.lastteleport", 0);
	}

	public long getLastTeleportTimestamp()
	{
		return lastTeleportTimestamp;
	}

	public void setLastTeleportTimestamp(long time)
	{
		lastTeleportTimestamp = time;
		config.setProperty("timestamps.lastteleport", time);
		config.save();
	}

	private long lastHealTimestamp;

	private long _getLastHealTimestamp()
	{
		return config.getLong("timestamps.lastheal", 0);
	}

	public long getLastHealTimestamp()
	{
		return lastHealTimestamp;
	}

	public void setLastHealTimestamp(long time)
	{
		lastHealTimestamp = time;
		config.setProperty("timestamps.lastheal", time);
		config.save();
	}

	private String jail;

	private String _getJail()
	{
		return config.getString("jail");
	}

	public String getJail()
	{
		return jail;
	}

	public void setJail(String jail)
	{
		if (jail == null || jail.isEmpty())
		{
			this.jail = null;
			config.removeProperty("jail");
		}
		else
		{
			this.jail = jail;
			config.setProperty("jail", jail);
		}
		config.save();
	}

	private List<String> mails;

	private List<String> _getMails()
	{
		return config.getStringList("mail");
	}

	public List<String> getMails()
	{
		return mails;
	}

	public void setMails(List<String> mails)
	{
		if (mails == null)
		{
			config.removeProperty("mail");
			mails = _getMails();
		}
		else
		{
			config.setProperty("mail", mails);
		}
		this.mails = mails;
		config.save();
	}

	public void addMail(String mail)
	{
		mails.add(mail);
		setMails(mails);
	}

	private boolean teleportEnabled;

	private boolean getTeleportEnabled()
	{
		return config.getBoolean("teleportenabled", true);
	}

	public boolean isTeleportEnabled()
	{
		return teleportEnabled;
	}

	public void setTeleportEnabled(boolean set)
	{
		teleportEnabled = set;
		config.setProperty("teleportenabled", set);
		config.save();
	}

	public boolean toggleTeleportEnabled()
	{
		boolean ret = !isTeleportEnabled();
		setTeleportEnabled(ret);
		return ret;
	}

	public boolean toggleSocialSpy()
	{
		boolean ret = !isSocialSpyEnabled();
		setSocialSpyEnabled(ret);
		return ret;
	}

	private List<String> ignoredPlayers;

	public List<String> getIgnoredPlayers()
	{
		return Collections.synchronizedList(config.getStringList("ignore"));
	}

	public void setIgnoredPlayers(List<String> players)
	{
		if (players == null || players.isEmpty())
		{
			ignoredPlayers = Collections.synchronizedList(new ArrayList<String>());
			config.removeProperty("ignore");
		}
		else
		{
			ignoredPlayers = players;
			config.setProperty("ignore", players);
		}
		config.save();
	}


	private boolean godmode;

	private boolean _getGodModeEnabled()
	{
		return config.getBoolean("godmode", false);
	}

	public boolean isGodModeEnabled()
	{
		return godmode;
	}

	public void setGodModeEnabled(boolean set)
	{
		godmode = set;
		config.setProperty("godmode", set);
		config.save();
	}

	private boolean muted;

	public boolean getMuted()
	{
		return config.getBoolean("muted", false);
	}

	public boolean isMuted()
	{
		return muted;
	}

	public void setMuted(boolean set)
	{
		muted = set;
		config.setProperty("muted", set);
		config.save();
	}

	private long muteTimeout;

	private long _getMuteTimeout()
	{
		return config.getLong("timestamps.mute", 0);
	}

	public long getMuteTimeout()
	{
		return muteTimeout;
	}

	public void setMuteTimeout(long time)
	{
		muteTimeout = time;
		config.setProperty("timestamps.mute", time);
		config.save();
	}

	private boolean jailed;

	private boolean getJailed()
	{
		return config.getBoolean("jailed", false);
	}

	public boolean isJailed()
	{
		return jailed;
	}

	public void setJailed(boolean set)
	{
		jailed = set;
		config.setProperty("jailed", set);
		config.save();
	}

	public boolean toggleJailed()
	{
		boolean ret = !isJailed();
		setJailed(ret);
		return ret;
	}

	private long jailTimeout;

	private long _getJailTimeout()
	{
		return config.getLong("timestamps.jail", 0);
	}

	public long getJailTimeout()
	{
		return jailTimeout;
	}

	public void setJailTimeout(long time)
	{
		jailTimeout = time;
		config.setProperty("timestamps.jail", time);
		config.save();
	}

	public String getBanReason()
	{
		return config.getString("ban.reason");
	}

	public void setBanReason(String reason)
	{
		config.setProperty("ban.reason", Util.sanitizeString(reason));
		config.save();
	}

	public long getBanTimeout()
	{
		return config.getLong("ban.timeout", 0);
	}

	public void setBanTimeout(long time)
	{
		config.setProperty("ban.timeout", time);
		config.save();
	}

	private long lastLogin;

	private long _getLastLogin()
	{
		return config.getLong("timestamps.login", 0);
	}

	public long getLastLogin()
	{
		return lastLogin;
	}

	private void _setLastLogin(long time) //unused method
	{
		lastLogin = time;
		config.setProperty("timestamps.login", time);
	}


	private long lastLogout;

	private long _getLastLogout()
	{
		return config.getLong("timestamps.logout", 0);
	}

	public long getLastLogout()
	{
		return lastLogout;
	}

	public void setLastLogout(long time)
	{
		lastLogout = time;
		config.setProperty("timestamps.logout", time);
		config.save();
	}

	private String lastLoginAddress;

	private String _getLastLoginAddress()
	{
		return config.getString("ipAddress", "");
	}

	public String getLastLoginAddress()
	{
		return lastLoginAddress;
	}

	private void _setLastLoginAddress(String address) //TODO: unused method?
	{
		lastLoginAddress = address;
		config.setProperty("ipAddress", address);
	}

	private boolean afk;

	private boolean getAfk()
	{
		return config.getBoolean("afk", false);
	}

	public boolean isAfk()
	{
		return afk;
	}

	public void setAfk(boolean set)
	{
		afk = set;
		config.setProperty("afk", set);
		config.save();
	}

	public boolean toggleAfk()
	{
		boolean ret = !isAfk();
		setAfk(ret);
		return ret;
	}

	private boolean newplayer; //TODO: unused variable?
	private String geolocation;

	private String _getGeoLocation()
	{
		return config.getString("geolocation");
	}

	public String getGeoLocation()
	{
		return geolocation;
	}

	public void setGeoLocation(String geolocation)
	{
		if (geolocation == null || geolocation.isEmpty())
		{
			this.geolocation = null;
			config.removeProperty("geolocation");
		}
		else
		{
			this.geolocation = geolocation;
			config.setProperty("geolocation", geolocation);
		}
		config.save();
	}

	private boolean isSocialSpyEnabled;

	private boolean _isSocialSpyEnabled()
	{
		return config.getBoolean("socialspy", false);
	}

	public boolean isSocialSpyEnabled()
	{
		return isSocialSpyEnabled;
	}

	public void setSocialSpyEnabled(boolean status)
	{
		isSocialSpyEnabled = status;
		config.setProperty("socialspy", status);
		config.save();
	}

	private boolean isNPC;

	private boolean _isNPC()
	{
		return config.getBoolean("npc", false);
	}

	public boolean isNPC()
	{
		return isNPC;
	}

	public void setNPC(boolean set)
	{
		isNPC = set;
		config.setProperty("npc", set);
		config.save();
	}

	private boolean arePowerToolsEnabled;

	public boolean arePowerToolsEnabled()
	{
		return arePowerToolsEnabled;
	}

	public void setPowerToolsEnabled(boolean set)
	{
		arePowerToolsEnabled = set;
		config.setProperty("powertoolsenabled", set);
		config.save();
	}

	public boolean togglePowerToolsEnabled()
	{
		boolean ret = !arePowerToolsEnabled();
		setPowerToolsEnabled(ret);
		return ret;
	}

	private boolean _arePowerToolsEnabled()
	{
		return config.getBoolean("powertoolsenabled", true);
	}

	private ConfigurationSection kitTimestamps;

	private ConfigurationSection _getKitTimestamps()
	{

		if (config.isConfigurationSection("timestamps.kits"))
		{
			final ConfigurationSection section = config.getConfigurationSection("timestamps.kits");
			final ConfigurationSection newSection = new MemoryConfiguration();
			for (String command : section.getKeys(false))
			{
				if (section.isLong(command))
				{
					newSection.set(command.toLowerCase(Locale.ENGLISH), section.getLong(command));
				}
				else if (section.isInt(command))
				{
					newSection.set(command.toLowerCase(Locale.ENGLISH), (long)section.getInt(command));
				}
			}
			return newSection;
		}
		return new MemoryConfiguration();
	}

	public Set<String> getKitTimestamps()
	{
		return kitTimestamps.getKeys(false);
	}

	public long getKitTimestamp(String name)
	{
		name = name.replace('.', '_').replace('/', '_');
		if (kitTimestamps != null)
		{
			return kitTimestamps.getLong(name, 0l);
		}
		return 0l;
	}

	public void setKitTimestamp(final String name, final long time)
	{
		kitTimestamps.set(name.toLowerCase(Locale.ENGLISH), time);
		config.setProperty("timestamps.kits", kitTimestamps);
		config.save();
	}

	public void save()
	{
		config.save();
	}
}