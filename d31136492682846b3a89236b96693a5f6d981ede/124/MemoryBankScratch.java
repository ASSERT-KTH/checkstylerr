
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Maxim Integrated Products, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL MAXIM INTEGRATED PRODUCTS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Maxim Integrated Products
 * shall not be used except as stated in the Maxim Integrated Products
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.dalsemi.onewire.container;

// imports
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.utils.CRC16;

/**
 * Memory bank class for the Scratchpad section of NVRAM iButtons and 1-Wire
 * devices.
 *
 * @version 0.00, 28 Aug 2000
 * @author DS
 */
class MemoryBankScratch implements PagedMemoryBank, ScratchPad {

	// --------
	// --------Static Final Variables
	// --------

	/**
	 * Write Scratchpad Command
	 */
	public static final byte WRITE_SCRATCHPAD_COMMAND = (byte) 0x0F;

	/**
	 * Read Scratchpad Command
	 */
	public static final byte READ_SCRATCHPAD_COMMAND = (byte) 0xAA;

	// --------
	// -------- Variables
	// --------

	/**
	 * Reference to the OneWireContainer this bank resides on.
	 */
	protected OneWireContainer ib;

	/**
	 * block of 0xFF's used for faster read pre-fill of 1-Wire blocks
	 */
	protected byte[] ffBlock;

	/**
	 * Copy Scratchpad Command
	 */
	protected byte COPY_SCRATCHPAD_COMMAND;

	/**
	 * Flag to indicate that speed needs to be set
	 */
	protected boolean doSetSpeed;

	// --------
	// -------- Protected Variables for MemoryBank implementation
	// --------

	/**
	 * Size of memory bank in bytes
	 */
	protected int size;

	/**
	 * Memory bank descriptions
	 */
	protected String bankDescription;

	/**
	 * Memory bank usage flags
	 */
	protected boolean generalPurposeMemory;

	/**
	 * Flag if memory bank is read/write
	 */
	protected boolean readWrite;

	/**
	 * Flag if memory bank is write once (EPROM)
	 */
	protected boolean writeOnce;

	/**
	 * Flag if memory bank is read only
	 */
	protected boolean readOnly;

	/**
	 * Flag if memory bank is non volatile (will not erase when power removed)
	 */
	protected boolean nonVolatile;

	/**
	 * Flag if memory bank needs program Pulse to write
	 */
	protected boolean programPulse;

	/**
	 * Flag if memory bank needs power delivery to write
	 */
	protected boolean powerDelivery;

	/**
	 * Starting physical address in memory bank. Needed for different types of
	 * memory in the same logical memory bank. This can be used to separate them
	 * into two virtual memory banks. Example: DS2406 status page has mixed EPROM
	 * and Volatile RAM.
	 */
	protected int startPhysicalAddress;

	/**
	 * Flag if read back verification is enabled in 'write()'.
	 */
	protected boolean writeVerification;

	// --------
	// -------- Protected Variables for PagedMemoryBank implementation
	// --------

	/**
	 * Number of pages in memory bank
	 */
	protected int numberPages;

	/**
	 * page length in memory bank
	 */
	protected int pageLength;

	/**
	 * Max data length in page packet in memory bank
	 */
	protected int maxPacketDataLength;

	/**
	 * Flag if memory bank has page auto-CRC generation
	 */
	protected boolean pageAutoCRC;

	/**
	 * Flag if reading a page in memory bank provides optional extra information
	 * (counter, tamper protection, SHA-1...)
	 */
	protected boolean extraInfo;

	/**
	 * Length of extra information when reading a page in memory bank
	 */
	protected int extraInfoLength;

	/**
	 * Extra information description when reading page in memory bank
	 */
	protected String extraInfoDescription;

	// --------
	// -------- Constructor
	// --------

	/**
	 * Memory bank contstuctor. Requires reference to the OneWireContainer this
	 * memory bank resides on.
	 */
	public MemoryBankScratch(OneWireContainer ibutton) {

		// keep reference to ibutton where memory bank is
		ib = ibutton;

		// initialize attributes of this memory bank - DEFAULT: DS199X scratchapd
		bankDescription = "Scratchpad";
		generalPurposeMemory = false;
		startPhysicalAddress = 0;
		size = 32;
		readWrite = true;
		writeOnce = false;
		readOnly = false;
		nonVolatile = false;
		programPulse = false;
		powerDelivery = false;
		writeVerification = true;
		numberPages = 1;
		pageLength = 32;
		maxPacketDataLength = 29;
		pageAutoCRC = false;
		extraInfo = true;
		extraInfoLength = 3;
		extraInfoDescription = "Target address, offset";

		// create the ffblock (used for faster 0xFF fills)
		ffBlock = new byte[96];

		for (int i = 0; i < 96; i++)
			ffBlock[i] = (byte) 0xFF;

		// default copy scratchpad command
		COPY_SCRATCHPAD_COMMAND = (byte) 0x55;

		// indicate speed has not been set
		doSetSpeed = true;
	}

	// --------
	// -------- Memory Bank methods
	// --------

	/**
	 * Query to see get a string description of the current memory bank.
	 *
	 * @return String containing the memory bank description
	 */
	public String getBankDescription() {
		return bankDescription;
	}

	/**
	 * Query to see if the current memory bank is general purpose user memory. If it
	 * is NOT then it is Memory-Mapped and writing values to this memory will affect
	 * the behavior of the 1-Wire device.
	 *
	 * @return 'true' if current memory bank is general purpose
	 */
	public boolean isGeneralPurposeMemory() {
		return generalPurposeMemory;
	}

	/**
	 * Query to see if current memory bank is read/write.
	 *
	 * @return 'true' if current memory bank is read/write
	 */
	public boolean isReadWrite() {
		return readWrite;
	}

	/**
	 * Query to see if current memory bank is write write once such as with EPROM
	 * technology.
	 *
	 * @return 'true' if current memory bank can only be written once
	 */
	public boolean isWriteOnce() {
		return writeOnce;
	}

	/**
	 * Query to see if current memory bank is read only.
	 *
	 * @return 'true' if current memory bank can only be read
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Query to see if current memory bank non-volatile. Memory is non-volatile if
	 * it retains its contents even when removed from the 1-Wire network.
	 *
	 * @return 'true' if current memory bank non volatile.
	 */
	public boolean isNonVolatile() {
		return nonVolatile;
	}

	/**
	 * Query to see if current memory bank pages need the adapter to have a
	 * 'ProgramPulse' in order to write to the memory.
	 *
	 * @return 'true' if writing to the current memory bank pages requires a
	 *         'ProgramPulse'.
	 */
	public boolean needsProgramPulse() {
		return programPulse;
	}

	/**
	 * Query to see if current memory bank pages need the adapter to have a
	 * 'PowerDelivery' feature in order to write to the memory.
	 *
	 * @return 'true' if writing to the current memory bank pages requires
	 *         'PowerDelivery'.
	 */
	public boolean needsPowerDelivery() {
		return powerDelivery;
	}

	/**
	 * Query to get the starting physical address of this bank. Physical banks are
	 * sometimes sub-divided into logical banks due to changes in attributes.
	 *
	 * @return physical starting address of this logical bank.
	 */
	public int getStartPhysicalAddress() {
		return startPhysicalAddress;
	}

	/**
	 * Query to get the memory bank size in bytes.
	 *
	 * @return memory bank size in bytes.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Query to get the number of pages in current memory bank.
	 *
	 * @return number of pages in current memory bank
	 */
	public int getNumberPages() {
		return numberPages;
	}

	/**
	 * Query to get page length in bytes in current memory bank.
	 *
	 * @return page length in bytes in current memory bank
	 */
	public int getPageLength() {
		return pageLength;
	}

	/**
	 * Query to get Maximum data page length in bytes for a packet read or written
	 * in the current memory bank. See the 'ReadPagePacket()' and
	 * 'WritePagePacket()' methods. This method is only useful if the current
	 * memory bank is general purpose memory.
	 *
	 * @return max packet page length in bytes in current memory bank
	 */
	public int getMaxPacketDataLength() {
		return maxPacketDataLength;
	}

	/**
	 * Query to see if current memory bank pages can be read with the contents being
	 * verified by a device generated CRC. This is used to see if the
	 * 'ReadPageCRC()' can be used.
	 *
	 * @return 'true' if current memory bank can be read with self generated CRC.
	 */
	public boolean hasPageAutoCRC() {
		return pageAutoCRC;
	}

	/**
	 * Query to see if current memory bank pages when read deliver extra information
	 * outside of the normal data space. Examples of this may be a redirection byte,
	 * counter, tamper protection bytes, or SHA-1 result. If this method returns
	 * true then the methods 'ReadPagePacket()' and 'readPageCRC()' with 'extraInfo'
	 * parameter can be used.
	 *
	 * @return 'true' if reading the current memory bank pages provides extra
	 *         information.
	 *
	 * @deprecated As of 1-Wire API 0.01, replaced by {@link #hasExtraInfo()}
	 */
	public boolean haveExtraInfo() {
		return extraInfo;
	}

	/**
	 * Checks to see if this memory bank's pages deliver extra information outside
	 * of the normal data space, when read. Examples of this may be a redirection
	 * byte, counter, tamper protection bytes, or SHA-1 result. If this method
	 * returns true then the methods with an 'extraInfo' parameter can be used:
	 * {@link #readPage(int,boolean,byte[],int,byte[]) readPage},
	 * {@link #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC}, and
	 * {@link #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket}.
	 *
	 * @return <CODE> true </CODE> if reading the this memory bank's pages provides
	 *         extra information
	 *
	 * @see #readPage(int,boolean,byte[],int,byte[]) readPage(extra)
	 * @see #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC(extra)
	 * @see #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket(extra)
	 * @since 1-Wire API 0.01
	 */
	public boolean hasExtraInfo() {
		return extraInfo;
	}

	/**
	 * Query to get the length in bytes of extra information that is read when read
	 * a page in the current memory bank. See 'hasExtraInfo()'.
	 *
	 * @return number of bytes in Extra Information read when reading pages in the
	 *         current memory bank.
	 */
	public int getExtraInfoLength() {
		return extraInfoLength;
	}

	/**
	 * Query to get a string description of what is contained in the Extra
	 * Informationed return when reading pages in the current memory bank. See
	 * 'hasExtraInfo()'.
	 *
	 * @return string describing extra information.
	 */
	public String getExtraInfoDescription() {
		return extraInfoDescription;
	}

	/**
	 * Set the write verification for the 'write()' method.
	 *
	 * @param doReadVerf true (default) verify write in 'write' false, don't verify
	 *                   write (used on Write-Once bit manipulation)
	 */
	public void setWriteVerification(boolean doReadVerf) {
		writeVerification = doReadVerf;
	}

	// --------
	// -------- I/O methods
	// --------

	/**
	 * Read memory in the current bank with no CRC checking (device or data). The
	 * resulting data from this API may or may not be what is on the 1-Wire device.
	 * It is recommends that the data contain some kind of checking (CRC) like in
	 * the readPagePacket() method or have the 1-Wire device provide the CRC as in
	 * readPageCRC(). readPageCRC() however is not supported on all memory types,
	 * see 'hasPageAutoCRC()'. If neither is an option then this method could be
	 * called more then once to at least verify that the same thing is read
	 * consistently.
	 *
	 * @param startAddr    starting address
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new read()
	 *                     continuous where the last one led off and it is inside a
	 *                     'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to place read data into
	 * @param offset       offset into readBuf to place data
	 * @param len          length in bytes to read
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void read(int startAddr, boolean readContinue, byte[] readBuf, int offset, int len)
			throws OneWireIOException, OneWireException {

		// check if read exceeds memory
		if ((startAddr + len) > size)
			throw new OneWireException("Read exceeds memory bank end");

		// attempt to put device at speed
		checkSpeed();

		// read the scratchpad, discard extra information
		readScratchpad(readBuf, offset, len, null);
	}

	/**
	 * Write memory in the current bank. It is recommended that when writing data
	 * that some structure in the data is created to provide error free reading back
	 * with read(). Or the method 'writePagePacket()' could be used which
	 * automatically wraps the data in a length and CRC.
	 *
	 * When using on Write-Once devices care must be taken to write into into empty
	 * space. If write() is used to write over an unlocked page on a Write-Once
	 * device it will fail. If write verification is turned off with the method
	 * 'setWriteVerification(false)' then the result will be an 'AND' of the
	 * existing data and the new data.
	 *
	 * @param startAddr starting address
	 * @param writeBuf  byte array containing data to write
	 * @param offset    offset into writeBuf to get data
	 * @param len       length in bytes to write
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void write(int startAddr, byte[] writeBuf, int offset, int len) throws OneWireIOException, OneWireException {

		// return if nothing to do
		if (len == 0)
			return;

		// attempt to put device at speed
		checkSpeed();

		// check if write exceeds memory
		if ((startAddr + len) > size)
			throw new OneWireException("Write exceeds memory bank end");

		// write the page of data to scratchpad
		writeScratchpad(startPhysicalAddress + startAddr, writeBuf, offset, len);

		// read to verify ok
		byte[] raw_buf = new byte[pageLength];
		byte[] extra_buf = new byte[extraInfoLength];

		readScratchpad(raw_buf, 0, pageLength, extra_buf);

		// check to see if the same
		for (int i = 0; i < len; i++)
			if (raw_buf[i] != writeBuf[i + offset]) {
				forceVerify();

				throw new OneWireIOException("Read back verify had incorrect data");
			}

		// check to make sure that the address is correct
		if ((((extra_buf[0] & 0x00FF) | ((extra_buf[1] << 8) & 0x00FF00)) & 0x00FFFF) != (startPhysicalAddress
				+ startAddr)) {
			forceVerify();

			throw new OneWireIOException("Read back address had incorrect data");
		}
	}

	// --------
	// -------- PagedMemoryBank I/O methods
	// --------

	/**
	 * Read page in the current bank with no CRC checking (device or data). The
	 * resulting data from this API may or may not be what is on the 1-Wire device.
	 * It is recommends that the data contain some kind of checking (CRC) like in
	 * the readPagePacket() method or have the 1-Wire device provide the CRC as in
	 * readPageCRC(). readPageCRC() however is not supported on all memory types,
	 * see 'hasPageAutoCRC()'. If neither is an option then this method could be
	 * called more then once to at least verify that the same thing is read
	 * consistently.
	 *
	 * @param page         page number to read packet from
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new readPage()
	 *                     continuous where the last one led off and it is inside a
	 *                     'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to place read data into
	 * @param offset       offset into readBuf to place data
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void readPage(int page, boolean readContinue, byte[] readBuf, int offset)
			throws OneWireIOException, OneWireException {

		// check if read exceeds memory
		if (page != 0)
			throw new OneWireException("Page read exceeds memory bank end");

		// attempt to put device at speed
		checkSpeed();

		// read the scratchpad, discard extra information
		readScratchpad(readBuf, offset, pageLength, null);
	}

	/**
	 * Read page with extra information in the current bank with no CRC checking
	 * (device or data). The resulting data from this API may or may not be what is
	 * on the 1-Wire device. It is recommends that the data contain some kind of
	 * checking (CRC) like in the readPagePacket() method or have the 1-Wire device
	 * provide the CRC as in readPageCRC(). readPageCRC() however is not supported
	 * on all memory types, see 'hasPageAutoCRC()'. If neither is an option then
	 * this method could be called more then once to at least verify that the same
	 * thing is read consistently. See the method 'hasExtraInfo()' for a description
	 * of the optional extra information some devices have.
	 *
	 * @param page         page number to read packet from
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new readPage()
	 *                     continuous where the last one led off and it is inside a
	 *                     'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to place read data into
	 * @param offset       offset into readBuf to place data
	 * @param extraInfo    byte array to put extra info read into
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void readPage(int page, boolean readContinue, byte[] readBuf, int offset, byte[] extraInfo)
			throws OneWireIOException, OneWireException {

		// check if read exceeds memory
		if (page != 0)
			throw new OneWireException("Page read exceeds memory bank end");

		// attempt to put device at speed
		checkSpeed();

		// read the scratchpad, discard extra information
		readScratchpad(readBuf, offset, pageLength, extraInfo);
	}

	/**
	 * Read a Universal Data Packet.
	 *
	 * The Universal Data Packet always starts on page boundaries but can end
	 * anywhere in the page. The structure specifies the length of data bytes not
	 * including the length byte and the CRC16 bytes. There is one length byte. The
	 * CRC16 is first initialized to the page number. This provides a check to
	 * verify the page that was intended is being read. The CRC16 is then calculated
	 * over the length and data bytes. The CRC16 is then inverted and stored low
	 * byte first followed by the high byte. This is structure is used by this
	 * method to verify the data but is not returned, only the data payload is
	 * returned.
	 *
	 * @param page         page number to read packet from
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new
	 *                     readPagePacket() continuous where the last one stopped
	 *                     and it is inside a 'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to put data read. Must have at least
	 *                     'getMaxPacketDataLength()' elements.
	 * @param offset       offset into readBuf to place data
	 *
	 * @return number of data bytes read from the device and written to readBuf at
	 *         the offset.
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public int readPagePacket(int page, boolean readContinue, byte[] readBuf, int offset)
			throws OneWireIOException, OneWireException {
		byte[] raw_buf = new byte[pageLength];

		// attempt to put device at speed
		checkSpeed();

		// read the scratchpad, discard extra information
		readScratchpad(raw_buf, 0, pageLength, null);

		// check if length is realistic
		if (raw_buf[0] > maxPacketDataLength) {
			forceVerify();

			throw new OneWireIOException("Invalid length in packet");
		}

		// verify the CRC is correct
		if (CRC16.compute(raw_buf, 0, raw_buf[0] + 3, page) == 0x0000B001) {

			// extract the data out of the packet
			System.arraycopy(raw_buf, 1, readBuf, offset, raw_buf[0]);

			// return the length
			return raw_buf[0];
		} else {
			forceVerify();

			throw new OneWireIOException("Invalid CRC16 in packet read");
		}
	}

	/**
	 * Read a Universal Data Packet and extra information. See the method
	 * 'readPagePacket()' for a description of the packet structure. See the method
	 * 'hasExtraInfo()' for a description of the optional extra information some
	 * devices have.
	 *
	 * @param page         page number to read packet from
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new
	 *                     readPagePacket() continuous where the last one stopped
	 *                     and it is inside a 'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to put data read. Must have at least
	 *                     'getMaxPacketDataLength()' elements.
	 * @param offset       offset into readBuf to place data
	 * @param extraInfo    byte array to put extra info read into
	 *
	 * @return number of data bytes read from the device and written to readBuf at
	 *         the offset.
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public int readPagePacket(int page, boolean readContinue, byte[] readBuf, int offset, byte[] extraInfo)
			throws OneWireIOException, OneWireException {
		byte[] raw_buf = new byte[pageLength];

		// attempt to put device at speed
		checkSpeed();

		// read the scratchpad, discard extra information
		readScratchpad(raw_buf, 0, pageLength, extraInfo);

		// check if length is realistic
		if (raw_buf[0] > maxPacketDataLength) {
			forceVerify();

			throw new OneWireIOException("Invalid length in packet");
		}

		// verify the CRC is correct
		if (CRC16.compute(raw_buf, 0, raw_buf[0] + 3, page) == 0x0000B001) {

			// extract the data out of the packet
			System.arraycopy(raw_buf, 1, readBuf, offset, raw_buf[0]);

			// return the length
			return raw_buf[0];
		} else {
			forceVerify();

			throw new OneWireIOException("Invalid CRC16 in packet read");
		}
	}

	/**
	 * Write a Universal Data Packet. See the method 'readPagePacket()' for a
	 * description of the packet structure.
	 *
	 * @param page     page number to write packet to
	 * @param writeBuf data byte array to write
	 * @param offset   offset into writeBuf where data to write is
	 * @param len      number of bytes to write
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void writePagePacket(int page, byte[] writeBuf, int offset, int len)
			throws OneWireIOException, OneWireException {

		// make sure length does not exceed max
		if (len > maxPacketDataLength)
			throw new OneWireException("Length of packet requested exceeds page size");

		// see if this bank is general read/write
		if (!generalPurposeMemory)
			throw new OneWireException("Current bank is not general purpose memory");

		// construct the packet to write
		byte[] raw_buf = new byte[len + 3];

		raw_buf[0] = (byte) len;

		System.arraycopy(writeBuf, offset, raw_buf, 1, len);

		int crc = CRC16.compute(raw_buf, 0, len + 1, page);

		raw_buf[len + 1] = (byte) (~crc & 0xFF);
		raw_buf[len + 2] = (byte) (((~crc & 0xFFFF) >>> 8) & 0xFF);

		// write the packet, return result
		write(page * pageLength, raw_buf, 0, len + 3);
	}

	/**
	 * Read a complete memory page with CRC verification provided by the device. Not
	 * supported by all devices. See the method 'hasPageAutoCRC()'.
	 *
	 * @param page         page number to read
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new
	 *                     readPagePacket() continuous where the last one stopped
	 *                     and it is inside a 'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to put data read. Must have at least
	 *                     'getMaxPacketDataLength()' elements.
	 * @param offset       offset into readBuf to place data
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void readPageCRC(int page, boolean readContinue, byte[] readBuf, int offset)
			throws OneWireIOException, OneWireException {

		// only needs to be implemented if supported by hardware
		throw new OneWireException("Read page with CRC not supported by this memory bank");
	}

	/**
	 * Read a complete memory page with CRC verification provided by the device with
	 * extra information. Not supported by all devices. See the method
	 * 'hasPageAutoCRC()'. See the method 'hasExtraInfo()' for a description of the
	 * optional extra information.
	 *
	 * @param page         page number to read
	 * @param readContinue if 'true' then device read is continued without
	 *                     re-selecting. This can only be used if the new
	 *                     readPagePacket() continuous where the last one stopped
	 *                     and it is inside a 'beginExclusive/endExclusive' block.
	 * @param readBuf      byte array to put data read. Must have at least
	 *                     'getMaxPacketDataLength()' elements.
	 * @param offset       offset into readBuf to place data
	 * @param extraInfo    byte array to put extra info read into
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void readPageCRC(int page, boolean readContinue, byte[] readBuf, int offset, byte[] extraInfo)
			throws OneWireIOException, OneWireException {

		// only needs to be implemented if supported by hardware
		throw new OneWireException("Read page with CRC and extra-info not supported by this memory bank");
	}

	// --------
	// -------- ScratchPad methods
	// --------

	/**
	 * Read the scratchpad page of memory from a NVRAM device This method reads and
	 * returns the entire scratchpad after the byte offset regardless of the actual
	 * ending offset
	 *
	 * @param readBuf   byte array to place read data into length of array is always
	 *                  pageLength.
	 * @param offset    offset into readBuf to pug data
	 * @param len       length in bytes to read
	 * @param extraInfo byte array to put extra info read into (TA1, TA2, e/s byte)
	 *                  length of array is always extraInfoLength. Can be 'null' if
	 *                  extra info is not needed.
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void readScratchpad(byte[] readBuf, int offset, int len, byte[] extraInfo)
			throws OneWireIOException, OneWireException {
		// select the device
		if (!ib.adapter.select(ib.address)) {
			forceVerify();

			throw new OneWireIOException("Device select failed");
		}

		// build first block
		byte[] raw_buf = new byte[1 + extraInfoLength];

		raw_buf[0] = READ_SCRATCHPAD_COMMAND;

		System.arraycopy(ffBlock, 0, raw_buf, 1, extraInfoLength);

		// do the first block for TA1, TA2, and E/S
		ib.adapter.dataBlock(raw_buf, 0, 1 + extraInfoLength);

		// optionally extract the extra info
		if (extraInfo != null)
			System.arraycopy(raw_buf, 1, extraInfo, 0, extraInfoLength);

		// build the next block
		System.arraycopy(ffBlock, 0, readBuf, offset, len);

		// send second block to read data, return result
		ib.adapter.dataBlock(readBuf, offset, len);
	}

	/**
	 * Write to the scratchpad page of memory a NVRAM device.
	 *
	 * @param startAddr starting address
	 * @param writeBuf  byte array containing data to write
	 * @param offset    offset into readBuf to place data
	 * @param len       length in bytes to write
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void writeScratchpad(int startAddr, byte[] writeBuf, int offset, int len)
			throws OneWireIOException, OneWireException {

		// select the device
		if (!ib.adapter.select(ib.address)) {
			forceVerify();

			throw new OneWireIOException("Device select failed");
		}

		// build block to send
		byte[] raw_buf = new byte[3 + len];

		raw_buf[0] = WRITE_SCRATCHPAD_COMMAND;
		raw_buf[1] = (byte) (startAddr & 0xFF);
		raw_buf[2] = (byte) (((startAddr & 0xFFFF) >>> 8) & 0xFF);

		System.arraycopy(writeBuf, offset, raw_buf, 3, len);

		// send block, return result
		ib.adapter.dataBlock(raw_buf, 0, len + 3);
	}

	/**
	 * Copy the scratchpad page to memory.
	 *
	 * @param startAddr starting address
	 * @param len       length in bytes that was written already
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void copyScratchpad(int startAddr, int len) throws OneWireIOException, OneWireException {

		// select the device
		if (!ib.adapter.select(ib.address)) {
			forceVerify();

			throw new OneWireIOException("Device select failed");
		}

		// build block to send
		byte[] raw_buf = new byte[5];

		raw_buf[0] = COPY_SCRATCHPAD_COMMAND;
		raw_buf[1] = (byte) (startAddr & 0xFF);
		raw_buf[2] = (byte) (((startAddr & 0xFFFF) >>> 8) & 0xFF);
		raw_buf[3] = (byte) ((startAddr + len - 1) & 0x1F);
		raw_buf[4] = (byte) 0xFF;

		// send block (check copy indication complete)
		ib.adapter.dataBlock(raw_buf, 0, 5);

		if ((raw_buf[4] & 0x0F0) != 0) {
			forceVerify();

			throw new OneWireIOException("Copy scratchpad complete not found");
		}
	}

	/**
	 * Copy the scratchpad page to memory.
	 *
	 * @param startAddr starting address
	 * @param len       length in bytes that was written already
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void copyScratchpad(int startAddr, int len, boolean WriteProtect)
			throws OneWireIOException, OneWireException {
		int i, j;

		if (WriteProtect)
			i = 2;
		else
			i = 0;

		for (j = 0; j < i; j++) {
			// select the device
			if (!ib.adapter.select(ib.address)) {
				forceVerify();

				throw new OneWireIOException("Device select failed");
			}

			// build block to send
			byte[] raw_buf = new byte[5];

			raw_buf[0] = COPY_SCRATCHPAD_COMMAND;
			raw_buf[1] = (byte) (startAddr & 0xFF);
			raw_buf[2] = (byte) (((startAddr & 0xFFFF) >>> 8) & 0xFF);
			raw_buf[3] = (byte) ((startAddr + len - 1) & 0x1F);
			raw_buf[3] = (byte) (raw_buf[3] | 0x80);
			raw_buf[4] = (byte) 0xFF;

			// send block (check copy indication complete)
			ib.adapter.dataBlock(raw_buf, 0, 5);

			if ((raw_buf[4] & 0x0F0) != 0) {
				forceVerify();

				throw new OneWireIOException("Copy scratchpad complete not found");
			}
		}
	}

	// --------
	// -------- checkSpeed methods
	// --------

	/**
	 * Check the device speed if has not been done before or if an error was
	 * detected.
	 *
	 * @throws OneWireIOException
	 * @throws OneWireException
	 */
	public void checkSpeed() throws OneWireIOException, OneWireException {
		synchronized (this) {
			// only check the speed
			// 9-23-2003 shughes: added the check to see if the adapter is
			// currently at the ibutton's max speed. If it isn't, we should
			// call the doSpeed() method, since the adapter might have
			// changed speeds.
			if (doSetSpeed || (ib.adapter.getSpeed() != ib.getMaxSpeed())) {
				// attempt to set the correct speed and verify device present
				ib.doSpeed();

				// no exceptions so clear flag
				doSetSpeed = false;
			}
		}
	}

	/**
	 * Set the flag to indicate the next 'checkSpeed()' will force a speed set and
	 * verify 'doSpeed()'.
	 */
	public void forceVerify() {
		synchronized (this) {
			doSetSpeed = true;
		}
	}
}
