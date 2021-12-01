package com.amihaiemil.zold;

/**
 * Zold Wallet.
 * @author Ammar Atef (ammar.atef45@gmail.com)
 * @version $Id: dd1493041e882f451bf0679f0f481714c86c7c5d $
 * @since 0.0.1
 */
public interface Wallet {
    /**
    * Get the balance of the wallet.
    * @return Balance
    */
    double balance();

    /**
    * Pay to another wallet.
    * @param keygap Sender keygap
    * @param user Recipient user id
    * @param amount Amount to be sent
    * @param details The details of transfer
    * @todo #11:30min solve checkstyle paramternumber error either by cahnging the
    * method structure or supressing the warning
    */
    void pay(String keygap, String user, double amount, String details);

    /**
    * Finds all payments that match this query and returns.
    * @param id Wallet id
    * @param details Regex of payment details
    */
    void find(String id, String details);
}