package com.jelly.MightyMiner.command;

import com.jelly.MightyMiner.features.BazaarSell;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ExecuteBazaarSell extends CommandBase {
    @Override
    public String getCommandName() {
        return "bazaarautosell";
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "piece of shit";
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        BazaarSell.enable();
    }
}
