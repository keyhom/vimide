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
public class VimData {

    private int bufferId;
    private int seqno;
    private String name;
    private String data;
    private VimBufferSession session;
    private int type;

    /**
     * Creates an new VimData instance.
     * 
     * @param session
     * @param seqno
     * @param type
     */
    protected VimData(VimBufferSession session, int seqno, int type) {
        super();
        this.session = session;
        this.seqno = seqno;
        this.type = type;
    }

    /**
     * Creates an new VimData instance.
     * 
     * @param session
     * @param seqno
     * @param data
     */
    protected VimData(VimBufferSession session, int seqno, String data) {
        super();
        this.session = session;
        this.seqno = seqno;
        this.data = data;
    }

    /**
     * Creates an new VimData instance.
     * 
     * @param session
     * @param bufferId
     * @param seqno
     * @param name
     * @param data
     */
    protected VimData(VimBufferSession session, int bufferId, int seqno,
            String name, String data) {
        super();
        this.session = session;
        this.bufferId = bufferId;
        this.seqno = seqno;
        this.name = name;
        this.data = data;
    }

    /**
     * Gets the value of bufferId property.
     * 
     * @return the bufferId
     */
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

    /**
     * Gets the value of seqno property.
     * 
     * @return the seqno
     */
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

    /**
     * Gets the value of name property.
     * 
     * @return the name
     */
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

    /**
     * Gets the value of data property.
     * 
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the value of data property.
     * 
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    public void flush() {
        if (null != session) {
            switch (type) {
                case 1:
                case 2:
                    session.getIoSession().write(toString());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case 1: // command.
                sb.append(getBufferId()).append(":").append(getName());
                sb.append("!");
                break;
            case 2: // function.
                sb.append(getBufferId()).append(":").append(getName());
                sb.append("/");
                break;
            case 4: // event.
                sb.append(getBufferId()).append(":").append(getName());
                sb.append("=");
                break;
            default:
                break;
        }

        sb.append(getSeqno());
        sb.append(" ");
        sb.append(data);
        return sb.toString();
    }

}
