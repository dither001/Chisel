package team.chisel.common.inventory;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.common.util.NBTUtil;

@Getter
@Setter
@ParametersAreNonnullByDefault
public class ContainerChiselHitech extends ContainerChisel {

    private @Nullable Slot selection, target;
    private List<Slot> selectionDuplicates = ImmutableList.of();
    private @Nullable ICarvingGroup currentGroup;
    
    public ContainerChiselHitech(InventoryPlayer inventoryplayer, InventoryChiselSelection inv, EnumHand hand) {
        super(inventoryplayer, inv, hand);
        
        int selectionSlot = NBTUtil.getHitechSelection(chisel);
        if (selectionSlot >= inv.getSizeInventory()) {
            setSelection(getSlot(selectionSlot));
        }
        
        int targetSlot = NBTUtil.getHitechTarget(chisel);
        if (targetSlot >= 0 && targetSlot < inv.getSizeInventory() - 1) {
            setTarget(getSlot(targetSlot));
        }
    }
    
    public void setSelection(@Nullable Slot slot) {
        this.selection = slot;

        if (slot == null || !slot.getHasStack()) {
            currentGroup = null;
            selectionDuplicates = ImmutableList.of();
            setTarget(null);
        } else {
            ImmutableList.Builder<Slot> builder = ImmutableList.builder();
            for (int i = getInventoryChisel().size + 1; i < inventorySlots.size(); i++) {
                Slot s = getSlot(i);
                if (slot != s && ItemStack.areItemsEqual(slot.getStack(), s.getStack())) {
                    builder.add(s);
                }
            }
            selectionDuplicates = builder.build();
            
            ICarvingGroup group = carving.getGroup(slot.getStack());
            if (group != currentGroup) {
                setTarget(null);
            }
            currentGroup = group;
        }
        
        ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getStack();
        getInventoryChisel().setStackInSpecialSlot(stack);
        getInventoryChisel().updateItems();
    }
    
    public @Nullable ItemStack getSelectionStack() {
        Slot slot = getSelection();
        return slot == null ? ItemStack.EMPTY : slot.getStack();
    }
    
    public ItemStack getTargetStack() {
        Slot slot = getTarget();
        return slot == null ? ItemStack.EMPTY : slot.getStack();    
    }

    @Override
    protected void addSlots() {
        int top = 8, left = 88;

        // selection slots
        for (int i = 0; i < getInventoryChisel().size; i++) {
            addSlotToContainer(new SlotChiselSelection(this, inventoryChisel, inventoryChisel, i, left + ((i % 9) * 18), top + ((i / 9) * 18)));
        }

        // main slot
        addSlotToContainer(new SlotChiselInput(this, inventoryChisel, getInventoryChisel().size, -1000, 0));

        top += 130;
        // main inv
        for (int i = 0; i < 27; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i + 9, left + ((i % 9) * 18), top + (i / 9) * 18));
        }

        top += 58;
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, left + ((i % 9) * 18), top + (i / 9) * 18));
        }
    }
    
    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        NBTUtil.setChiselTarget(getChisel(), getTargetStack());
        super.onContainerClosed(entityplayer);
    }
    
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId >= 0) {
            Slot slot = getSlot(slotId);
            if (slotId < getInventoryChisel().size) {
                setTarget(slot);
            } else if (dragType == 1) {
                ItemStack toFind = slot.getStack();
                if (!toFind.isEmpty()) {
                    for (int i = 0; i < getInventoryChisel().size; i++) {
                        if (ItemStack.areItemsEqual(toFind, getInventoryChisel().getStackInSlot(i))) {
                            setTarget(getSlot(i));
                        }
                    }
                }
            } else {
                setSelection(slot);
            }
        }
        return ItemStack.EMPTY;
    }
}
