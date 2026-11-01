package de.fireveilring;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class FireVeilRingClient implements ClientModInitializer {
    private static final String WAND_ID = "FIRE_VEIL_WAND";
    private static boolean openMenu = false;

    @Override
    public void onInitializeClient() {
        FireVeilConfig.load();
        FireVeilRenderer.init();

        // Rechtsklick mit der Wand -> Ring starten (greift nie ins Spiel ein)
        UseItemCallback.EVENT.register((player, level, hand) -> {
            Minecraft mc = Minecraft.getInstance();
            if (level.isClientSide()
                    && player == mc.player
                    && FireVeilConfig.get().enabled
                    && isFireVeilWand(player.getItemInHand(hand))) {
                FireVeilState.onCast(player.position());
            }
            return InteractionResult.PASS;
        });

        // /fireveil = Menue, /fireveil test = Ring 5s zum Ausprobieren
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("fireveil")
                        .executes(ctx -> {
                            openMenu = true;
                            return 1;
                        })
                        .then(ClientCommands.literal("test").executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player != null) {
                                FireVeilState.start(mc.player.position());
                            }
                            return 1;
                        }))));

        // Menue erst im naechsten Tick oeffnen, sonst schliesst der Chat es sofort wieder
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openMenu) {
                openMenu = false;
                client.setScreen(new FireVeilConfigScreen(null));
            }
        });
    }

    private static boolean isFireVeilWand(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (WAND_ID.equals(tag.getStringOr("id", ""))) {
                return true;
            }
        }
        return stack.getHoverName().getString().contains("Fire Veil Wand");
    }
}
