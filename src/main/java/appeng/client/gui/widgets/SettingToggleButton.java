/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.StorageFilter;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.core.localization.ButtonToolTips;
import appeng.util.EnumCycler;

public class SettingToggleButton<T extends Enum<T>> extends IconButton {
    private static Map<EnumPair<?>, ButtonAppearance> appearances;
    private final Setting<T> buttonSetting;
    private final IHandler<SettingToggleButton<T>> onPress;
    private final EnumSet<T> validValues;
    private T currentValue;

    @FunctionalInterface
    public interface IHandler<T extends SettingToggleButton<?>> {
        void handle(T button, boolean backwards);
    }

    public SettingToggleButton(Setting<T> setting, T val,
            IHandler<SettingToggleButton<T>> onPress) {
        this(setting, val, t -> true, onPress);
    }

    public SettingToggleButton(Setting<T> setting, T val, Predicate<T> isValidValue,
            IHandler<SettingToggleButton<T>> onPress) {
        super(SettingToggleButton::onPress);
        this.onPress = onPress;

        // Build a list of values (in order) that are valid w.r.t. the given predicate
        EnumSet<T> validValues = EnumSet.allOf(val.getDeclaringClass());
        validValues.removeIf(isValidValue.negate());
        validValues.removeIf(s -> !setting.getValues().contains(s));
        this.validValues = validValues;

        this.buttonSetting = setting;
        this.currentValue = val;

        if (appearances == null) {
            appearances = new HashMap<>();
            registerApp(Icon.ACCESS_READ, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode,
                    ButtonToolTips.Read);
            registerApp(Icon.ACCESS_WRITE, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode,
                    ButtonToolTips.Write);
            registerApp(Icon.ACCESS_READ_WRITE, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode,
                    ButtonToolTips.ReadWrite);

            registerApp(Icon.ARROW_UP, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.Ascending);
            registerApp(Icon.ARROW_DOWN, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.Descending);

            registerApp(Icon.TERMINAL_STYLE_SMALL, Settings.TERMINAL_STYLE, TerminalStyle.SMALL,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Small);
            registerApp(Icon.TERMINAL_STYLE_MEDIUM, Settings.TERMINAL_STYLE, TerminalStyle.MEDIUM,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Medium);
            registerApp(Icon.TERMINAL_STYLE_TALL, Settings.TERMINAL_STYLE, TerminalStyle.TALL,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Tall);
            registerApp(Icon.TERMINAL_STYLE_FULL, Settings.TERMINAL_STYLE, TerminalStyle.FULL,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Full);

            registerApp(Icon.SORT_BY_NAME, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy,
                    ButtonToolTips.ItemName);
            registerApp(Icon.SORT_BY_AMOUNT, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy,
                    ButtonToolTips.NumberOfItems);
            registerApp(Icon.SORT_BY_MOD, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod);

            registerApp(Icon.FUZZY_PERCENT_25, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_25);
            registerApp(Icon.FUZZY_PERCENT_50, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_50);
            registerApp(Icon.FUZZY_PERCENT_75, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_75);
            registerApp(Icon.FUZZY_PERCENT_99, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_99);
            registerApp(Icon.FUZZY_IGNORE, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZIgnoreAll);

            registerApp(Icon.FULLNESS_EMPTY, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenEmpty);
            registerApp(Icon.FULLNESS_HALF, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenWorkIsDone);
            registerApp(Icon.FULLNESS_FULL, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenFull);

            registerApp(Icon.BLOCKING_MODE_YES, Settings.BLOCKING_MODE, YesNo.YES, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.Blocking);
            registerApp(Icon.BLOCKING_MODE_NO, Settings.BLOCKING_MODE, YesNo.NO, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.NonBlocking);

            registerApp(Icon.VIEW_MODE_CRAFTING, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft,
                    ButtonToolTips.CraftOnly);
            registerApp(Icon.VIEW_MODE_ALL, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft,
                    ButtonToolTips.CraftEither);

            registerApp(Icon.STORAGE_FILTER_EXTRACTABLE_ONLY, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY,
                    ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsNo);
            registerApp(Icon.STORAGE_FILTER_EXTRACTABLE_NONE, Settings.STORAGE_FILTER, StorageFilter.NONE,
                    ButtonToolTips.ReportInaccessibleItems,
                    ButtonToolTips.ReportInaccessibleItemsYes);

            registerApp(Icon.PLACEMENT_BLOCK, Settings.PLACE_BLOCK, YesNo.YES, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementYes);
            registerApp(Icon.PLACEMENT_ITEM, Settings.PLACE_BLOCK, YesNo.NO, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementNo);

            registerApp(Icon.SCHEDULING_DEFAULT, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT,
                    ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeDefault);
            registerApp(Icon.SCHEDULING_ROUND_ROBIN, Settings.SCHEDULING_MODE, SchedulingMode.ROUNDROBIN,
                    ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRoundRobin);
            registerApp(Icon.SCHEDULING_RANDOM, Settings.SCHEDULING_MODE, SchedulingMode.RANDOM,
                    ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRandom);

            registerApp(Icon.OVERLAY_OFF, Settings.OVERLAY_MODE, YesNo.NO, ButtonToolTips.OverlayMode,
                    ButtonToolTips.OverlayModeNo);
            registerApp(Icon.OVERLAY_ON, Settings.OVERLAY_MODE, YesNo.YES, ButtonToolTips.OverlayMode,
                    ButtonToolTips.OverlayModeYes);

            registerApp(Icon.FILTER_ON_EXTRACT_ENABLED, Settings.FILTER_ON_EXTRACT, YesNo.YES,
                    ButtonToolTips.FilterOnExtract, ButtonToolTips.FilterOnExtractEnabled);
            registerApp(Icon.FILTER_ON_EXTRACT_DISABLED, Settings.FILTER_ON_EXTRACT, YesNo.NO,
                    ButtonToolTips.FilterOnExtract, ButtonToolTips.FilterOnExtractDisabled);

            registerApp(Icon.UNLOCKED, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE,
                    ButtonToolTips.LockCraftingMode,
                    ButtonToolTips.LockCraftingModeNone);
            registerApp(Icon.REDSTONE_LOW, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_WHILE_HIGH,
                    ButtonToolTips.LockCraftingMode,
                    ButtonToolTips.LockCraftingWhileRedstoneHigh);
            registerApp(Icon.REDSTONE_HIGH, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_WHILE_LOW,
                    ButtonToolTips.LockCraftingMode,
                    ButtonToolTips.LockCraftingWhileRedstoneLow);
            registerApp(Icon.REDSTONE_PULSE, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_PULSE,
                    ButtonToolTips.LockCraftingMode,
                    ButtonToolTips.LockCraftingUntilRedstonePulse);
            registerApp(Icon.ENTER, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_RESULT,
                    ButtonToolTips.LockCraftingMode,
                    ButtonToolTips.LockCraftingUntilResultReturned);
        }
    }

    private static void onPress(Button btn) {
        if (btn instanceof SettingToggleButton) {
            ((SettingToggleButton<?>) btn).triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = false;
        // This isn't great, but we don't get any information about right-clicks
        // otherwise
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AEBaseScreen) {
            backwards = ((AEBaseScreen<?>) currentScreen).isHandlingRightClick();
        }
        onPress.handle(this, backwards);
    }

    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val,
            ButtonToolTips title, Component... tooltipLines) {
        var lines = new ArrayList<Component>();
        lines.add(title.text());
        Collections.addAll(lines, tooltipLines);

        appearances.put(
                new EnumPair<>(setting, val),
                new ButtonAppearance(icon, null, lines));
    }

    private static <T extends Enum<T>> void registerApp(ItemLike item, Setting<T> setting, T val,
            ButtonToolTips title, Component... tooltipLines) {
        var lines = new ArrayList<Component>();
        lines.add(title.text());
        Collections.addAll(lines, tooltipLines);

        appearances.put(
                new EnumPair<>(setting, val),
                new ButtonAppearance(null, item.asItem(), lines));
    }

    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val,
            ButtonToolTips title, ButtonToolTips hint) {
        registerApp(icon, setting, val, title, hint.text());
    }

    @Nullable
    private ButtonAppearance getApperance() {
        if (this.buttonSetting != null && this.currentValue != null) {
            return appearances.get(new EnumPair<>(this.buttonSetting, this.currentValue));
        }
        return null;
    }

    @Override
    protected Icon getIcon() {
        var app = getApperance();
        if (app != null && app.icon != null) {
            return app.icon;
        }
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    @Override
    protected Item getItemOverlay() {
        var app = getApperance();
        if (app != null && app.item != null) {
            return app.item;
        }
        return null;
    }

    public Setting<T> getSetting() {
        return this.buttonSetting;
    }

    public T getCurrentValue() {
        return this.currentValue;
    }

    public void set(T e) {
        if (this.currentValue != e) {
            this.currentValue = e;
        }
    }

    public T getNextValue(boolean backwards) {
        return EnumCycler.rotateEnum(currentValue, backwards, validValues);
    }

    @Override
    public List<Component> getTooltipMessage() {

        if (this.buttonSetting == null || this.currentValue == null) {
            return Collections.emptyList();
        }

        var buttonAppearance = appearances.get(new EnumPair<>(this.buttonSetting, this.currentValue));
        if (buttonAppearance == null) {
            return Collections.singletonList(ButtonToolTips.NoSuchMessage.text());
        }

        return buttonAppearance.tooltipLines;
    }

    private static final class EnumPair<T extends Enum<T>> {

        final Setting<T> setting;
        final T value;

        public EnumPair(Setting<T> setting, T value) {
            this.setting = setting;
            this.value = value;
        }

        @Override
        public int hashCode() {
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final EnumPair<?> other = (EnumPair<?>) obj;
            return other.setting == this.setting && other.value == this.value;
        }
    }

    private record ButtonAppearance(@Nullable Icon icon, @Nullable Item item, List<Component> tooltipLines) {
    }
}
