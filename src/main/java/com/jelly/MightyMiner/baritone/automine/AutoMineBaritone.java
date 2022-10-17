package com.jelly.MightyMiner.baritone.automine;

import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.automine.pathing.AStarPathFinder;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathMode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockType;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.LinkedList;

public class AutoMineBaritone{

    Minecraft mc = Minecraft.getMinecraft();
    MineBehaviour mineBehaviour;


    LinkedList<BlockNode> blocksToMine = new LinkedList<>();
    LinkedList<BlockNode> minedBlocks = new LinkedList<>();

    boolean inAction = false;
    Rotation rotation = new Rotation();

    int deltaJumpTick = 0;

    enum PlayerState {
        WALKING,
        MINING,
        NONE
    }
    PlayerState currentState;
    Block[] targetBlockType;
    volatile boolean enabled;

    AStarPathFinder pathFinder;
    BlockPos playerFloorPos;

    boolean jumpFlag;
    int jumpCooldown;

    boolean shouldGoToFinalBlock;

    public AutoMineBaritone(MineBehaviour mineBehaviour){
        this.mineBehaviour = mineBehaviour;
        pathFinder = new AStarPathFinder(getPathBehaviour());
    }


    public void clearBlocksToWalk(){
        blocksToMine.clear();
        BlockRenderer.renderMap.clear();
        minedBlocks.clear();
    }




    public void mineFor(BlockPos blockPos){
        enable();
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();
        shouldGoToFinalBlock = false;

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);


        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        new Thread(() -> {
            try{
                blocksToMine = pathFinder.getPath(blockPos);
            } catch (Throwable e){
                Logger.playerLog("Error when getting path!");
                e.printStackTrace();
            }
            if (!blocksToMine.isEmpty()) {
                for (BlockNode blockNode : blocksToMine) {
                    BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
                }
                BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
            } else {
                Logger.playerLog("blocks to mine EMPTY!");
            }
            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }


    public void mineFor(Block... blockType) {
        targetBlockType = blockType;
        shouldGoToFinalBlock = false;
        enable();
        clearBlocksToWalk();

        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        new Thread(() -> {
            try{
                if(mineBehaviour.isMineWithPreference())
                    blocksToMine = pathFinder.getPathWithPreference(blockType);
                else
                    blocksToMine = pathFinder.getPath(blockType);
            } catch (Throwable e){
                Logger.playerLog("Error when getting path!");
                e.printStackTrace();
            }
            if (!blocksToMine.isEmpty()) {
                for (BlockNode blockNode : blocksToMine) {
                    BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
                }
                BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
            } else {
                Logger.playerLog("blocks to mine EMPTY!");
            }
            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }

    public void mineForInSingleThread(Block... blockType) throws Exception{ // ONLY USABLE IN SHORT DISTANCE!!!!
        enable();
        targetBlockType = blockType;
        shouldGoToFinalBlock = false;

        clearBlocksToWalk();

        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        if(mineBehaviour.isMineWithPreference())
            blocksToMine = pathFinder.getPathWithPreference(blockType);
        else
            blocksToMine = pathFinder.getPath(blockType);

        if (!blocksToMine.isEmpty()) {
            for (BlockNode blockNode : blocksToMine) {
                BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
            }
            BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
        } else {
            Logger.playerLog("blocks to mine EMPTY!");
        }
        Logger.log("Starting to mine");
        inAction = true;
        currentState = PlayerState.NONE;
        stuckTickCount = 0;
    }


    public void goTo(BlockPos blockPos){
        shouldGoToFinalBlock = true;
        enable();
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);


        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        new Thread(() -> {
            try{
                blocksToMine = pathFinder.getPath(blockPos, PathMode.GOTO);
            } catch (Throwable e){
                Logger.playerLog("Error when getting path!");
                e.printStackTrace();
            }
            if (!blocksToMine.isEmpty()) {
                for (BlockNode blockNode : blocksToMine) {
                    BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
                }
                BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
            } else {
                Logger.playerLog("blocks to mine EMPTY!");
            }
            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }



    private void enable(){
        enabled = true;
    }


    public void disableBaritone() {
        pauseBaritone();
        enabled = false;
    }
    private void pauseBaritone() {
        inAction = false;
        currentState = PlayerState.NONE;
        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!blocksToMine.isEmpty() && blocksToMine.getLast().getBlockType() == BlockType.MINE)
            pathFinder.addToBlackList(blocksToMine.getLast().getBlockPos());


        clearBlocksToWalk();

    }
    public boolean isEnabled(){
        return enabled;
    }


    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToMine != null){
                if(!blocksToMine.isEmpty()){
                    for(int i = 0; i < blocksToMine.size(); i++){
                        mc.fontRendererObj.drawString(blocksToMine.get(i).getBlockPos().toString() + " " + blocksToMine.get(i).getBlockType().toString() , 5, 5 + 10 * i, -1);
                    }
                }
            }
            if(currentState != null)
                mc.fontRendererObj.drawString(currentState.toString(), 300, 5, -1);
        }
    }


    int stuckTickCount = 0;
    public void onTickEvent(TickEvent.Phase phase){

        if(phase != TickEvent.Phase.START || !inAction || blocksToMine.isEmpty())
            return;


        if (shouldRemoveFromList(blocksToMine.getLast())) {
            stuckTickCount = 0;
            minedBlocks.add(blocksToMine.getLast());
            BlockRenderer.renderMap.remove(blocksToMine.getLast().getBlockPos());
            blocksToMine.removeLast();
        } else {
            //stuck handling
            stuckTickCount++;
            if(stuckTickCount > 20 * mineBehaviour.getRestartTimeThreshold()){
                new Thread(restartBaritone).start();
                return;
            }
        }

        if(blocksToMine.isEmpty() || (BlockUtils.isPassable(blocksToMine.getFirst().getBlockPos()) && blocksToMine.getFirst().getBlockType() == BlockType.MINE)){
            if(!shouldGoToFinalBlock || BlockUtils.getPlayerLoc().equals(minedBlocks.getLast().getBlockPos())) {
                disableBaritone();
                return;
            }
        }

        updateState();

        BlockPos targetBlock = blocksToMine.isEmpty() ? minedBlocks.getLast().getBlockPos() : blocksToMine.getLast().getBlockPos();

        switch (currentState){
            case WALKING:
                float reqYaw = AngleUtils.getRequiredYaw(targetBlock);
                if(inAction && !blocksToMine.isEmpty())
                    rotation.intLockAngle(reqYaw, 0, 5); // camera angle

                if(!jumpFlag && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0 && jumpCooldown == 0 && mc.thePlayer.onGround){

                    if((targetBlock.getY() > mc.thePlayer.posY && (blocksToMine.isEmpty() || blocksToMine.getLast().getBlockType() == BlockType.WALK)) ||
                            (!minedBlocks.isEmpty() && minedBlocks.getLast().getBlockPos().getY() > mc.thePlayer.posY) && blocksToMine.getLast().getBlockType() == BlockType.MINE){
                        jumpFlag = true;
                        jumpCooldown = 10;
                    }
                }

                KeybindHandler.updateKeys(
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) < -4 * 10 + 45,
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) >= 45,
                        false, false, false, false, false,
                        jumpFlag);

                jumpFlag = false;
                if(jumpCooldown > 0) jumpCooldown --;
                break;


            case MINING:
                mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Pick", "Drill", "Gauntlet");
                KeybindHandler.updateKeys(
                        false, false, false, false,
                        mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null &&
                                mc.objectMouseOver.getBlockPos().equals(targetBlock),
                        false,
                        mineBehaviour.isShiftWhenMine(),
                        false);


                if(mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null){
                    // special cases for optimization
                    if(BlockUtils.isAdjacentXZ(targetBlock, BlockUtils.getPlayerLoc()) && !AngleUtils.shouldLookAtCenter(targetBlock) &&
                            (( targetBlock.getY() - mc.thePlayer.posY == 0 && BlockUtils.getBlock(targetBlock.up()).equals(Blocks.air) )|| targetBlock.getY() - mc.thePlayer.posY == 1)){
                        rotation.intLockAngle(AngleUtils.getRequiredYaw(targetBlock), 28, mineBehaviour.getRotationTime());
                    } else if (!BlockUtils.isPassable(targetBlock) && !rotation.rotating)
                        rotation.intLockAngle(AngleUtils.getRequiredYaw(targetBlock), AngleUtils.getRequiredPitch(targetBlock), mineBehaviour.getRotationTime());

                }
                break;
        }


        if (deltaJumpTick > 0)
            deltaJumpTick--;
    }

    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();

    }

    private void updateState(){

        if(mineBehaviour.getMineType() == AutoMineType.STATIC) {
            currentState = PlayerState.MINING;
            return;
        }
        if(shouldGoToFinalBlock && blocksToMine.isEmpty()){
            currentState = PlayerState.WALKING;
            return;
        }

        if(blocksToMine.isEmpty())
            return;

        if(minedBlocks.isEmpty()){
            currentState =  blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
            return;
        }

        if(blocksToMine.getLast().getBlockType() == BlockType.WALK) {
            currentState = PlayerState.WALKING;
            return;
        }

        if(currentState == PlayerState.WALKING){
            if(minedBlocks.getLast().getBlockType() == BlockType.WALK){
                if(blocksToMine.getLast().getBlockType() == BlockType.MINE)
                    currentState = PlayerState.MINING;

            } else if(minedBlocks.getLast().getBlockType() == BlockType.MINE) {
                if (BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc()))
                    currentState = PlayerState.MINING;
            }

        } else if(currentState == PlayerState.MINING){
            if (blocksToMine.getLast().getBlockType() == BlockType.MINE) {
                if( (BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down()) || BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down(2)))
                        && !BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc())) {
                    currentState = PlayerState.WALKING;
                }
            }

        } else if(currentState == PlayerState.NONE){
            currentState = blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
        }
    }

    private final Runnable restartBaritone = () -> {
        try {
            pauseBaritone();
            Logger.playerLog("Restarting baritone");
            Thread.sleep(200);
            KeybindHandler.setKeyBindState(KeybindHandler.keybindS, true);
            Thread.sleep(100);
            mineFor(targetBlockType);
        } catch (InterruptedException ignored) {}
    };

    private boolean shouldRemoveFromList(BlockNode lastBlockNode){
        if(lastBlockNode.getBlockType() == BlockType.MINE) {
           //System.out.println(BlockUtils.getBlockUnCashed(lastBlockNode.getBlockPos()));
            return BlockUtils.isPassable(lastBlockNode.getBlockPos()) || BlockUtils.getBlock(lastBlockNode.getBlockPos()).equals(Blocks.bedrock);
        }
        else
            return BlockUtils.onTheSameXZ(lastBlockNode.getBlockPos(), BlockUtils.getPlayerLoc()) || !BlockUtils.fitsPlayer(lastBlockNode.getBlockPos().down());
    }

    private PathBehaviour getPathBehaviour(){
        return new PathBehaviour(
                mineBehaviour.getForbiddenMiningBlocks() == null ? null : mineBehaviour.getForbiddenMiningBlocks(),
                mineBehaviour.getAllowedMiningBlocks() == null ? null : mineBehaviour.getAllowedMiningBlocks(),
                mineBehaviour.getMaxY(),
                mineBehaviour.getMinY(),
                mineBehaviour.getMineType() == AutoMineType.DYNAMIC ? 30 : 4,
                mineBehaviour.getMineType() == AutoMineType.STATIC
        );
    }


}
