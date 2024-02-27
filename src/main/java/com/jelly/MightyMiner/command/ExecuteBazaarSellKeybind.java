package com.jelly.MightyMiner.command;

import com.jelly.MightyMiner.features.BazaarSell;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class ExecuteBazaarSellKeybind {
    private final KeyBinding bazaarSell = new KeyBinding("Auto sell items to bazaar", Keyboard.KEY_P, "MightyMiner - Autosell");

    public ExecuteBazaarSellKeybind() {
        ClientRegistry.registerKeyBinding(bazaarSell);
    }
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (bazaarSell.isPressed()) {
            BazaarSell.enable();
        }
    }

}
