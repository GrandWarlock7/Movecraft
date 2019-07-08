package net.countercraft.movecraft.commands;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.countercraft.movecraft.utils.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class ManOverboardCommand implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("manOverBoard")) {
            return false;
        }
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("ManOverboard - Must Be Player"));
            return true;
        }

        Player player = (Player) commandSender;
        Craft craft = CraftManager.getInstance().getCraftByPlayerName(player.getName());
        if (craft == null) { //player is not piloting a craft
            for(Craft playerCraft : CraftManager.getInstance()) {
                if (playerCraft.getMovedPlayers().containsKey(player)) {
                    craft = playerCraft;
                    break;
                }
            }
        }
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("ManOverboard - No Craft Found"));
            return true;
        }

        Location telPoint = craft.getCrewSigns().containsKey(player.getUniqueId()) ? craft.getCrewSigns().get(player.getUniqueId()) : getCraftTeleportPoint(craft);

        if (craft.getW() != player.getWorld()) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Craft is on another world!"));
            return true;
        }

        if (craft.getMovedPlayers().containsKey(player) && (System.currentTimeMillis() - craft.getMovedPlayers().get(player)) / 1_000 > Settings.ManOverBoardTimeout) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("ManOverboard - Timed Out"));
            return true;
        }

        if (telPoint.distanceSquared(player.getLocation()) > 1_000_000) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Craft is over 1000 blocks away!"));
            return true;
        }

        if (craft.getDisabled() || craft.getSinking()) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Can't teleport to a disabled or sinking craft"));
        }

        player.teleport(telPoint);
        return true;
    }

    private Location getCraftTeleportPoint(Craft craft) {
        double telX = (craft.getHitBox().getMinX() + craft.getHitBox().getMaxX())/2D;
        double telZ = (craft.getHitBox().getMinZ() + craft.getHitBox().getMaxZ())/2D;
        double telY = craft.getHitBox().getMaxY();
        return new Location(craft.getW(), telX, telY, telZ);
    }
}
