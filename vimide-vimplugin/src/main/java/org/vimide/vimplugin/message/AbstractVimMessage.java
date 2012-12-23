/**
 * Copyright (c) 2012 keyhom.c@gmail.com.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose
 * excluding commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *     2. Altered source versions must be plainly marked as such, and must not
 *     be misrepresented as being the original software.
 *
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */
package org.vimide.vimplugin.message;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public abstract class AbstractVimMessage implements VimMessage {

    private static final long serialVersionUID = 1L;

    private int bufferId;
    private String name;
    private int seqno;
    private String rawData;
    private String seqData;

    /**
     * Creates an new AbstractVimMessage instance.
     */
    public AbstractVimMessage() {
        super();
    }

    /**
     * Creates an new AbstractVimMessage instance.
     * 
     * @param bufferId
     * @param name
     * @param seqno
     * @param data
     */
    protected AbstractVimMessage(int bufferId, String name, int seqno,
            String data) {
        super();
        this.bufferId = bufferId;
        this.name = name;
        this.seqno = seqno;
        setData(data);
    }

    @Override
    public int getBufferId() {
        return bufferId;
    }

    /**
     * Sets the value of bufferId property.
     * 
     * @param bufferId the bufferId to set
     */
    public void setBufferId(int bufferId) {
        this.bufferId = bufferId;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the value of name property.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getSeqno() {
        return seqno;
    }

    /**
     * Sets the value of seqno property.
     * 
     * @param seqno the seqno to set
     */
    public void setSeqno(int seqno) {
        this.seqno = seqno;
    }

    @Override
    public String getData() {
        return rawData;
    }

    /**
     * Sets the value of data property.
     * 
     * @param data the data to set
     */
    public void setData(String data) {
        this.rawData = data;
        if (null != data)
            this.seqData = new String(data);
    }

    private void checkData() throws Exception {
        if (null == seqData) {
            throw new IllegalAccessException("No enough data.");
        }
    }

    private String next() {
        String[] temp = seqData.split(" ", 2);
        if (temp.length == 1)
            seqData = null;
        else {
            seqData = temp[1];
        }
        return temp[0];
    }

    @Override
    public String nextString() throws Exception {
        checkData();
        return next().replace("\"", "");
    }

    @Override
    public int nextNumber() throws Exception {
        checkData();
        return Integer.valueOf(next());
    }

    @Override
    public boolean nextBoolean() throws Exception {
        checkData();
        return next().equals("T") ? true : false;
    }

    @Override
    public int[] nextLnumCol() throws Exception {
        checkData();
        String[] s = next().split("/", 2);
        int[] ia = new int[2];
        for (int i = 0; i < 2; i++) {
            ia[i] = Integer.parseInt(s[i]);
        }
        return ia;
    }

}
