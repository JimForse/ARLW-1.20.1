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
    private final List<Character> characters;

    public ModInventory() {
        this.weapons = new ArrayList<>();
        this.accessories = new ArrayList<>();
        this.characters = new ArrayList<>();
    }

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public void addAccessory(String accessory) {
        accessories.add(accessory);
    }

    public void addCharacter(Character character) {
        characters.add(character);
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public List<String> getAccessories() {
        return accessories;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList weaponList = new NbtList();
        for (Weapon weapon : weapons) {
            if (weapon instanceof TrainingBaton) {
                weaponList.add(NbtString.of("TrainingBaton"));
            }
        }
        // TODO: Добавить поддержку других типов оружия
        nbt.put("Weapons", weaponList);

        NbtList accessoryList = new NbtList();
        for (String accessory : accessories) {
            accessoryList.add(NbtString.of(accessory));
        }
        nbt.put("Accessories", accessoryList);

        NbtList characterList = new NbtList();
        for (Character character : characters) {
            NbtCompound charNbt = new NbtCompound();
            charNbt.putString("Type", character.getType().name());
            charNbt.putInt("StarLevel", character.getStarLevel());
            characterList.add(charNbt);
        }
        nbt.put("Characters", characterList);

        return nbt;
    }

    public static ModInventory fromNbt(NbtCompound nbt) {
        ModInventory inventory = new ModInventory();
        NbtList weaponList = nbt.getList("Weapons", NbtString.STRING_TYPE);
        for (int i = 0; i < weaponList.size(); i++) {
            String weaponType = weaponList.getString(i);
            if (weaponType.equals("TrainingBaton")) {
                inventory.weapons.add(new TrainingBaton());
            }
            // TODO: Добавить поддержку других типов оружия
        }
        NbtList accessoryList = nbt.getList("Accessories", NbtString.STRING_TYPE);
        for (int i = 0; i < accessoryList.size(); i++) {
            inventory.accessories.add(accessoryList.getString(i));
        }
        NbtList characterList = nbt.getList("Characters", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < characterList.size(); i++) {
            NbtCompound charNbt = characterList.getCompound(i);
            String type = charNbt.getString("Type");
            int starLevel = charNbt.getInt("StarLevel");
            Character character = CharacterInitializer.getCharacter(
                    type.equals(Character.CharacterType.SUPPORT.name()) ? "kllima777" : "unknown"
            );
            if (character != null) {
                inventory.characters.add(character);
            }
        }
        return inventory;
    }
}