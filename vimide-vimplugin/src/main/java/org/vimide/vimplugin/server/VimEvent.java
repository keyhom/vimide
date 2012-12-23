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
package org.vimide.vimplugin.server;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimEvent {

    private int bufferId;
    private int seqno;
    private String name;
    private String rawData;
    private String seqData;

    /**
     * Creates an new VimEvent instance.
     * 
     * @param bufferId
     * @param name
     * @param seqno
     * @param data
     */
    public VimEvent(int bufferId, String name, int seqno, String data) {
        super();
        this.bufferId = bufferId;
        this.seqno = seqno;
        this.name = name;
        setData(data);
    }

    /**
     * @param data
     */
    private void setData(String data) {
        this.rawData = data;
        if (null != data)
            this.seqData = new String(data);
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getSeqno() {
        return seqno;
    }

    public String getName() {
        return name;
    }

    public String getRawData() {
        return rawData;
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

    public String nextString() throws Exception {
        checkData();
        return next().replace("\"", "");
    }

    public int nextNumber() throws Exception {
        checkData();
        return Integer.valueOf(next());
    }

    public boolean nextBoolean() throws Exception {
        checkData();
        return next().equals("T") ? true : false;
    }

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
