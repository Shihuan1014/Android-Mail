package net.novaborn.entity;

/**
 * @description:
 * @author: 周世焕
 * @time: 2020-05-27 23:26
 */
public class Block {
    String userAddress;
    String blockAddress;

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getBlockAddress() {
        return blockAddress;
    }

    public void setBlockAddress(String blockAddress) {
        this.blockAddress = blockAddress;
    }

    @Override
    public String toString() {
        return "Block{" +
                "userAddress='" + userAddress + '\'' +
                ", blockAddress='" + blockAddress + '\'' +
                '}';
    }
}
