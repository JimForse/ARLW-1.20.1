package rw.modden.character;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import rw.modden.weapon.TrainingBaton;
import rw.modden.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

public class ModInventory {
    private final List<Weapon> weapons;
    private final List<String> accessories;

    public ModInventory() {
        this.weapons = new ArrayList<>();
        this.accessories = new ArrayList<>();
    }

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public void addAccessory(String accessory) {
        accessories.add(accessory);
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public List<String> getAccessories() {
        return accessories;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList weaponList = new NbtList();
        for (Weapon weapon : weapons) {
            if (weapon instanceof TrainingBaton) {
                weaponList.add(NbtString.of("TrainingClub"));
            }
            // TODO: Добавить поддержку других типов оружия
        }
        nbt.put("Weapons", weaponList);

        NbtList accessoryList = new NbtList();
        for (String accessory : accessories) {
            accessoryList.add(NbtString.of(accessory));
        }
        nbt.put("Accessories", accessoryList);
        return nbt;
    }

    public static ModInventory fromNbt(NbtCompound nbt) {
        ModInventory inventory = new ModInventory();
        NbtList weaponList = nbt.getList("Weapons", NbtString.STRING_TYPE);
        for (int i = 0; i < weaponList.size(); i++) {
            String weaponType = weaponList.getString(i);
            if (weaponType.equals("TrainingClub")) {
                inventory.weapons.add(new TrainingBaton());
            }
            // TODO: Добавить поддержку других типов оружия
        }
        NbtList accessoryList = nbt.getList("Accessories", NbtString.STRING_TYPE);
        for (int i = 0; i < accessoryList.size(); i++) {
            inventory.accessories.add(accessoryList.getString(i));
        }
        return inventory;
    }
}