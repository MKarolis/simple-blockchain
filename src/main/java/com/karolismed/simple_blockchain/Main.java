package com.karolismed.simple_blockchain;

import com.karolismed.simple_blockchain.blockchain.BlockChainRunner;

public class Main {

    public static void main(String[] args) {
        BlockChainRunner runner = new BlockChainRunner();
        try {
            runner.init();
        } catch (Exception ex) {
            System.out.println("Unhandled exception occurred: ");
            ex.printStackTrace();
        }

    }
}
