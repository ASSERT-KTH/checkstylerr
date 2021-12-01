/**
 * Copyright (c) 2020-2021, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.api;

import java.time.LocalDateTime;

/**
 * Platform invoices.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.50
 */
public interface PlatformInvoices extends Iterable<PlatformInvoice> {

    /**
     * Get a PlatformInvoice by its ID.
     * @param id The ID.
     * @return PlatformInvoice or null if it's not found.
     */
    PlatformInvoice getById(final int id);

    /**
     * Get a PlatformInvoice by the payment coordinates. TransactionId and
     * paymentTime should actually be the primary key of the PlatformInvoice.
     * They are not because of technical reasons (the corresponding Invoice can
     * be deleted, whereas a PlatformInvoice should never be deleted).
     *
     * @param transactionId String transaction (payment) id.
     * @param paymentTime Payment time.
     * @return PlatformInvoice or null if it's not found.
     */
    PlatformInvoice getByPayment(
        final String transactionId,
        final LocalDateTime paymentTime
    );
}
