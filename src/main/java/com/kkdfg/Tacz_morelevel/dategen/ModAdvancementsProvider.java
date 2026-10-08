package com.kkdfg.Tacz_morelevel.dategen;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 自定义进度根节点见 {@code data/tacz_morelevel/advancements/main/root.json}（手写 JSON，使用 {@code minecraft:tick}）。
 */
public class ModAdvancementsProvider extends AdvancementProvider {

    public ModAdvancementsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper) {
        super(output, registries, List.of(new EmptyAdvancements()));
        Objects.requireNonNull(existingFileHelper);
    }

    private static final class EmptyAdvancements implements AdvancementSubProvider {
        @Override
        public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {}
    }
}
