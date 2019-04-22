package net.runelite.client.plugins.prayeralert;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.itemstats.stats.Stat;
import net.runelite.client.plugins.itemstats.stats.Stats;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class PrayerAlertOverlay extends Overlay
{
    private final Client client;
    private final PrayerAlertConfig config;
    private final PanelComponent panelComponent = new PanelComponent();
    private final ItemManager itemManager;

    private final Stat prayer = Stats.PRAYER;

    @Inject
    private PrayerAlertOverlay(Client client, PrayerAlertConfig config, ItemManager itemManager)
    {
        setPosition(OverlayPosition.TOP_RIGHT);
        this.client = client;
        this.config = config;
        this.itemManager = itemManager;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
            int prayerLevel = getPrayerLevel();
            int prayerPoints = getPrayerPoints();

            if (config.alwaysShowAlert()){
                boolean drink = drinkPrayerPotion(prayerLevel, prayerPoints);
                if (drink) {
                    prayerRestorePanel(panelComponent, graphics);
                }
            }
            else {
                boolean drink = drinkPrayerPotion(prayerLevel, prayerPoints);
                boolean hasPrayerPotion = checkInventoryForPotion();
                if (drink && hasPrayerPotion) {
                    prayerRestorePanel(panelComponent, graphics);
                }
            }

        return panelComponent.render(graphics);
    }

    private int getPrayerLevel()
    {
        return prayer.getMaximum(client);
    }

    private int getPrayerPoints()
    {
        return prayer.getValue(client);
    }

    private boolean drinkPrayerPotion(int prayerLevel, int prayerPoints)
    {
        boolean drink = false;
        int prayerPotionRestoreValue = 7;
        double quarterOfPrayerLevel = (0.25) * (double) prayerLevel;

        prayerPotionRestoreValue = prayerPotionRestoreValue + (int) quarterOfPrayerLevel;

        if (prayerPoints < (prayerLevel - prayerPotionRestoreValue))
        {
            drink = true;
        }

        return drink;
    }

    private boolean checkInventoryForPotion()
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        Item[] inventoryItems;
        boolean hasPrayerPotion = false;

        int[] potionID = {ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4, ItemID.PRAYER_POTION1_20396, ItemID.PRAYER_POTION2_20395,
                ItemID.PRAYER_POTION3_20394, ItemID.PRAYER_POTION4_20393, ItemID.PRAYER_MIX1, ItemID.PRAYER_MIX2, ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2,
                ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4, ItemID.SUPER_RESTORE_MIX1, ItemID.SUPER_RESTORE_MIX2};

        if (inventory != null)
        {
            inventoryItems = inventory.getItems();
            for (Item item : inventoryItems)
            {
                for (int prayerPotionId : potionID)
                {
                    if (item.getId() == prayerPotionId)
                    {
                        hasPrayerPotion = true;
                    }
                }
            }
        }

        return hasPrayerPotion;
    }

    private void prayerRestorePanel(PanelComponent panelComponent, Graphics2D graphics){
        panelComponent.getChildren().add(new ImageComponent(itemManager.getImage(ItemID.PRAYER_POTION4)));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Drink")
                .color(Color.RED)
                .build());
        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth("Drink") + 12,0));
    }
}