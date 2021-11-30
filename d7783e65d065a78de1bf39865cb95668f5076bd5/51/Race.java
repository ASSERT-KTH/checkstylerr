package Multithreading.Race;

import java.util.concurrent.CountDownLatch;


public class Race {

    // Race Conditions and Critical Sections
    // <http://tutorials.jenkov.com/java-concurrency/race-conditions-and-critical-sections.html#race-conditions-in-critical-sections>


//    public synchronized WalletInfo generateAddress(GenerateWallet generateWallet) {
//
//        CountDownLatch finshedSetup = new CountDownLatch(1);
//
//        final WalletInfo walletInfo = new WalletInfo();
//
//        String walletName = generateWallet.getWalletName();
//
//        String currencyName = generateWallet.getCurrencyName();
//
//        WalletInfo walletInfoDb = iWalletInfoDao.getWalletInfoWithWalletNameAndCurrency(walletName, currencyName);
//
//        if (walletInfoDb == null && genWalletMap.get(walletName) == null) {
//
//            String currency = currencyName.toUpperCase();
//
//            if (currency.equals("BITCOIN")) {
//
//                try {
//                    final WalletManager walletManager = WalletManager.setupWallet(walletName);
//
//                    walletManager.addWalletSetupCompletedListener((wallet) -> {
//
//                        Address address = wallet.currentReceiveAddress();
//                        WalletInfo newWallet = createWalletInfo(walletName, currencyName, address.toString());
//
//                        // set the properties of the walletInfo
//                        walletInfo.setId(newWallet.getId());
//                        walletInfo.setName(newWallet.getName());
//                        walletInfo.setAddress(newWallet.getAddress());
//                        walletInfo.setCurrency(newWallet.getCurrency());
//
//                        walletMangersMap.put(newWallet.getId(), walletManager);
//                        genWalletMap.remove(walletName);
//
//                        // start the count down
//                        finshedSetup.countDown();
//                    });
//
//                    genWalletMap.put(walletName, walletManager);
//
//                    // wait for the completion of the thread
//                    finshedSetup.await();
//                    return walletInfo;
//                } catch (InterruptedException ex) {
//
//                }
//            } else if (currency.equals("ETHEREUM")) {
//                return walletInfo;
//            } else {
//                return walletInfo;
//            }
//        }
//
//        return walletInfo;
//    }

}
