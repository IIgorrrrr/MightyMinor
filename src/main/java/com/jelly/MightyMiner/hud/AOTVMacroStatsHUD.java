package com.jelly.MightyMiner.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.macros.AOTVMacro;

import java.util.Arrays;
import java.util.List;

public class AOTVMacroStatsHUD extends TextHud {
    public AOTVMacroStatsHUD () {
            super(true, 0, 0, 1.0f, true, true, 10, 8, 8, new OneColor(0, 0, 0, 150), true, 2, new OneColor(0, 0, 0, 240));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (!MacroHandler.macros.get(3).isEnabled()) {
            String[] exampleLines = new String[]{
                    "§l§4Aotv Makro:",
                    "§rThis shit has been running for: §f" + "0",
                    "§rwaypoints visited: §f" + "0",
                    "§rwaypoints visited / h: §f" + "0",
            };
            lines.addAll(Arrays.asList(exampleLines));
        } else {
            String[] AOTVMacroLines = AOTVMacro.drawInfo();
            lines.addAll(Arrays.asList(AOTVMacroLines));
        }
    }
}
