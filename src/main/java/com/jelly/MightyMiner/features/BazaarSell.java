package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.utils.InventoryUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.Timer;
import com.jelly.MightyMiner.world.GameState;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BazaarSell {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String prefix = "[§9§lBazaar §9§lSell] ";
    private static int originalSlot;
    private static boolean wasMacroEnabled;
    public static StateBZ currentStateBZ = StateBZ.NONE;
    public static boolean haveSacksBeenSold = false;
    public static final int delay = MightyMiner.config.bazaarAutoSellDelay;
    public static boolean hasAllBeenSold = false;
    public static boolean haveInvBeenSold = false;
    private static final Timer waitTimer = new Timer();
    @Getter
    private static boolean enabled;
    public enum StateBZ {
        NONE,
        START,
        OPEN_MENU,
        SELL_INV,
        SELL_SACKS,
        SELL_SACKS_CONFIRM,
        SELL_INV_CONFIRM,
        CLOSE_MENU
    }
    public static void enable() {
        if (MacroHandler.gameState.cookie == GameState.EffectState.OFF) {
            log("bro yo broke ass aint got a cookie");
            disable();
            return;
        }
        originalSlot = mc.thePlayer.inventory.currentItem;
        enabled = true;
        wasMacroEnabled = MacroHandler.isEnabled();
        if (wasMacroEnabled) MacroHandler.disableScript();
        waitTimer.reset();
        log("STARTDEDEDEDED");
        currentStateBZ = StateBZ.START;
        haveSacksBeenSold = false;
        hasAllBeenSold = false;
        haveInvBeenSold = false;
    }

    public static void disable() {
        LogUtils.debugLog("[AutoSell] Finished auto sell");
        if (wasMacroEnabled) MacroHandler.startScript(MightyMiner.config.macroType);
        mc.thePlayer.closeScreen();
        mc.thePlayer.inventory.currentItem = originalSlot;
        enabled = false;
        mc.inGameHasFocus = true;
        currentStateBZ = StateBZ.NONE;
        mc.mouseHelper.grabMouseCursor();
    }
    @SubscribeEvent
    public final void onTick(TickEvent.ClientTickEvent event) { // yarr matey sphagetti up ahead!!
        if (event.phase == TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!enabled) return;

        if (!waitTimer.hasReached(delay)) {
            return;
        }
        switch (currentStateBZ) {
            case START:
                if (mc.currentScreen == null) {
                    currentStateBZ = StateBZ.OPEN_MENU;
                } else if (InventoryUtils.getInventoryName() != null && InventoryUtils.getInventoryName().contains("Bazaar")) {
                    currentStateBZ = StateBZ.SELL_INV;
                }
                waitTimer.reset();
                break;
            case OPEN_MENU:
                mc.thePlayer.sendChatMessage("/bz");
                log("ok guys we are officially in the bazaar menu yoo less go");
                // lets go and sell some sacks guys
                currentStateBZ = StateBZ.SELL_SACKS;
                waitTimer.reset();
                break;
            case SELL_SACKS:
                InventoryUtils.clickOpenContainerSlot(InventoryUtils.getSlotFromGui("Sell Sacks"));
                currentStateBZ = StateBZ.SELL_SACKS_CONFIRM;
                waitTimer.reset();
                break;
            case SELL_INV_CONFIRM:
                InventoryUtils.clickOpenContainerSlot(InventoryUtils.getSlotFromGui("Selling whole"));
                currentStateBZ = StateBZ.CLOSE_MENU;
                waitTimer.reset();
                break;
            case SELL_SACKS_CONFIRM:
                InventoryUtils.clickOpenContainerSlot(InventoryUtils.getSlotFromGui("Selling whole"));
                currentStateBZ = StateBZ.SELL_INV;
                waitTimer.reset();
                break;
            case SELL_INV:
                InventoryUtils.clickOpenContainerSlot(InventoryUtils.getSlotFromGui("Sell Inventory"));
                currentStateBZ = StateBZ.SELL_INV_CONFIRM;
                waitTimer.reset();
                break;
            case CLOSE_MENU:
                mc.thePlayer.closeScreen();
                currentStateBZ = StateBZ.NONE;
                log("yo this shit has been sold guys");
                disable();
                waitTimer.reset();
                break;
        }

    }



    public static void log(String message) { LogUtils.addMessage(prefix + message); }
}
