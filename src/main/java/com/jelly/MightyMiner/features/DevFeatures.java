package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DevFeatures {
    private static final Minecraft mc = Minecraft.getMinecraft();
    @SubscribeEvent
    public final void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!MightyMiner.config.testingMode) return;
        if (mc.currentScreen == null) return;
        for (Slot slot : mc.thePlayer.inventoryContainer.inventorySlots) {
            if (slot.getSlotTexture() == null) continue;
            LogUtils.addMessage(slot.getSlotTexture());
        }
    }
}
